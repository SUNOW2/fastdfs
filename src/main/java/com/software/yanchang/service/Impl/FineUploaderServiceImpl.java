package com.software.yanchang.service.Impl;


import com.software.yanchang.service.FineUploaderService;
import com.software.yanchang.utils.FastdfsOperation;
import com.software.yanchang.utils.FastdfsResults;
import com.software.yanchang.utils.MultipartUploadParser;
import com.software.yanchang.utils.RequestParser;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.regex.Pattern;


@Service
@Configuration
public class FineUploaderServiceImpl implements FineUploaderService {

    @Value("${breakpoint.upload.dir.tmp}")
    private String TEMP_DIR;

    @Value("${breakpoint.upload.dir.fin}")
    private String UPLOAD_DIR;


    private static String CONTENT_LENGTH = "Content-Length";
    private static int SUCCESS_RESPONSE_CODE = 200;

    final Logger log = LoggerFactory.getLogger(FineUploaderServiceImpl.class);

    @Autowired
    private FastdfsOperation fastdfsOperation;

    public void init() throws ServletException {
        new File(UPLOAD_DIR).mkdirs();
    }

    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uuid = req.getPathInfo().replaceAll("/", "");

        handleDeleteFileRequest(uuid, resp);
    }

    public void handleDeleteFileRequest(String uuid, HttpServletResponse resp) throws IOException {
        FileUtils.deleteDirectory(new File(UPLOAD_DIR, uuid));

        if (new File(UPLOAD_DIR, uuid).exists()) {
            log.warn("couldn't find or delete " + uuid);
        } else {
            log.info("deleted " + uuid);
        }

        resp.setStatus(SUCCESS_RESPONSE_CODE);
    }

    public FastdfsResults uploadFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestParser requestParser = null;
        String uri = "";
        System.out.println("UPLOAD_DIR = " + UPLOAD_DIR );
        boolean isIframe = req.getHeader("X-Requested-With") == null || !req.getHeader("X-Requested-With").equals("XMLHttpRequest");

        try {

            if (ServletFileUpload.isMultipartContent(req)) {
                MultipartUploadParser multipartUploadParser = new MultipartUploadParser(req, new File(TEMP_DIR), req.getServletContext());

                requestParser = RequestParser.getInstance(req, multipartUploadParser);

                uri = writeFileForMultipartRequest(requestParser);

            } else {
                requestParser = RequestParser.getInstance(req, null);

                //handle POST delete file request
                if (requestParser.getMethod() != null
                        && requestParser.getMethod().equalsIgnoreCase("DELETE")) {
                    String uuid = requestParser.getUuid();
                    handleDeleteFileRequest(uuid, resp);
                } else {
                    writeFileForNonMultipartRequest(req, requestParser);
                    writeResponse(resp.getWriter(), requestParser.generateError() ? "Generated error" : null, isIframe, false, requestParser);
                }
            }
        } catch (Exception e) {
            log.error("Problem handling upload request", e);
            if (e instanceof MergePartsException) {
                writeResponse(resp.getWriter(), e.getMessage(), isIframe, true, requestParser);
            } else {
                writeResponse(resp.getWriter(), e.getMessage(), isIframe, false, requestParser);
            }
        }
        return new FastdfsResults(true,requestParser.getOriginalFilename(), uri);
    }

    public void writeFileForNonMultipartRequest(HttpServletRequest req, RequestParser requestParser) throws Exception {
        File dir = new File(UPLOAD_DIR, requestParser.getUuid());
        dir.mkdirs();

        String contentLengthHeader = req.getHeader(CONTENT_LENGTH);
        long expectedFileSize = Long.parseLong(contentLengthHeader);

        if (requestParser.getPartIndex() >= 0) {
            writeFile(req.getInputStream(), new File(dir, requestParser.getUuid() + "_" + String.format("%05d", requestParser.getPartIndex())), null);

            if (requestParser.getTotalParts() - 1 == requestParser.getPartIndex()) {
                File[] parts = getPartitionFiles(dir, requestParser.getUuid());
                File outputFile = new File(dir, requestParser.getFilename());
                for (File part : parts) {
                    mergeFiles(outputFile, part);
                }

                assertCombinedFileIsVaid(requestParser.getTotalFileSize(), outputFile, requestParser.getUuid());
                deletePartitionFiles(dir, requestParser.getUuid());
            }
        } else {
            writeFile(req.getInputStream(), new File(dir, requestParser.getFilename()), expectedFileSize);
        }
    }


    public String writeFileForMultipartRequest(RequestParser requestParser) throws Exception {
        File dir = new File(UPLOAD_DIR, requestParser.getUuid());
        dir.mkdirs();
        String uri = "";

        if (requestParser.getPartIndex() >= 0) {
            writeFile(requestParser.getUploadItem().getInputStream(), new File(dir, requestParser.getUuid() + "_" + String.format("%05d", requestParser.getPartIndex())), null);

            if (requestParser.getTotalParts() - 1 == requestParser.getPartIndex()) {
                File[] parts = getPartitionFiles(dir, requestParser.getUuid());
                File outputFile = new File(dir, requestParser.getOriginalFilename());
                for (File part : parts) {
                    mergeFiles(outputFile, part);
                }

                assertCombinedFileIsVaid(requestParser.getTotalFileSize(), outputFile, requestParser.getUuid());
                deletePartitionFiles(dir, requestParser.getUuid());
                uri =  fastdfsOperation.fastdfsUploadFile(UPLOAD_DIR, requestParser);
            }
        } else {
//          未分片文件：直接上传到FastDFS文件系统，下载的时候，直接使用nginx进行反向代理
            writeFile(requestParser.getUploadItem().getInputStream(), new File(dir, requestParser.getFilename()), null);
            uri =  fastdfsOperation.fastdfsUploadFile(UPLOAD_DIR, requestParser);
        }
        return uri;
    }

    private void assertCombinedFileIsVaid(long totalFileSize, File outputFile, String uuid) throws MergePartsException {
        if (totalFileSize != outputFile.length()) {
            deletePartitionFiles(new File(UPLOAD_DIR), uuid);
            outputFile.delete();
            throw new MergePartsException("Incorrect combined file size!");
        }

    }


    private static class PartitionFilesFilter implements FilenameFilter {
        private String filename;

        PartitionFilesFilter(String filename) {
            this.filename = filename;
        }

        @Override
        public boolean accept(File file, String s) {
            return s.matches(Pattern.quote(filename) + "_\\d+");
        }
    }

    private static File[] getPartitionFiles(File directory, String filename) {
        File[] files = directory.listFiles(new PartitionFilesFilter(filename));
        Arrays.sort(files);
        return files;
    }

    private static void deletePartitionFiles(File directory, String filename) {
        File[] partFiles = getPartitionFiles(directory, filename);
        for (File partFile : partFiles) {
            partFile.delete();
        }
    }

    public File mergeFiles(File outputFile, File partFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputFile, true);

        try {
            FileInputStream fis = new FileInputStream(partFile);

            try {
                IOUtils.copy(fis, fos);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        } finally {
            IOUtils.closeQuietly(fos);
        }

        return outputFile;
    }

    public File writeFile(InputStream in, File out, Long expectedFileSize) throws IOException {
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(out);

            IOUtils.copy(in, fos);

            if (expectedFileSize != null) {
                Long bytesWrittenToDisk = out.length();
                if (!expectedFileSize.equals(bytesWrittenToDisk)) {
                    log.warn("Expected file {} to be {} bytes; file on disk is {} bytes", new Object[]{out.getAbsolutePath(), expectedFileSize, 1});
                    out.delete();
                    throw new IOException(String.format("Unexpected file size mismatch. Actual bytes %s. Expected bytes %s.", bytesWrittenToDisk, expectedFileSize));
                }
            }

            return out;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    public void writeResponse(PrintWriter writer, String failureReason, boolean isIframe, boolean restartChunking, RequestParser requestParser) {
        if (!failureReason.equals("Generated error")) {
            writer.print("{\"uri\":" + failureReason + "}");

        } else {
            if (restartChunking) {
                writer.print("{\"error\": \"" + failureReason + "\", \"reset\": true}");
            } else {
                writer.print("{\"error\": \"" + failureReason + "\"}");
            }
        }
    }


    private class MergePartsException extends Exception {
        MergePartsException(String message) {
            super(message);
        }
    }
}
