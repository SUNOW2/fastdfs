package com.software.yanchang.service.Impl;

import com.software.yanchang.dao.FastdfsMapper;
import com.software.yanchang.domain.FastdfsFile;
import com.software.yanchang.service.StorageService;
import com.software.yanchang.utils.*;
import org.apache.catalina.connector.ClientAbortException;
import org.csource.common.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

import static com.software.yanchang.utils.FastdfsConstants.SUCCESS;

@Service
public class StorageServiceImpl implements StorageService {

    @Autowired
    private FastdfsMapper fastdfsMapper;

    @Autowired
    private FastdfsOperation fastdfsOperation;

    @Value("${breakpoint.upload.dir1}")
    private String finalDirPath;


    /**
     * 从应用服务器下载文件至浏览器，支持断点续传
     *
     * @param srcPath
     * @param request
     * @param response
     * @throws IOException
     * @throws MyException
     */
    public void downloadFileThrowBrowser(String srcPath, HttpServletRequest request, HttpServletResponse response) throws IOException {

        File file = new File(srcPath);
//        开始下载地方
        long startByte = 0;
//        结束下载地方
        long endByte = file.length() - 1;
        String range = request.getHeader("Content-Range");
//        主要用于下载的断点续传
        if (range != null && range.contains("bytes=") && range.contains("-")) {
            range = range.substring(range.lastIndexOf("=") + 1).trim();
            String ranges[] = range.split("-");
            try {
//                判断range类型
                if (ranges.length == 1) {
                    if (range.startsWith("-")) {
                        endByte = Long.parseLong(ranges[0]);
                    } else if (range.endsWith("-")) {
                        startByte = Long.parseLong(ranges[0]);
                    }
                } else if (ranges.length == 2) {
                    startByte = Long.parseLong(ranges[0]);
                    endByte = Long.parseLong(ranges[1]);
                }

            } catch (NumberFormatException e) {
                startByte = 0;
                endByte = file.length() - 1;
            }
        }
//        要下载的长度
        long contentLength = endByte - startByte + 1;

//        文件名
        String fileName = URLEncoder.encode(file.getName(), "UTF-8");
//        文件类型
        String contentType = request.getServletContext().getMimeType(fileName);

        response.setHeader("Accept-Ranges", "bytes");
//        状态码设为206
        response.setStatus(response.SC_PARTIAL_CONTENT);
        response.setHeader("Content-Type", contentType);
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length());

//        清除首部的空白行，必须加上这句，不然无法在chrome中使用,另外放在Content-Disposition属性之前，不然无法设置文件名
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        BufferedOutputStream outputStream = null;
        RandomAccessFile randomAccessFile = null;
//        已传送数据大小
        long transmitted = 0;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            outputStream = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[4096];
            int len = 0;
            randomAccessFile.seek(startByte);
            while ((transmitted + len) <= contentLength && (len = randomAccessFile.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
                transmitted += len;
            }
            //处理不足buff.length部分
            if (transmitted < contentLength) {
                len = randomAccessFile.read(buff, 0, (int) (contentLength - transmitted));
                outputStream.write(buff, 0, len);
                transmitted += len;
            }

            outputStream.flush();
            response.flushBuffer();
            randomAccessFile.close();
            outputStream.close();
            System.out.println("下载完毕：" + startByte + "-" + endByte + "：" + transmitted);
            DirectoryOperation.deleteDirectoryOperation(new File(finalDirPath + file.getName()));
        } catch (ClientAbortException e) {
            System.out.println("用户停止下载：" + startByte + "-" + endByte + "：" + transmitted);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 将文件从文件服务器下载到应用服务器
     *
     * @param id
     * @return
     * @throws IOException
     * @throws MyException
     */
    public void applicationToFile(String id, HttpServletRequest request, HttpServletResponse response) throws IOException, MyException {
        String fileparts = fastdfsMapper.getFileParts(id).getFileParts();
        String[] parts = fileparts.split(";");
        String fileTmpPath = finalDirPath + parts[0];

        String str = fastdfsOperation.fastdfsDownloadFile(fileTmpPath, parts);

        if(str.equals(SUCCESS)) {
            downloadFileThrowBrowser(fileTmpPath, request, response);
        }
    }

    /**
     * 删除数据库中文件记录以及文件系统中对应的文件
     * @param fastdfsFile
     */
    @Override
    @Transactional
    public void deleteFile(FastdfsFile fastdfsFile) throws IOException, MyException {
        fastdfsOperation.fastdfsDeleteFile(fastdfsMapper.getFileParts(fastdfsFile.getId()).getFileParts().split(";"));
        fastdfsMapper.deleteFile(fastdfsFile);
    }
}
