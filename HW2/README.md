# 应用系统体系架构 — 作业2

> Spring 事务传播机制 + Kafka 异步处理 + WebSocket 实时通知实验

---

# 📌 一、事务传播机制实验

## 🎯 目标

验证：

* `Propagation.REQUIRED`
* `Propagation.REQUIRES_NEW`

在不同异常场景下的行为差异。

---

# 1️⃣ 核心事务代码

## Service 层作为事务边界

```java
@Transactional(propagation = Propagation.REQUIRED)
public void createOrderFromCart(Long userId, String shippingAddress, String contactPhone) {
    orderDao.save(order);

    for (CartItem cartItem : cartItems) {
        orderItemDao.save(orderItem);
    }
}
```

📌 设计原因（见第1页）：

* 有外键约束的表必须在同一事务中操作
* 保证数据一致性
* 避免死锁

---

## DAO 层使用 REQUIRES_NEW

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public OrderItem save(OrderItem orderItem) {
    return orderItemRepository.save(orderItem);
}
```

---

# 2️⃣ 事务行为实验总结

根据 PPT 实验（第2页）

| 场景 | 异常位置           | 事务类型         | 结果         |
| -- | -------------- | ------------ | ---------- |
| 1  | 无异常            | REQUIRED     | 全部成功       |
| 2  | Service异常      | REQUIRED     | 全部回滚       |
| 3  | OrderDao异常     | REQUIRED     | 全部回滚       |
| 4  | OrderItem异常    | REQUIRED     | 全部回滚       |
| 5  | OrderItem异常    | REQUIRES_NEW | 订单成功，订单项失败 |
| 6  | OrderItem之前异常  | REQUIRES_NEW | 全部回滚       |
| 7  | OrderItem之后异常  | REQUIRES_NEW | 前两项回滚      |
| 8  | OrderDao异常     | REQUIRES_NEW | 全部回滚       |
| 9  | OrderItemDao异常 | REQUIRES_NEW | 订单成功，订单项回滚 |

---

# 🚨 二、死锁分析（核心）

PDF 第4页详细说明（事务图示）

### 事务流：

```
事务1 (createOrderFromCart)
 ├─ INSERT orders (持有排它锁)
 └─ 调用 orderItemDao.save()

事务2 (REQUIRES_NEW)
 ├─ INSERT order_items
 └─ 需要检查外键 → SELECT orders
     └─ 需要共享锁 → 被事务1阻塞
```

### 结果：

```
Lock wait timeout exceeded; try restarting transaction
```

📌 原因：

* 外键检查需要读锁
* 父事务持有写锁
* 新事务等待
* 父事务等待子事务返回
* 死锁

---

# 📌 三、Kafka + WebSocket 架构设计

PDF 第9页给出总体方案 

---

## 架构流程

```
前端下单
   ↓
Kafka topic1: order_submit
   ↓
OrderListener
   ↓
调用 OrderService
   ↓
Kafka topic2: order_result
   ↓
WebSocket 推送
   ↓
前端弹窗提示
```

---

# 1️⃣ Kafka 监听代码

## topic1 — 提交订单

```java
@KafkaListener(topics = "order_submit")
public void submitListener(ConsumerRecord<String, String> record) {

    Long userId = Long.parseLong(parts[0]);

    try {
        orderService.createOrderFromCart(userId, shippingAddress, contactPhone);
        kafkaTemplate.send("order_result", key, "SUCCESS:订单创建成功");
    } catch (Exception e) {
        kafkaTemplate.send("order_result", key, "FAILED:订单创建失败");
    }
}
```

---

## topic2 — 推送结果

```java
@KafkaListener(topics = "order_result")
public void resultListener(ConsumerRecord<String, String> record) {
    orderWebSocketServer.sendToUser(record.key(), record.value());
}
```

---

# 2️⃣ WebSocket 服务端

PDF 第10-12页 

## 配置

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderWebSocketServer, "/ws/order")
                .setAllowedOrigins("*");
    }
}
```

---

## WebSocket Server

```java
@Component
public class OrderWebSocketServer extends TextWebSocketHandler {

    private static ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> userToSession = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public static void sendToUser(String userId, Object message) {
        String sessionId = userToSession.get(userId);
        sendToSession(sessionId, message);
    }
}
```

---

# 3️⃣ 前端 WebSocket

PDF 第14页 

```javascript
const websocket = new WebSocket(
  `ws://localhost:8080/ws/order?userId=${UserID}`
)

websocket.onmessage = (event) => {
  if (event.data.match('SUCCESS:')) {
      alert("订单创建成功")
  }
  if (event.data.match('FAILED:')) {
      alert("订单创建失败")
  }
}
```

---

# 📊 四、三种方案对比

| 方案        | 优点     | 缺点        |
| --------- | ------ | --------- |
| JS监听Topic | 实现简单   | 刷新页面，安全性差 |
| Ajax轮询    | 无需刷新页面 | 非实时，浪费资源  |
| WebSocket | 实时，低延迟 | 实现复杂      |

📌 最终采用 WebSocket（见第9页）

---

# 🧠 五、核心知识点总结

## ✅ 事务传播机制

* REQUIRED：加入当前事务
* REQUIRES_NEW：开启新事务
* 外键检查可能导致锁冲突
* 事务嵌套需谨慎

---

## ✅ 消息中间件设计思想

* 解耦系统
* 异步削峰
* 提高吞吐量

---

## ✅ WebSocket 优势

* 服务端主动推送
* 降低延迟
* 更好用户体验

---


# 🏁 总结

本次作业实现了：

✔ Spring 事务传播机制实验
✔ 外键与锁冲突分析
✔ Kafka 异步订单处理
✔ WebSocket 实时通知
✔ 完整前后端联调

体现：

* 分层架构设计
* 事务边界控制
* 分布式消息架构
* 实时通信机制
