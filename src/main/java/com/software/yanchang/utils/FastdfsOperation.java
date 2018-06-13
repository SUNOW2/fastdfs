package com.software.yanchang.utils;

import com.software.yanchang.dao.FastdfsMapper;
import com.software.yanchang.domain.FastdfsFile;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.Date;

import static com.software.yanchang.utils.FastdfsConstants.SUCCESS;

@Component
public class FastdfsOperation {

    @Autowired
    private FastdfsMapper fastdfsMapper;

    @Value("${fastdfs.uploadSmallFileUri}")
    private String uploadSmallFileUri;

    @Value("${fastdfs.uploadBigFileUri}")
    private String uploadBigFileUri;

    /**
     * 上传文件至Fastdfs文件服务器
     *
     * @param uploadDir
     * @param requestParser
     * @return
     * @throws IOException
     * @throws MyException
     */
    public String fastdfsUploadFile(String uploadDir, RequestParser requestParser) throws IOException, MyException {

        String uri = "";
        String extName = "";
        String fileName = requestParser.getOriginalFilename();
        String fileParts = fileName;
        String uploadFile = uploadDir + requestParser.getUuid() + "/" + requestParser.getOriginalFilename();
        File file = new File(uploadFile);

        if (fileName.contains(".")) {
            extName = fileName.substring(fileName.lastIndexOf(".") + 1);
        }

        FileInputStream inputStream = new FileInputStream(new File(uploadFile));
        // 设置分片文件大小：40M
        int num = 1024 * 1024 * 40;
        byte[] file_buff = new byte[num];

        // fastdfs用于注释的文件信息
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("fileName", fileName);
        FastdfsPool fastdfsPool = FastdfsPool.getFastdfs();

        if (file.length() <= num) {
            file_buff = new byte[(int) file.length()];
            inputStream.read(file_buff);

            StorageClient1 storageClient1 = fastdfsPool.checkout(10);
            String[] upload_file = storageClient1.upload_file(file_buff, extName, meta_list);
            fastdfsPool.checkin(storageClient1);
            fileParts += ";" + Arrays.asList(upload_file);
        } else {
            while (inputStream.read(file_buff) != -1) {
                //  使用FastDFS连接池
                StorageClient1 storageClient1 = fastdfsPool.checkout(10);
                String[] upload_file = storageClient1.upload_file(file_buff, extName, meta_list);
                fastdfsPool.checkin(storageClient1);
                fileParts += ";" + Arrays.asList(upload_file);
            }
        }

        //  新增数据库文件记录
        FastdfsFile fastdfsFile = new FastdfsFile();
        fastdfsFile.setFileParts(fileParts);
        fastdfsFile.setId(fastdfsMapper.createId());
        fastdfsFile.setFastdfsDate(new Date());
        fastdfsMapper.addFile(fastdfsFile);
        String[] filePart = fileParts.split(";");

        if (file.length() <= num) {
            uri = uploadSmallFileUri + filePart[1].replace("[", "")
                    .replace("]", "").replace(" ", "")
                    .replace(",", "/") + "?attname=" + requestParser.getOriginalFilename();
        } else {
            uri = uploadBigFileUri + fastdfsFile.getId();
        }

        // 上传成功，删除应用服务器文件
        DirectoryOperation.deleteDirectoryOperation(new File(uploadDir + "/" + requestParser.getUuid()));
        return uri;
    }

    /**
     * 从文件服务器下载到应用服务器
     * @param downloadDir
     * @param parts
     * @return
     * @throws IOException
     * @throws MyException
     */
    public String fastdfsDownloadFile(String downloadDir, String ...parts) throws IOException, MyException {

        File file = new File(downloadDir);

        if (!file.exists()) {
            file.createNewFile();
        }

        FastdfsPool fastdfsPool = FastdfsPool.getFastdfs();
        FileOutputStream out = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(out);
        //  从文件服务器上下载文件到应用服务器
        for (int i = 1; i < parts.length; i++) {
            StorageClient1 storageClient1 = fastdfsPool.checkout(10);
            String[] part = parts[i].split(",");
            byte[] file_buff = storageClient1.download_file(part[0].substring(1).trim(), part[1].substring(0, part[1].length() - 1).trim());
            fastdfsPool.checkin(storageClient1);
            bos.write(file_buff);
        }
        out.close();
        bos.flush();
        bos.close();

        System.out.println("文件已经从文件服务器下载到应用服务器");

        return SUCCESS;
    }

    /**
     * 删除文件系统中的文件
     * @param parts
     * @throws IOException
     * @throws MyException
     */
    public void fastdfsDeleteFile(String ...parts) throws IOException, MyException {

        FastdfsPool fastdfsPool = FastdfsPool.getFastdfs();

        for (int i = 1; i < parts.length; i++) {
            StorageClient1 storageClient1 = fastdfsPool.checkout(10);
            String[] part = parts[i].split(",");
            int j = storageClient1.delete_file(part[0].substring(1).trim(), part[1].substring(0, part[1].length() - 1).trim());
            fastdfsPool.checkin(storageClient1);
        }
    }
}
