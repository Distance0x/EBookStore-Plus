# 应用系统体系架构 — 作业3

> WebSocket 精准推送 + 线程安全设计 + 数据库恢复机制分析

---

# 📌 一、WebSocket 消息结构升级

为实现更规范的前后端通信，引入 DTO：

```java
public class WebSocketMessageDTO {

    // 业务类型：用于前端筛选
    private String type;

    // SUCCESS / FAILED / INFO / WARNING / SYSTEM
    private String status;

    // 消息内容
    private String message;

    // 时间戳
    private Long timestamp;
}
```

## 🎯 设计思想

* `type` → 定位业务场景（如 ORDER_RESULT）
* `status` → 表示状态
* `timestamp` → 保证时间一致性

实现关注点分离（Separation of Concern）

---

# 📌 二、客户端筛选机制设计


---

## 1️⃣ 服务端连接管理

使用两个 ConcurrentHashMap：

```java
// sessionId -> session
ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

// userId -> sessionId
ConcurrentHashMap<String, String> userToSession = new ConcurrentHashMap<>();
```

### 作用：

* sessions：用于实际发送消息
* userToSession：实现按用户精准推送

---

## 2️⃣ 用户识别机制

前端连接时携带 userId：

```javascript
const ws = new WebSocket(
  `ws://localhost:8080/ws/order?userId=${userId}`
)
```

服务端解析：

```java
private String extractUserId(WebSocketSession session) {
    String query = session.getUri().getQuery();
    if (query.startsWith("userId=")) {
        return query.substring("userId=".length());
    }
}
```

建立映射：

```java
@Override
public void afterConnectionEstablished(WebSocketSession session) {
    sessions.put(session.getId(), session);

    String userId = extractUserId(session);
    if (userId != null) {
        userToSession.put(userId, session.getId());
    }
}
```

---

## 3️⃣ 精准点对点推送

```java
public static void sendToUser(String userId, Object message) {
    String sessionId = userToSession.get(userId);
    sendToSession(sessionId, message);
}
```

---

## 4️⃣ 前端筛选消息


```javascript
websocket.onmessage = (event) => {
    const msg = JSON.parse(event.data)

    if (msg.type === 'ORDER_RESULT') {

        if (msg.status === 'SUCCESS') {
            console.log('订单创建成功')
        }
        else if (msg.status === 'FAILED') {
            console.log('订单创建失败')
        }
    }
}
```

---

# 📌 三、为什么使用 ConcurrentHashMap？


WebSocket 场景存在多线程访问：

* 多客户端同时连接
* I/O线程
* 消息广播线程

若使用 HashMap：

* 可能导致红黑树结构损坏
* 数据丢失
* 并发写冲突

---

## 🔒 ConcurrentHashMap 线程安全机制

### 1️⃣ CAS 无锁操作

* put/get 使用 CPU 原子指令
* 减少锁竞争

### 2️⃣ 桶级别 synchronized

* 仅在哈希冲突时锁定桶
* 不影响其他桶

实现高并发 + 线程安全

---

# 📌 四、数据库恢复策略分析


---

## i️⃣ 事务执行过程中持续落盘（STEAL）

### 问题：

* 未提交数据已落盘
* 可能产生脏读
* 事务原子性被破坏
* 可能导致不可重复读

### 解决方案：

* Undo 日志
* 记录修改前数据
* 出现异常时回滚
* 加锁控制并发

---

## ii️⃣ 事务提交后再落盘（NO-FORCE）

### 问题：

* 已提交但未落盘
* 系统崩溃 → 数据丢失
* 违反持久性

### 解决方案：

* Redo 日志
* 提交前强制写日志
* 崩溃后重做
* 使用锁控制并发

---

# 📊 五、整体架构总结

本次作业结合：

* WebSocket 精准推送
* Kafka 异步消息
* 线程安全设计
* 数据库恢复理论

构成一个完整的：

> 实时消息驱动订单系统

---

# 🧠 六、核心知识点汇总

## ✅ WebSocket设计升级

* DTO封装
* 业务与状态分离
* 时间一致性

## ✅ 并发安全设计

* ConcurrentHashMap
* CAS
* 桶级锁

## ✅ 数据库恢复理论

* STEAL + Undo
* NO-FORCE + Redo
* ACID 保证



