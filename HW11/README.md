# 应用系统体系架构 — 作业11

> Nginx 负载均衡策略实验 + GraphQL 查询接口实现

---

# 📌 一、实验环境

本次实验包含两个 Spring Boot 服务实例：

| 实例         | 端口   | 返回标识       |
| ---------- | ---- | ---------- |
| Server One | 8081 | Server One |
| Server Two | 8082 | Server Two |

Nginx 作为反向代理与负载均衡器：

```text
访问入口：http://localhost:8000/
```

---

# 📌 二、Nginx 负载均衡策略实验

---

# 1️⃣ Round Robin（轮询）

### 原理

Round Robin 是默认策略：

* 第一次请求 → Server 1
* 第二次请求 → Server 2
* 第三次请求 → Server 1
* 第四次请求 → Server 2

以此类推。

---

### 实验现象

多次访问：

```bash
curl http://localhost:8000
```

返回结果呈现：

```
Server One
Server Two
Server One
Server Two
```

说明：

* 请求被平均分配
* 不考虑服务器负载
* 不考虑客户端 IP

---

# 2️⃣ ip_hash 策略

### 原理

* 根据客户端 IP 计算 Hash
* Hash 决定请求固定落到某个服务器

---

### 实验现象

由于：

* 本地测试 IP 不变

多次访问：

```bash
curl http://localhost:8000
```

始终返回：

```
Server One
```

说明：

* 同一 IP 始终命中同一后端
* 实现“会话保持”

---

# 3️⃣ Least Connection（最少连接数）

### 原理

将请求分配给：

> 当前活动连接数最少的服务器

---

### 实验设计

为了模拟高负载：

在 Server One 中加入：

```java
Thread.sleep(10000);
```

让它每次请求延迟 10 秒。

---

### 实验步骤

窗口 A：

```bash
curl http://localhost:8000
```

（会卡住 10 秒）

窗口 B：

多次执行：

```bash
curl http://localhost:8000
```

---

### 实验结果

由于：

* Server One 有 1 个活跃连接
* Server Two 有 0 个连接

Nginx 会将窗口 B 的请求全部分配给：

```
Server Two
```

说明：

* Least Connection 会动态考虑当前负载
* 更适合长连接或高并发场景

---

# 4️⃣ Round Robin + 权重（3:1）

### 配置示例

```nginx
upstream backend {
    server localhost:8081 weight=3;
    server localhost:8082 weight=1;
}
```

---

### 实验现象

连续多次请求：

```
Server One
Server One
Server One
Server Two
```

比例接近：

```
3 : 1
```

例如实验中：

```
6 : 2
```

说明：

* 权重越高
* 分配请求越多
* 可用于硬件性能不同的服务器

---

# 📌 三、负载均衡策略对比总结

| 策略               | 特点     | 适用场景      |
| ---------------- | ------ | --------- |
| Round Robin      | 平均分配   | 普通无状态服务   |
| ip_hash          | 固定用户绑定 | 需要会话保持    |
| Least Connection | 动态负载   | 长连接 / 高并发 |
| 权重轮询             | 按性能分配  | 服务器配置不同   |

---

# 📌 四、GraphQL 集成

本次实验还引入 GraphQL 查询接口。

---

# 1️⃣ Book 类型设计

定义 GraphQL Schema：

```graphql
type Book {
  id: ID
  title: String
  author: String
  price: Float
}
```

---

# 2️⃣ Query Schema

```graphql
type Query {
  searchBooks(keyword: String): [Book]
}
```

---

# 3️⃣ Controller 实现

复用原有 Service 层逻辑：

```java
@QueryMapping
public List<BookDTO> searchBooks(@Argument String keyword) {
    log.info("GraphQL search keyword: {}", keyword);
    return bookService.searchBooks(keyword);
}
```

特点：

* 不重复写业务逻辑
* 直接复用已有 Service
* 添加日志便于调试

---

# 4️⃣ 前端调用（变量方式）

请求示例：

```graphql
query SearchBooks($keyword: String) {
  searchBooks(keyword: $keyword) {
    title
    author
    price
  }
}
```

变量：

```json
{
  "keyword": "Java"
}
```

---

# 5️⃣ 返回结果示例

搜索关键词：

```
Java
```

返回：

```json
{
  "data": {
    "searchBooks": [
      {
        "title": "Java 核心技术",
        "author": "Cay Horstmann",
        "price": 99.0
      }
    ]
  }
}
```

---

# 📌 五、REST vs GraphQL 对比

| REST      | GraphQL |
| --------- | ------- |
| 固定接口      | 自定义字段   |
| 可能过多或过少数据 | 精确查询    |
| 多接口       | 单入口     |

GraphQL 优势：

* 前端按需获取字段
* 减少网络传输
* 统一查询入口

