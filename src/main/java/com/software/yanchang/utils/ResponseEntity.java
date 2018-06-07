package com.software.yanchang.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseEntity<T> extends BaseSerializable {
    // 状态：ok 成功，fail 失败
    private String result;

    // 状态码
    private Integer code;

    // 备注原因
    private String msg;

    // 返回数据
    private T data;
}
