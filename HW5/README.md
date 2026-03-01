# 应用系统体系架构 — 作业5

> Eureka + Gateway + 微服务 + 函数式服务 实践

---

# 📌 一、整体架构设计

本次作业将系统拆分为多个微服务，并引入：

* Eureka 服务注册中心
* Spring Cloud Gateway 网关
* Author 查询微服务
* Price 函数式计算服务
* 主后端服务

---

## 🏗 系统结构

```text
Client
   ↓
Gateway (8080)
   ↓
 ├── ebookstore-backend (8081)
 ├── author-service (9001)
 └── price-service (9002)
          ↑
       Eureka (8040)
```

---

# 📌 二、Eureka 服务注册中心

## 🎯 作用

* 服务注册
* 服务发现
* 心跳检测
* 实例剔除
* 动态上下线

---

## 服务注册配置

```yaml
client:
  service-url:
    defaultZone: http://localhost:8040/eureka
instance:
  prefer-ip-address: true
  instance-id: ${spring.application.name}:${server.port}
```

---

## 心跳机制

```yaml
lease-renewal-interval-in-seconds: 10
lease-expiration-duration-in-seconds: 30
```

作用：

* 每 10 秒发送一次心跳
* 超过 30 秒未收到心跳 → 剔除实例

---

## 服务发现调用示例

```java
Map<String, Object> response = webClient.post()
    .uri("lb://price-service/calculateTotal")
    .bodyValue(request)
    .retrieve()
    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
    .block();
```

特点：

* 使用 `lb://` 通过服务名调用
* 不需要写死 IP

---

# 📌 三、Gateway 网关

## 🎯 作用

* 统一入口（8080）
* 路由转发
* 负载均衡
* 统一跨域配置
* 动态路由（基于 Eureka）

---

## 路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: ebookstore-backend
          uri: lb://ebookstore-backend
          predicates:
            - Path=/api/**

        - id: author-service
          uri: lb://author-service
          predicates:
            - Path=/author/**
          filters:
            - StripPrefix=1

        - id: price-service
          uri: lb://price-service
          predicates:
            - Path=/price/**
          filters:
            - StripPrefix=1
```

---

## 跨域统一配置

```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowed-origin-patterns: "*"
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
      allowed-headers: "*"
      allow-credentials: true
```

优势：

* 内部微服务无需单独配置 CORS
* 网关统一处理

---

# 📌 四、作者查询微服务（author-service）

## 调用方式

### 通过 Gateway

```bash
GET http://localhost:8080/author/api/authors/by-title?title=围城&exact=false
```

### 直接访问服务

```bash
GET http://localhost:9001/api/authors/by-title?title=围城&exact=false
```

---

## Controller 示例

```java
@GetMapping("/by-title")
public ResponseEntity<AuthorResponseDTO> getAuthorByTitle(
        @RequestParam String title,
        @RequestParam(defaultValue = "false") boolean exact) {

    AuthorResponseDTO response = bookService.getAuthorByTitle(title, exact);
    return ResponseEntity.ok(response);
}
```

---

# 📌 五、函数式服务（price-service）

## 🎯 设计思想：无状态服务

无状态的特点：

* 不依赖历史请求
* 不依赖 Session
* 不依赖缓存
* 不依赖数据库
* 相同输入 → 相同输出（幂等）

---

## 函数式定义

```java
@Bean
public Function<PriceRequest, PriceResponse> calculateTotal() {
    return request -> {

        if (request.getPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "价格不能为空");
        }

        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数量非法");
        }

        BigDecimal total = request.getPrice()
                .multiply(new BigDecimal(request.getQuantity()));

        return new PriceResponse(
                request.getPrice(),
                request.getQuantity(),
                total
        );
    };
}
```

---

## 通过 Gateway 调用

```bash
curl -X POST "http://localhost:8080/price/calculateTotal" \
-H "Content-Type: application/json" \
-d '{
  "price": 19.99,
  "quantity": 3
}'
```

返回：

```json
{
  "price": 19.99,
  "quantity": 3,
  "total": 59.97,
  "status": "success"
}
```

---

# 📌 六、主服务调用函数式服务

```java
@Service
public class MicroserviceClient {

    private final WebClient webClient;

    public BigDecimal calculatePrice(BigDecimal price, Integer quantity) {

        try {

            Map<String, Object> request = new HashMap<>();
            request.put("price", price);
            request.put("quantity", quantity);

            Map<String, Object> response = webClient.post()
                .uri("lb://price-service/calculateTotal")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofSeconds(3))
                .block();

            if (response != null && response.containsKey("total")) {
                return new BigDecimal(response.get("total").toString());
            }

            return price.multiply(BigDecimal.valueOf(quantity));

        } catch (Exception e) {

            // 失败降级：本地计算
            return price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
```

特点：

* 使用负载均衡调用
* 设置超时
* 提供降级策略

---

# 📌 七、函数式服务的扩展性优势

## 可水平扩展

例如：

* 同时接收 10,000 个订单
* 可以启动 100 个 price-service 实例
* Gateway 自动负载均衡

因为：

* 无状态
* 无会话依赖
* 无共享内存

非常适合云部署

---

# 📌 八、端口与服务名一览

| 服务      | 端口   | 服务名                |
| ------- | ---- | ------------------ |
| Eureka  | 8040 | eureka-service     |
| Gateway | 8080 | api-gateway        |
| 主后端     | 8081 | ebookstore-backend |
| 作者服务    | 9001 | author-service     |
| 价格函数服务  | 9002 | price-service      |
