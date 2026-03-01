package com.ebookstore.ebookstorebackend.utils;


import com.ebookstore.ebookstorebackend.dto.WebSocketMessageDTO;
import com.ebookstore.ebookstorebackend.service.OrderService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {
    private final OrderService orderService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderListener(OrderService orderService, KafkaTemplate<String, String> kafkaTemplate) {
        this.orderService = orderService;
        this.kafkaTemplate = kafkaTemplate;
    }

    // 监听订单创建事件
    @KafkaListener(topics = "order_submit", groupId = "order_group")
    public void submitListener(ConsumerRecord<String, String> record){
        String key = record.key();
        String value = record.value();
        System.out.println("Received message: key=" + key + ", value=" + value);


        if (value == null || value.isBlank()) {
            System.out.println("消息内容为空，跳过处理");
            return;
        }

        String [] parts = value.split(",");

        if (parts.length != 3) {
            System.out.println("Invalid message format: " + value);
            kafkaTemplate.send("order_result", key, "FAILED:消息格式错误");
            return;
        }

        // 提取字段
        try{
            Long userId = Long.parseLong(parts[0]);
            String shippingAddress = parts[1];
            String contactPhone = parts[2];

            // 调用订单服务创建订单
            try {
                orderService.createOrderFromCart(userId, shippingAddress, contactPhone);
                System.out.println("订单已经成功创建！准备sending给下一个topic。userId: " + userId);
                kafkaTemplate.send("order_result", key, "SUCCESS:订单创建成功");
            } catch (Exception e) {
                kafkaTemplate.send("order_result", key, "FAILED:订单创建失败 - " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid userId format: " + parts[0]);
            kafkaTemplate.send("order_result", key, "FAILED:用户ID格式错误");
        }
    }

    @KafkaListener(topics = "order_result", groupId = "order_group")
    public void resultListener(ConsumerRecord<String, String> record){
        String key = record.key();
        String value = record.value();
        System.out.println("收到order_submit的结果，参数为: key=" + key + ", value=" + value);
        
        try {
            // 解析结果并构建消息
            WebSocketMessageDTO message;
            if (value.startsWith("SUCCESS:")) {
                String msg = value.substring(8); // 去掉 "SUCCESS:" 前缀
                message = WebSocketMessageDTO.orderSuccess(msg);
            } else if (value.startsWith("FAILED:")) {
                String msg = value.substring(7); // 去掉 "FAILED:" 前缀
                message = WebSocketMessageDTO.orderFailed(msg);
            } else {
                message = WebSocketMessageDTO.info("ORDER_RESULT", value);
            }
            
            // 发送给指定用户
            OrderWebSocketServer.sendToUser(key, message);
            System.out.println("已通过WebSocket发送消息给用户: " + key);
        } catch (Exception e) {
            System.out.println("WebSocket推送失败: " + e.getMessage());
        }
        System.out.println("订单处理结果: key=" + key + ", result=" + value);
    }

}
