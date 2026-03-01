# 应用系统体系架构 — 作业4

> Redis 缓存接入 + 表结构拆分 + 缓存降级设计

---

# 📌 一、数据库结构优化

对原有 `book` 表进行拆分：

* book（基础信息）
* book_stock（库存信息）

---

## 1️⃣ book 表

```sql
CREATE TABLE book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    cover LONGTEXT,
    description TEXT,
    isbn VARCHAR(20),
    publisher TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
```

---

## 2️⃣ book_stock 表

```sql
CREATE TABLE book_stock (
    book_id BIGINT NOT NULL,
    stock INT DEFAULT 0,
    PRIMARY KEY (book_id),
    FOREIGN KEY (book_id)
        REFERENCES book(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
```

### 🎯 设计目的

* 将高频变化字段（库存）与基础信息分离
* 减少缓存失效范围
* 提高读性能

---

# 📌 二、Redis 缓存接入


---

## 1️⃣ 首次读取

日志显示：

```text
[BOOK CACHE MISS] Querying DB for bookId: 1
[BOOK CACHE LOAD] Cached bookId: 1
```

说明：

* Redis 未命中
* 查询 MySQL
* 数据写入缓存

---

## 2️⃣ 再次读取

```text
[CACHE HIT] Cache: 'book', Key: '1'
```

说明：

* 直接从 Redis 读取
* 无数据库访问

---

# 📌 三、缓存更新机制
管理员修改书籍信息后：

* 删除对应缓存
* 下次读取重新 Cache

---

## 更新书籍使用 @CacheEvict

```java
@Transactional
@Caching(evict = {
    @CacheEvict(value = "book", key = "#id"),
    @CacheEvict(value = "bookList", allEntries = true)
})
public BookDTO updateBook(Long id, BookDTO bookDTO) {
    ...
}
```

---

# 📌 四、@Cacheable 实现

```java
@Transactional(readOnly = true)
@Cacheable(value = "book", key = "#id", unless = "#result == null")
public BookDTO getBookById(Long id) {

    logger.info("[BOOK CACHE MISS] Querying DB for bookId: {}", id);

    Book book = bookDao.findByIdAndDeletedFalse(id).orElse(null);

    if (book == null) {
        logger.warn("[BOOK NOT FOUND] bookId: {}", id);
        return null;
    }

    BookDTO dto = bookMapper.toDTO(book);
    fillStock(dto);

    return dto;
}
```

---

# 📌 五、自定义 CacheManager（增强日志）


通过包装 RedisCacheManager：

```java
@Override
public ValueWrapper get(Object key) {
    ValueWrapper result = cache.get(key);
    if (result != null) {
        logger.info("[CACHE HIT] Cache: '{}', Key: '{}'", name, key);
    }
    return result;
}
```

实现：

* 统一打印 Cache HIT
* 更清晰观察缓存行为

---

# 📌 六、Redis 宕机降级策略


关闭 Redis 后：

* 不抛异常
* 自动 fallback 到 MySQL
* 页面仍然可正常显示

---

## 自定义 CacheErrorHandler


```java
@Override
public void handleCacheGetError(RuntimeException exception,
                                Cache cache,
                                Object key) {
    logger.warn("[REDIS ERROR] Cache GET failed, fallback to DB.");
}
```

设计思想：

* Redis 只是缓存
* 不影响主业务逻辑
* 系统具备容错能力

---

# 📌 七、缓存策略总结

| 场景      | 行为         |
| ------- | ---------- |
| 首次访问    | DB → Redis |
| 再次访问    | Redis      |
| 更新数据    | 删除缓存       |
| Redis宕机 | 自动降级       |

---

# 📌 八、核心设计思想

## ✅ 表拆分

* 高变字段与低变字段分离
* 降低缓存频繁失效

---

## ✅ 缓存设计

* TTL = 2 小时
* JSON 序列化
* 禁止缓存 null

---

## ✅ 容错设计

* CacheErrorHandler
* 不因 Redis 宕机导致服务不可用
