# 应用系统体系架构 — 作业6

> MySQL 存储结构 + 索引设计 + 主键选择 + Buffer Pool 机制分析

---

# 📌 一、为什么聚簇索引查询效率高？

在 InnoDB 中，主键索引是 **聚簇索引（Clustered Index）**。

## 1️⃣ 结构特点

* 使用 B+Tree 结构
* 叶子节点存储 **完整数据行**
* 数据按主键物理顺序存储
* 数据页之间逻辑有序、物理上基本连续

---

## 2️⃣ 直接查询效率高

```sql
SELECT * FROM book WHERE id = 10;
```

执行过程：

* 定位到 B+Tree 叶子节点
* 直接返回完整数据
* 不需要回表

对比二级索引：

* 二级索引叶子节点只存主键
* 需要再根据主键去聚簇索引查一次（回表）

---

## 3️⃣ 范围查询效率更高

```sql
SELECT * FROM book WHERE id BETWEEN 100 AND 200;
```

原因：

* 数据物理连续
* 叶子节点之间链表连接
* 可以顺序读取页
* 极大减少磁盘 I/O

相比二级索引：

* 需要频繁根据主键跳转
* 磁盘随机 I/O 增加

---

# 📌 二、Dynamic / Compact / Compressed 行格式对比

InnoDB 行格式差异主要体现在：

* 大字段（VARCHAR/BLOB/TEXT）存储方式
* 页内空间利用率
* CPU 开销

---

## 1️⃣ Compact

特点：

* 尽量在行内存储
* 超过约 767B 才溢出
* 行内保留部分字段数据

问题：

* 容易产生页内碎片
* 页利用率不稳定

---

## 2️⃣ Dynamic

特点：

* 大字段直接存溢出页
* 行内只保留指针
* 页内结构更紧凑

优点：

* 页更小
* Buffer Pool 可缓存更多行
* 减少碎片

---

## 3️⃣ Compressed

特点：

* 和 Dynamic 溢出策略相同
* 溢出页数据会压缩

缺点：

* 每次读写需要压缩/解压
* CPU 开销高

---

## ✅ 为什么默认选择 Dynamic？

这是一个典型的 trade-off：

* 避免 Compact 的页内碎片问题
* 避免 Compressed 的 CPU 开销
* 提高缓存命中率
* 数据页可容纳更多行

---

# 📌 三、复合索引设计

假设 book 表字段：

* author
* title
* publish_date

---

## 建立复合索引

```sql
CREATE INDEX idx_book_author_title_pubdate
ON book(author ASC, title ASC, publish_date DESC);
```

---

## 字段顺序设计原则

### 1️⃣ author 放首位

* 查询频率高
* WHERE author = ?
* 满足最左前缀原则

---

### 2️⃣ title 放第二位

支持：

```sql
WHERE author = ? AND title = ?
```

避免索引失效

---

### 3️⃣ publish_date 放最后

* 次要筛选字段
* 常用于排序
* 放最后可以用于 ORDER BY

---

## 升降序考虑

* author：升序（默认）
* title：升序
* publish_date：降序

常见需求：

```sql
WHERE author = ?
ORDER BY publish_date DESC
```

使用 DESC 可以避免额外排序

---

# 📌 四、订单表主键：自增 vs UUID

## ✅ 选择：自增主键

---

## 1️⃣ 主键越短越好

* UUID 长度 36 字符
* BIGINT 仅 8 字节

---

## 2️⃣ InnoDB 二级索引包含主键

主键会被复制到：

* 每一个二级索引

UUID 会导致：

* 索引膨胀
* Buffer Pool 浪费

---

## 3️⃣ 插入效率

自增主键：

* 追加写
* B+Tree 尾部插入
* 减少页分裂

UUID：

* 随机插入
* 频繁页分裂
* 随机 I/O 增加

---

## 4️⃣ 结合项目实际

* 数据量不大
* 无分布式主键需求
* 自增主键更合适

---

# 📌 五、为什么 Buffer Pool 新页插入 LRU 的 3/8 处？

InnoDB 的 LRU 实际是：

* young 区
* old 区

比例大约 5/8 : 3/8

---

## 如果使用标准 LRU

全表扫描时：

* 大量新页进入
* 挤出热点数据
* 造成缓存污染

---

## 插入到 old 区的意义

* 新页先进入 old 区
* 如果只是一次性扫描
* 很快被淘汰
* 不影响热点页

---

## 如果页面被频繁访问

* 会被提升到 young 区
* 成为真正热点数据

---

## 3/8 是 trade-off

* old 区足够大
* 又不会影响热点数据晋升

---

# 📌 六、整体数据库设计思想总结

| 设计点         | 原理     |
| ----------- | ------ |
| 聚簇索引        | 减少回表   |
| Dynamic 行格式 | 提升缓存效率 |
| 复合索引        | 最左前缀原则 |
| 自增主键        | 减少页分裂  |
| LRU 3/8 策略  | 避免缓存污染 |
