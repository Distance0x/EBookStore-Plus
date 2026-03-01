package com.ebookstore.ebookstorebackend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderWebSocketServer extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrderWebSocketServer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 存储所有的WebSocket会话 (sessionId -> session)
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // 用户ID -> sessionId
    private static final ConcurrentHashMap<String, String> userToSession = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        // 尝试解析 userId（允许前端使用 ws://.../ws/order?userId=123）
        String userId = extractUserId(session);
        if (userId != null && !userId.isBlank()) {
            // 如果同一用户已有旧连接，移除旧的映射（不强行关闭旧连接，保持最小侵入）
            String previous = userToSession.put(userId, session.getId());
            if (previous != null && !previous.equals(session.getId())) {
                logger.info("用户 {} 重新建立WebSocket连接，旧会话ID: {} -> 新会话ID: {}", userId, previous, session.getId());
            }
            logger.info("用户 {} 绑定会话ID: {}", userId, session.getId());
        }
        logger.info("WebSocket连接建立，会话ID: {}, 当前连接数: {}", session.getId(), sessions.size());
        session.sendMessage(new TextMessage("连接成功，等待订单更新..."));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("收到来自客户端的消息: {}", message.getPayload());

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        // 清理 userToSession 映射中的引用
        userToSession.entrySet().removeIf(e -> e.getValue().equals(session.getId()));
        logger.info("WebSocket连接关闭，会话ID: {}, 当前连接数: {}", session.getId(), sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket发生错误，会话ID: {}", session.getId(), exception);
        sessions.remove(session.getId());
        if (session.isOpen()) {
            session.close();
        }
    }

    /**
     * 广播消息给所有连接的客户端
     */
    public static void broadcast(Object message) {
        if (sessions.isEmpty()) {
            logger.debug("没有活跃的WebSocket连接，跳过广播");
            return;
        }

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            logger.info("广播消息给 {} 个客户端: {}", sessions.size(), jsonMessage);

            // 使用迭代器安全地遍历和移除失效会话
            sessions.entrySet().removeIf(entry -> {
                try {
                    if (entry.getValue().isOpen()) {
                        entry.getValue().sendMessage(new TextMessage(jsonMessage));
                        return false; // 发送成功，保留会话
                    } else {
                        return true; // 会话已关闭，移除会话
                    }
                } catch (Exception e) {
                    logger.warn("向会话 {} 发送消息失败，移除会话", entry.getKey(), e);
                    return true; // 发送失败，移除会话
                }
            });
        } catch (Exception e) {
            logger.error("序列化消息失败", e);
        }
    }

    /**
     * 向特定会话发送消息
     */
    public static void sendToSession(String sessionId, Object message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
                logger.info("向会话 {} 发送消息: {}", sessionId, jsonMessage);
            } catch (Exception e) {
                logger.error("向会话 {} 发送消息失败", sessionId, e);
                sessions.remove(sessionId);
            }
        }
    }

    /**
     * 获取当前连接数
     */
    public static int getConnectionCount() {
        return sessions.size();
    }

    /**
     * 新增：按用户ID发送（如果该用户在线且已建立映射）
     */
    public static void sendToUser(String userId, Object message) {
        if (userId == null) {
            logger.warn("sendToUser 调用时 userId 为空");
            return;
        }
        String sessionId = userToSession.get(userId);
        if (sessionId == null) {
            logger.debug("用户 {} 不在线，跳过发送", userId);
            return;
        }
        sendToSession(sessionId, message);
    }

    /**
     * 解析 URL 参数中的 userId
     */
    private String extractUserId(WebSocketSession session) {
        try {
            if (session.getUri() == null) return null;
            String query = session.getUri().getQuery();
            if (query == null || query.isBlank()) return null;
            // 简单解析：userId=xxx
            String[] parts = query.split("&");
            for (String p : parts) {
                if (p.startsWith("userId=")) {
                    return p.substring("userId=".length());
                }
            }
        } catch (Exception e) {
            logger.warn("解析 userId 失败", e);
        }
        return null;
    }
}