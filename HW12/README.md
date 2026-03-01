# 应用系统体系架构 — 作业12

> Docker 部署 + MySQL 持久化 + Hadoop & MapReduce 实践

---

# 📌 一、后端 Docker 化部署

本次作业将整个后端系统容器化部署，实现：

* 后端服务容器化
* MySQL 容器化
* 数据持久化
* 网络隔离
* 一键启动

---

# 1️⃣ Dockerfile

构建环境：

* Maven 3.9.9
* JDK 17

核心步骤：

```dockerfile
FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/ebookstore.jar"]
```

说明：

* 工作目录 `/app`
* 构建阶段直接打包
* 暴露 8080 端口
* 启动 Spring Boot 服务

---

# 2️⃣ Docker Compose 配置

---

## MySQL 服务

镜像：

```
mysql:8.0
```

数据持久化：

```yaml
volumes:
  - ./.docker/mysql/data:/var/lib/mysql
```

作用：

* 宿主机目录绑定到容器
* 数据不会因容器销毁而丢失

初始化机制：

* `/docker-entrypoint-initdb.d/`
* 自动执行 `data.sql`
* 初始化书籍数据

---

## Backend 服务

```yaml
services:
  ebookstore-backend:
    build: .
    container_name: ebookstore-backend
    depends_on:
      mysql:
        condition: service_healthy
```

特点：

* 等待 MySQL 健康检查通过
* 再启动后端
* 保持与 application.properties 一致配置

---

## 网络配置

```yaml
networks:
  default:
    name: ebookstore-net
```

作用：

* 独立容器网络
* 服务间通过容器名通信

---

# 3️⃣ 成功部署验证

启动：

```bash
docker compose up -d
```

验证：

* 后端接口可访问
* MySQL 正常连接
* 数据初始化成功

---

# 4️⃣ 数据持久化验证

步骤：

1. 生成订单数据
2. 执行：

```bash
docker compose down
```

3. 再次启动：

```bash
docker compose up -d
```

验证结果：

* 订单仍然存在
* 数据成功持久化

说明：

> Bind Mount 生效

---

# 📌 二、Hadoop & MapReduce 实践

---

# 1️⃣ Hadoop 部署

为了在 Windows 上运行 Hadoop：

采用 Docker 部署。

---

## 部署组件

定义 4 个容器：

| 组件              | 作用        |
| --------------- | --------- |
| NameNode        | HDFS 管理节点 |
| DataNode        | 数据存储      |
| ResourceManager | YARN 调度   |
| NodeManager     | 任务执行      |

---

## 端口映射

| 组件              | 端口                  |
| --------------- | ------------------- |
| NameNode        | 9870 (Web UI), 9000 |
| DataNode        | 9864, 9866          |
| ResourceManager | 8088                |

可通过 Web UI 监控：

* HDFS 文件
* 任务执行情况
* 集群状态

---

# 2️⃣ MapReduce 实现

目标：

> 统计关键词在文本中出现的次数（支持重叠）

---

## a. Mapper

功能：

* 读取文本
* 查找关键词子串
* 统计出现次数
* 输出 (keyword, 1)

示例逻辑：

```java
while ((index = line.indexOf(keyword, index)) != -1) {
    context.write(new Text(keyword), new IntWritable(1));
    index++; // 支持重叠
}
```

---

## b. Reducer

功能：

* 对相同 keyword 的值累加
* 输出 (keyword, sum)

```java
int sum = 0;
for (IntWritable val : values) {
    sum += val.get();
}
context.write(key, new IntWritable(sum));
```

---

# 3️⃣ 后端触发 MapReduce

在 `Service.java` 中提供 POST 接口：

流程：

```text
本地准备输入文件
        ↓
uploadToHdfs()
        ↓
runJob()
        ↓
获取输出结果
```

关键步骤：

1. 获取 FileSystem 连接
2. 上传输入文件到 HDFS
3. 提交 Job
4. 等待任务完成
5. 读取输出目录

---

# 4️⃣ 执行结果

HDFS 目录结构：

```text
/input/xxx.txt
/output/part-r-00000
```

Output 文件内容：

```text
python    15
java      8
```

表示：

* python 出现 15 次
* java 出现 8 次

---

# 📌 三、整体架构整合

本次作业将系统扩展为：

```text
Spring Boot
   ↓
Docker Compose
   ↓
MySQL 持久化
   ↓
Hadoop 集群
   ↓
MapReduce 分布式计算
```
