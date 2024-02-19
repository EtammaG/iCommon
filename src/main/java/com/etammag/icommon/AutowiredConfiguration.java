//package com.etammag.istarter;
//
//import com.etammag.istarter.exception.FilterExceptionFilter;
//import com.etammag.istarter.limit.IdLimitAspect;
//import com.etammag.istarter.limit.IpLimitAspect;
//import com.etammag.istarter.security.JwtAuthTokenFilter;
//import com.etammag.istarter.utils.redis.CacheUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.core.StringRedisTemplate;
//
//@Configuration
//@EnableConfigurationProperties(Properties.class)
//public class AutowiredConfiguration {
//
//    private final StringRedisTemplate stringRedisTemplate;
//
//    @Autowired
//    public AutowiredConfiguration(StringRedisTemplate stringRedisTemplate) {
//        this.stringRedisTemplate = stringRedisTemplate;
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public FilterExceptionFilter filterExceptionFilter(Properties properties) {
//        return new FilterExceptionFilter();
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public JwtAuthTokenFilter jwtAuthTokenFilter(Properties properties) {
//        return new JwtAuthTokenFilter(stringRedisTemplate, properties.getSign().getDuration());
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public CacheUtil cacheUtil(Properties properties) {
//        return new CacheUtil(stringRedisTemplate);
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public IdLimitAspect idLimitAspect(Properties properties) {
//        return new IdLimitAspect(stringRedisTemplate, properties.getRedisKey().getIdLimit());
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public IpLimitAspect ipLimitAspect(Properties properties) {
//        return new IpLimitAspect(stringRedisTemplate, properties.getRedisKey().getIpLimit());
//    }
//
//}