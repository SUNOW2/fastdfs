package com.software.yanchang.dao;

import com.software.yanchang.domain.FastdfsFile;
import org.springframework.stereotype.Component;

@Component
public interface FastdfsMapper {

    /**
     * 根据id查询文件块
     * @param id
     * @return
     */
    FastdfsFile getFileParts(String id);

    /**
     * 添加文件记录
     * @param fastdfsFile
     */
    void addFile(FastdfsFile fastdfsFile);

    /**
     * 删除数据库内文件记录
     * @param fastdfsFile
     */
    void deleteFile(FastdfsFile fastdfsFile);

    /**
     * 生成主键，不采用id自增的原因：自增的主键存在主键值重复的情况
     * @return
     */
    String createId();
}
