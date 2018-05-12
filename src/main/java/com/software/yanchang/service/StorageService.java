package com.software.yanchang.service;

import com.software.yanchang.domain.FastdfsFile;
import org.csource.common.MyException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public interface StorageService {
    /**
     * 浏览器下载文件
     * @param srcPath
     * @param request
     * @param response
     */
    void downloadFileThrowBrowser(String srcPath, HttpServletRequest request, HttpServletResponse response) throws IOException, MyException;

    /**
     * 将文件从应用服务器下载到文件服务器
     * @param id
     * @return
     */
    void applicationToFile(Integer id, HttpServletRequest request, HttpServletResponse response) throws IOException, MyException;

    /**
     * 删除数据库中的记录
     * @param fastdfsFile
     */
    void deleteFile(FastdfsFile fastdfsFile) throws IOException, MyException;
}
