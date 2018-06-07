package com.software.yanchang.controller;

import com.software.yanchang.domain.FastdfsFile;
import com.software.yanchang.service.FineUploaderService;
import com.software.yanchang.service.StorageService;
import com.software.yanchang.utils.FastdfsResults;
import com.software.yanchang.utils.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.csource.common.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping(value = "/fastdfs")
public class FastdfsController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private FineUploaderService fineUploaderService;

    @Value("${breakpoint.upload.dir}")
    private String path;

//    @Autowired
//    private FastdfsMapper fastdfsMapper;

    /**
     * 将文件服务器上的文件下载到应用服务器上
     * @param fastdfsFile
     * @param request
     * @param response
     * @throws IOException
     * @throws MyException
     */
    @RequestMapping(value = "/applicationToFile", method = RequestMethod.GET)
    public void applicationToFile(FastdfsFile fastdfsFile, HttpServletRequest request, HttpServletResponse response) throws IOException, MyException {
        storageService.applicationToFile(fastdfsFile.getId(), request, response);
    }

    /**
     * 上传文件至FastDFS文件系统
     * @param req
     * @param resp
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/uploadFile", method = {RequestMethod.POST, RequestMethod.GET})
    public FastdfsResults uploadFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return fineUploaderService.uploadFile(req, resp);
    }

    /**
     * 删除数据库文件记录以及文件系统中的文件
     * @param fastdfsFile
     * @throws IOException
     * @throws MyException
     */
    @RequestMapping(value = "/deleteFile", method = {RequestMethod.GET, RequestMethod.POST})
    public void deleteFile(FastdfsFile fastdfsFile) throws IOException, MyException {
        storageService.deleteFile(fastdfsFile);
    }

//    /**
//     * 新增文件，测试用
//     * @param fastdfsFile
//     */
//    @RequestMapping(value = "/addFile", method = {RequestMethod.GET, RequestMethod.POST})
//    public void addFile(FastdfsFile fastdfsFile) {
//        fastdfsFile.setId(fastdfsMapper.createId());
//        log.trace("日志输出 trace");
//        log.debug("日志输出 debug");
//        log.info("日志输出 info");
//        fastdfsMapper.addFile(fastdfsFile);
//    }

    @RequestMapping(value = "/test", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<FastdfsFile> test()  {
        FastdfsFile fastdfsFile = new FastdfsFile();
        fastdfsFile.setId("test");
        fastdfsFile.setFileParts("112233445566");
        log.info("path=" + path);
        return new ResponseEntity("ok", 200, "成功", fastdfsFile);
    }
}
