package com.software.yanchang.service;

import com.software.yanchang.utils.FastdfsResults;
import com.software.yanchang.utils.RequestParser;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 注：静态方法和内部类未重构
 */
@Component
public interface FineUploaderService {
    void init() throws ServletException;

    void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException;

    void handleDeleteFileRequest(String uuid, HttpServletResponse resp) throws IOException;

    FastdfsResults uploadFile(HttpServletRequest req, HttpServletResponse resp) throws IOException;

    void writeFileForNonMultipartRequest(HttpServletRequest req, RequestParser requestParser) throws Exception;

    String writeFileForMultipartRequest(RequestParser requestParser) throws Exception;

    File mergeFiles(File outputFile, File partFile) throws IOException;

    File writeFile(InputStream in, File out, Long expectedFileSize) throws IOException;

    void writeResponse(PrintWriter writer, String failureReason, boolean isIframe, boolean restartChunking, RequestParser requestParser);
}
