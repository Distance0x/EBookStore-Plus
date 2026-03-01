package com.ebookstore.ebookstorebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * WebSocket 统一消息格式
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessageDTO {
    
    /**
     * 消息类型，用于前端筛选处理
     */
    private String type;
    
    /**
     * 消息状态：SUCCESS / FAILED / INFO / WARNING / SYSTEMS
     */
    private String status;
    
    /**
     * 消息内容
     */
    private String message;
    
    /**
     * 时间戳
     */
    private Long timestamp;

    public WebSocketMessageDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    public WebSocketMessageDTO(String type, String status, String message) {
        this.type = type;
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public WebSocketMessageDTO(String type, String status, String message, Object data) {
        this.type = type;
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    // 快捷构造方法
    public static WebSocketMessageDTO orderSuccess(String message) {
        return new WebSocketMessageDTO("ORDER_RESULT", "SUCCESS", message);
    }

    public static WebSocketMessageDTO orderSuccess(String message, Object orderData) {
        return new WebSocketMessageDTO("ORDER_RESULT", "SUCCESS", message, orderData);
    }

    public static WebSocketMessageDTO orderFailed(String message) {
        return new WebSocketMessageDTO("ORDER_RESULT", "FAILED", message);
    }

    public static WebSocketMessageDTO info(String type, String message) {
        return new WebSocketMessageDTO(type, "INFO", message);
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
