package com.etammag.icommon.context;


import com.etammag.icommon.entity.BaseInfo;

public class BaseInfoContext {
    private static final ThreadLocal<BaseInfo> threadLocal = new ThreadLocal<>();

    public static <T extends BaseInfo> void set(T baseInfo){
        threadLocal.set(baseInfo);
    }

    public static BaseInfo get(){
        return threadLocal.get();
    }
}
