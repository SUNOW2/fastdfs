package com.software.yanchang.controller;

import com.software.yanchang.dao.FastdfsMapper;
import com.software.yanchang.domain.FastdfsFile;
import com.software.yanchang.service.FineUploaderService;
import com.software.yanchang.service.StorageService;
import com.software.yanchang.utils.FastdfsOperation;
import com.software.yanchang.utils.FastdfsResults;
import lombok.extern.slf4j.Slf4j;
import org.csource.common.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/fastdfs")
public class FastdfsController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private FineUploaderService fineUploaderService;

    @Autowired
    private FastdfsOperation fastdfsOperation;

    @Value("${breakpoint.upload.dir1}")
    private String path;

    @Value("${server.mysql.ip}")
    private String mysqlIp;

    @Autowired
    private FastdfsMapper fastdfsMapper;

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
//        fastdfsFile.setFastdfsDate(new Date());
//        log.trace("日志输出 trace");
//        log.debug("日志输出 debug");
//        log.info("日志输出 info");
//        fastdfsMapper.addFile(fastdfsFile);
//    }

//    @RequestMapping(value = "/test", method = {RequestMethod.GET, RequestMethod.POST})
//    public ResponseEntity<FastdfsFile> test()  {
//        FastdfsFile fastdfsFile = new FastdfsFile();
//        fastdfsFile.setId("test");
//        fastdfsFile.setFileParts("112233445566");
//        log.info("path=" + path);
//        return new ResponseEntity("ok", 200, "成功", fastdfsFile);
//    }

    @RequestMapping(value = "/test", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public FastdfsResults test(@RequestParam(value = "list") List<String> list) throws IOException, MyException {
//        测试springboot上传List
        /**
         * 上传数组 ( @RequestParam(value = "list") String[] list )
         * 上传List（ @RequestParam(value = "list") List<String> list ）
         * postman采用x-www-form-urlencoded格式
         */
        System.out.println("List测试");
        for(String str : list) {
            System.out.println("str=" + str);
        }

        List testList = new ArrayList();
        testList.add("1");
        testList.add("2");
        System.out.println("testList=" + testList);

        int[] arr = {1, 2, 3};
        System.out.println("arr=" + Arrays.toString(arr));

//        测试docker中的fastdfs连接
//        String extName = "test.txt";
//        FileInputStream inputStream = new FileInputStream(new File("/Users/sunow/study/code/work/延长科技/fastdfs整合/src/main/java/com/software/yanchang/conf/CorsFilter.java"));
//        NameValuePair[] meta_list = new NameValuePair[1];
//        meta_list[0] = new NameValuePair("fileName", "a.txt");
//        int num = 1024 * 1024;
//        byte[] file_buff = new byte[num];
//
//        inputStream.read(file_buff);
//        System.out.println("this is testing fastDFS");
//
//        FastdfsPool fastdfsPool = FastdfsPool.getFastdfs();
//        System.out.println("fast");
//        StorageClient1 storageClient1 = fastdfsPool.checkout(10);
//        System.out.println("hhhhh");
//        String[] upload_file = storageClient1.upload_file(file_buff, extName, meta_list);
//        fastdfsPool.checkin(storageClient1);
//
//        System.out.println("fileName=" + Arrays.asList(upload_file));


//        测试docker中的mysql连接
//        System.out.println("这是测试docker的mysql");
//        System.out.println("数据库地址" + mysqlIp);
//        fastdfsMapper.addFile(new FastdfsFile("22","333", new Date()));
//        System.out.println("测试成功");
        return new FastdfsResults();
    }
}
