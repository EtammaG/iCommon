package com.etammag.icommon.exception;

import com.etammag.icommon.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
public class SimpleGlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public Result<String> handleUnknownException(Throwable e) {
        log.warn("Unknown Exception:", e);
        return Result.error("未知错误");
    }

    @ExceptionHandler(CustomException.class)
    public Result<String> handleCustomException(CustomException e) {
        return Result.fail(e.getMessage());
    }

}
