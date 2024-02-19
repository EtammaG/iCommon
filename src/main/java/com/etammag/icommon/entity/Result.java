package com.etammag.icommon.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Result<T> {

    private Integer code;

    private String msg;

    private T data;

    private Map<String, Object> map;

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.map = new HashMap<>();
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(400, msg, null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    public Result<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }
}