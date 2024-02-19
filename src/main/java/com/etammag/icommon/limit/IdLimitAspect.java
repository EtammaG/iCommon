package com.etammag.icommon.limit;

import com.etammag.icommon.context.BaseInfoContext;
import com.etammag.icommon.entity.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@Aspect
public class IdLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;
    private final String redisKey;

    public IdLimitAspect(StringRedisTemplate stringRedisTemplate, String redisKey) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisKey = redisKey;
    }

    @Pointcut("@annotation(idLimit)")
    public void excludeService(IdLimit idLimit) {
    }

    @Around("excludeService(idLimit)")
    public Object doAround(ProceedingJoinPoint pjp, IdLimit idLimit) throws Throwable {
        String key = redisKey + BaseInfoContext.get().getId();
        String times = stringRedisTemplate.opsForValue().get(key);
        int t = times == null ? 1 : Integer.parseInt(times) + 1;
        if (t > (idLimit.timesInAUnit() == 0 ? idLimit.value() : idLimit.timesInAUnit()))
            return Result.error("请求次数过多，请稍后再试");
        stringRedisTemplate.opsForValue().set(key, Integer.toString(t), 1, idLimit.unit());
        return pjp.proceed();
    }

}