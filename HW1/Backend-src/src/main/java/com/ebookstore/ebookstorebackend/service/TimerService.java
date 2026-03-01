package com.ebookstore.ebookstorebackend.service;

import java.time.Duration;

public interface TimerService {
    void start();
    Duration stop();           // 停止并返回本次会话持续时间
    boolean isRunning();
    void reset();
}
