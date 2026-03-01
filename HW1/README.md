# 应用系统体系架构 — 作业1

> 基于 Spring Boot + Kafka + 数据库 的简单应用系统架构实验
> 涉及 Session 作用域管理、异步消息处理、订单落库


# 📌 一、Spring Bean 作用域设计

## 1️⃣ TimerService 使用 Session 作用域

```java
@Service
@Scope("session")
public class TimerService {

    private long startTime;

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public long stop() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
}
```

### ✅ 设计原因

* 每个用户（浏览器）需要独立计时
* 不同 Session 之间不能共享计时状态
* 每个 Session 维护自己的 Duration

---

## 2️⃣ Controller 使用 Singleton（默认）

```java
@RestController
@RequestMapping("/timer")
public class TimerController {

    @Autowired
    private TimerService timerService;

    @GetMapping("/start")
    public String start() {
        timerService.start();
        return "Started";
    }

    @GetMapping("/stop")
    public long stop() {
        return timerService.stop();
    }
}
```

### ✅ 设计原因

* Controller 不保存状态
* 请求量较小
* Singleton 可减少资源开销

---

# 🧪 二、多浏览器 Session 实验

## 实验方法

* 使用 **Chrome** 和 **Edge**
* 间隔 1 秒分别登录
* 再分别登出

## 实验结果

后端输出：

```
User1 Duration: 8 seconds
User2 Duration: 11 seconds
```

前端使用：

```javascript
alert("Duration: " + result);
```

### ✅ 说明

* 每个浏览器有独立 Session
* TimerService 成功实现 Session 隔离
* 计时结果互不影响

---

# 🚀 三、Kafka 消息中间件处理订单

本实验实现：

> 前端下单 → Kafka缓冲 → 后端监听 → 处理订单 → 写数据库 → 返回结果

---

## 🧱 系统架构流程

```
前端请求
    ↓
Controller (/create/kafka)
    ↓
Kafka Topic1: submit
    ↓
OrderListener
    ↓
Kafka Topic2: result
    ↓
结果监听输出
```

---

## 1️⃣ Controller 接收订单请求

```java
@PostMapping("/create/kafka")
public ResponseEntity<String> createOrder(@RequestBody Order order) {
    kafkaTemplate.send("submit", order.getUserId(), order);
    return ResponseEntity.ok("ok");
}
```

说明：

* 接收前端请求
* 发送消息到 Topic: `submit`
* 立即返回 OK（异步）

---

## 2️⃣ OrderListener 监听 submit

```java
@KafkaListener(topics = "submit")
public void listenSubmit(ConsumerRecord<String, Order> record) {
    Order order = record.value();
    orderService.save(order);
    kafkaTemplate.send("result", record.key(), "Order Created");
}
```

说明：

* 监听 Topic1
* 创建订单
* 写入数据库
* 发送到 Topic2

---

## 3️⃣ 监听 result 输出结果

```java
@KafkaListener(topics = "result")
public void listenResult(ConsumerRecord<String, String> record) {
    System.out.println("User: " + record.key() + 
                       " Result: " + record.value());
}
```

输出示例：

```
User: 1001 Result: Order Created
```

📌 Key 为 UserID

---

# 💾 四、订单写数据库

```java
@Service
public class OrderService {

    @Autowired
    private OrderRepository repository;

    public void save(Order order) {
        repository.save(order);
    }
}
```

订单在：

* Kafka 异步消费时
* 由 OrderService 写入数据库

---

# 🧰 五、技术栈总结

| 技术           | 用途          |
| ------------ | ----------- |
| Spring Boot  | Web 框架      |
| Spring Scope | Bean 生命周期管理 |
| Kafka        | 异步消息队列      |
| Spring Kafka | Kafka 集成    |
| JPA / MySQL  | 数据库存储       |
| REST API     | 前后端通信       |

---

# 📊 六、架构优势分析

## 1️⃣ Session Scope 优势

* 用户隔离
* 状态独立
* 符合 Web 会话模型

## 2️⃣ Kafka 异步优势

* 解耦前后端
* 提高吞吐
* 支持缓冲削峰
* 可扩展为分布式架构

---

# 🎯 七、实验总结

本次作业实现了：

* ✅ Session 级别计时器
* ✅ 多浏览器隔离验证
* ✅ Kafka 双 Topic 异步订单处理
* ✅ 订单数据库持久化
* ✅ 前后端交互展示

体现了：

* Bean 生命周期管理
* 异步架构设计思想
* 消息中间件解耦
* 分层结构设计

