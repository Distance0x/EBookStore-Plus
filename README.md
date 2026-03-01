# 应用系统体系架构（Sjtu-se） — 作业合集

**课程名称：应用系统体系架构**
**课程号：SE3353**
---

## 📂 项目导航

| 编号 | 主题 | 目录 |
|------|------|------|
| HW1  | Spring Bean 作用域 + Kafka 异步消息 + 订单落库 | [./HW1](./HW1) |
| HW2  | Spring 事务传播机制 + Kafka 异步处理 + WebSocket 实时通知 | [./HW2](./HW2) |
| HW3  | WebSocket 精准推送 + 线程安全设计 + 数据库恢复机制 | [./HW3](./HW3) |
| HW4  | Redis 缓存接入 + 表结构拆分 + 缓存降级设计 | [./HW4](./HW4) |
| HW5  | Eureka + Gateway + 微服务 + 函数式服务 | [./HW5](./HW5) |
| HW6  | MySQL 存储结构 + 索引设计 + Buffer Pool 机制分析 | [./HW6](./HW6) |
| HW7  | 数据库备份策略 + 分区设计 + MCP 工具理解 | [./HW7](./HW7) |
| HW8  | MySQL + MongoDB + Neo4j 多数据库融合设计 | [./HW8](./HW8) |
| HW9  | InfluxDB 监控实践 + LSM-tree 放大率 + 向量数据库 PQ | [./HW9](./HW9) |
| HW10 | n8n Agent 工作流 + MCP Tool 调用 + 前端集成 | [./HW10](./HW10) |
| HW11 | Nginx 负载均衡策略实验 + GraphQL 查询接口 | [./HW11](./HW11) |
| HW12 | Docker 容器化部署 + MySQL 持久化 + Hadoop & MapReduce | [./HW12](./HW12) |

---

## 📖 各作业简介

### [HW1 — Spring Bean 作用域 + Kafka 消息处理](./HW1)

- Spring Bean 三种作用域（Session / Singleton / Prototype）设计原则
- Kafka 异步消息驱动订单处理
- 订单落库与事务基础

---

### [HW2 — 事务传播机制 + WebSocket 实时通知](./HW2)

- `Propagation.REQUIRED` 与 `Propagation.REQUIRES_NEW` 对比实验
- Kafka 消费者异步处理订单
- WebSocket 向前端推送订单结果

---

### [HW3 — WebSocket 精准推送 + 线程安全](./HW3)

- WebSocket 消息结构（DTO）升级
- 基于 `ConcurrentHashMap` 的会话管理
- 数据库崩溃恢复机制（Redo Log / Undo Log）分析

---

### [HW4 — Redis 缓存 + 表结构拆分](./HW4)

- `book` 与 `book_stock` 表拆分设计
- Redis 缓存穿透、击穿、雪崩防护
- 缓存更新策略与降级方案

---

### [HW5 — 微服务架构：Eureka + Gateway](./HW5)

- Eureka 服务注册与发现
- Spring Cloud Gateway 路由与过滤
- Author 查询微服务 + Price 函数式计算服务

---

### [HW6 — MySQL 索引与存储引擎分析](./HW6)

- 聚簇索引 vs 二级索引查询效率分析
- Dynamic / Compact / Compressed 行格式对比
- Buffer Pool 缓冲机制与主键设计策略

---

### [HW7 — 数据库备份 + 分区设计 + MCP](./HW7)

- 物理备份 vs 逻辑备份对比
- MySQL 表分区设计实践
- MCP（Model Context Protocol）工具集成理解

---

### [HW8 — 多数据库融合：MySQL + MongoDB + Neo4j](./HW8)

- MySQL 保留核心结构化字段
- MongoDB 存储封面、描述等大字段文档
- Neo4j 实现标签关系图谱搜索

---

### [HW9 — InfluxDB 监控 + LSM-tree + 向量数据库](./HW9)

- InfluxDB 实时监控 CPU 状态指标
- LSM-tree 写放大、读放大、空间放大分析
- 向量数据库乘积量化（Product Quantization）原理

---

### [HW10 — n8n AI Agent + MCP Tool](./HW10)

- n8n 构建 AI Agent 工作流
- 接入 OpenAI 模型 + Simple Memory 多轮对话
- MCP Client 访问书籍数据库，前端集成展示

---

### [HW11 — Nginx 负载均衡 + GraphQL](./HW11)

- Round Robin / IP Hash / 加权轮询负载均衡策略实验
- Nginx 反向代理配置
- GraphQL 查询接口设计与实现

---

### [HW12 — Docker 容器化 + Hadoop MapReduce](./HW12)

- Spring Boot 后端 Docker 化，MySQL 数据持久化
- Docker Compose 一键启动全栈环境
- Hadoop 集群搭建 + MapReduce 词频统计实践

---

## 🛠 技术栈总览

| 分类 | 技术 |
|------|------|
| 后端框架 | Spring Boot, Spring Cloud |
| 消息队列 | Apache Kafka |
| 实时通信 | WebSocket |
| 缓存 | Redis |
| 关系型数据库 | MySQL (InnoDB) |
| 文档数据库 | MongoDB |
| 图数据库 | Neo4j |
| 时序数据库 | InfluxDB |
| 微服务 | Eureka, Spring Cloud Gateway |
| AI Agent | n8n, OpenAI, MCP |
| 负载均衡 | Nginx |
| 查询语言 | GraphQL |
| 容器化 | Docker, Docker Compose |
| 大数据 | Hadoop, MapReduce |
