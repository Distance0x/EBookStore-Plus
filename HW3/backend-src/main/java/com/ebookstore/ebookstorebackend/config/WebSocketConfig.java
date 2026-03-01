package com.ebookstore.ebookstorebackend.config;

import com.ebookstore.ebookstorebackend.utils.OrderWebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private OrderWebSocketServer orderWebSocketServer;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket端点，允许跨域
        registry.addHandler(orderWebSocketServer, "/ws/order")
                .setAllowedOrigins("*");
    }

}
