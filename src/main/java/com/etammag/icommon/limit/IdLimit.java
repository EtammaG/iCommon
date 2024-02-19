package com.etammag.icommon.limit;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdLimit {

    @AliasFor("timesInAUnit")
    int value() default 0;

    @AliasFor("value")
    int timesInAUnit() default 0;

    TimeUnit unit() default TimeUnit.MINUTES;

}
