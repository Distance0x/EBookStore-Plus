package com.ebookstore.ebookstorebackend.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class MicroserviceClient {
    
    private final WebClient webClient;
    
    @Autowired
    public MicroserviceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public BigDecimal calculatePrice(BigDecimal price, Integer quantity) {
        try {
            System.out.println("调用 price-service 计算价格，单价: " + price + "，数量: " + quantity);
            
            Map<String, Object> request = new HashMap<>();
            request.put("price", price);
            request.put("quantity", quantity);
            
            Map<String, Object> response = webClient.post()
                .uri("lb://price-service/calculateTotal")  // ✅ 函数名路径
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofSeconds(3))
                .block();
            
            if (response != null && response.containsKey("total")) {
                BigDecimal total = new BigDecimal(response.get("total").toString());
                System.out.println("计算成功，总价: " + total);
                return total;
            }
            return price.multiply(BigDecimal.valueOf(quantity));
            
        } catch (Exception e) {
            System.err.println("调用 price-service 失败，使用本地计算: " + e.getMessage());
            return price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}