package com.software.yanchang.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class FastdfsFile {
    /**
     * 文件编号
     */
    private String id;

    /**
     * 分片文件在文件服务器中的存储位置
     */
    private String fileParts;

    /**
     * 文件上传的时间
     */
    private Date fastdfsDate;
}
