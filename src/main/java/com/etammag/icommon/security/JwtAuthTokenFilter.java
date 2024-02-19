package com.etammag.icommon.security;

import com.alibaba.fastjson.JSON;
import com.etammag.icommon.context.BaseInfoContext;
import com.etammag.icommon.entity.IUserDetails;
import com.etammag.icommon.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.function.BiFunction;

@Slf4j
public class JwtAuthTokenFilter extends OncePerRequestFilter {

    private final StringRedisTemplate stringRedisTemplate;

    private final Duration signinDuration;

    private final BiFunction<HttpServletRequest, HttpServletResponse, String> tokenExtractor;

    public JwtAuthTokenFilter(StringRedisTemplate stringRedisTemplate, Duration signinDuration, BiFunction<HttpServletRequest, HttpServletResponse, String> tokenExtractor) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.signinDuration = signinDuration;
        this.tokenExtractor = tokenExtractor;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = tokenExtractor.apply(request, response);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        String redisKey;
        try {
            redisKey = JwtUtil.parseJWT(token).getBody().getSubject();
        } catch (JwtException e) {
            log.warn("illegal token");
            filterChain.doFilter(request, response);
            return;
        }
        IUserDetails userDetails = JSON.parseObject(stringRedisTemplate.opsForValue().get(redisKey), IUserDetails.class);
        if (userDetails == null) {
            log.warn("illegal token");
            filterChain.doFilter(request, response);
            return;
        }

        stringRedisTemplate.expire(redisKey, signinDuration);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        BaseInfoContext.set(userDetails.getBaseInfo());
        filterChain.doFilter(request, response);
    }

}
