package com.resume.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果类
 */
@Data
@NoArgsConstructor
public class Result<T> {
    /**
     * 响应码：200-成功，其他-失败
     */
    private Integer code;
    /**
     * 响应消息
     */
    private String msg;
    /**
     * 响应数据
     */
    private T data;

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "成功", data);
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "成功", null);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    /**
     * 失败响应（带数据）
     */
    public static <T> Result<T> errorWithData(String msg, T data) {
        return new Result<>(500, msg, data);
    }

    /**
     * 失败响应（带数据）
     */
    public static <T> Result<T> error(String message, T data) {
        return new Result<>(0, message, data);
    }
}