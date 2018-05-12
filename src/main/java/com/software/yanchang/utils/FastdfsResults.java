package com.software.yanchang.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ccoke
 * @Description:
 * @Date: Created in 15:23 2018/3/29
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FastdfsResults {
    private Boolean success;
    private String error;
    private Boolean reset;
    private Result result;
    public FastdfsResults(String error, Boolean reset) {
        this.error = error;
        this.reset = reset;
    }

    public FastdfsResults(Boolean success, String name, String uri) {
        this.success = success;
        this.result = new Result(name, uri);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class Result {
        private String name;
        private String uri;
    }
}
