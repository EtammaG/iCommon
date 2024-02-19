package com.etammag.icommon.utils.redis;

import java.time.Duration;

public interface ILock {

    boolean tryLock(String lockName, Duration duration);

    void unlock(String lockName);

}