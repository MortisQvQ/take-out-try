package com.example.takeouttry.DTO;

import lombok.Data;

@Data
public class Result<T> {

    private boolean success;
    private T data;
    private String message;
    private Integer code;

    // 成功 - 只带数据
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setData(data);
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    // 成功 - 带自定义消息
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = success(data);
        result.setMessage(message);
        return result;
    }

    // 失败 - 带消息
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(message);
        result.setCode(400);
        return result;
    }

    // 失败 - 带自定义码
    public static <T> Result<T> error(String message, Integer code) {
        Result<T> result = error(message);
        result.setCode(code);
        return result;
    }
}