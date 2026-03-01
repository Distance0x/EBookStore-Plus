package com.ebookstore.ebookstorebackend.serviceimpl;

import com.ebookstore.ebookstorebackend.service.TimerService;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.Instant;


@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TimerServiceImpl implements TimerService {
    private Instant startTime;
    private Duration duration = Duration.ZERO;

    @Override
    public void start(){
        startTime = Instant.now();
        duration = Duration.ZERO;
    }

    @Override
    public Duration stop(){
        if (startTime == null) { // 未启动直接停止，返回 0
            return Duration.ZERO;
        }
        duration = Duration.between(startTime, Instant.now());
        startTime = null; // 停止后重置开始时间
        return duration;
    }

    @Override
    public boolean isRunning(){
        return startTime != null;
    }

    @Override
    public void reset(){
        startTime = null;
        duration = Duration.ZERO;
    }
}
