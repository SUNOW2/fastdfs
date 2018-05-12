package com.software.yanchang.controller;

import com.software.yanchang.domain.FastdfsFile;
import com.software.yanchang.service.FineUploaderService;
import com.software.yanchang.service.StorageService;
import com.software.yanchang.utils.FastdfsResults;
import org.csource.common.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping(value = "/fastdfs")
public class FastdfsController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private FineUploaderService fineUploaderService;

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
}
