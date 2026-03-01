# 应用系统体系架构 — 作业8

> MySQL + MongoDB + Neo4j 多数据库融合设计

---

# 📌 一、设计目标

原系统：

* 所有图书信息全部存储在 MySQL

本次改造目标：

1. 避免关系型数据库存储大字段
2. 为 description 非结构化扩展做准备
3. 引入图数据库实现标签关系搜索

最终形成：

* MySQL → 核心结构化数据
* MongoDB → 大字段文档数据
* Neo4j → 标签关系图

---

# 📌 二、MySQL + MongoDB 数据拆分

---

## 1️⃣ MySQL：核心结构化字段

保留字段：

* id
* title
* author
* price
* stock
* tags

移除字段：

* cover
* description

---

## MySQL 实体 Book

```java id="x4r1hz"
@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 结构化字段省略

    // MongoDB字段，仅用于展示
    @Transient
    private String cover;

    @Transient
    private String description;
}
```

---

## 2️⃣ MongoDB：BookInfo 文档

```java id="yzx7rc"
@Document(collection = "book_info")
public class BookInfo {

    @Id
    private Long bookId; // 对应 MySQL book.id

    private String cover;
    private String description;
}
```

特点：

* 一书一文档
* 大字段存储在文档数据库
* 支持未来扩展

---

## 3️⃣ DAO 层整合查询

```java id="u38p3j"
@Override
public Book findBookById(Long id) {

    Book book = bookRepository.findById(id).orElse(null);

    fillBookInfo(book);

    return book;
}

private void fillBookInfo(Book book) {

    if (book == null) return;

    bookInfoRepository.findByBookId(book.getId())
        .ifPresent(info -> {
            book.setCover(info.getCover());
            book.setDescription(info.getDescription());
        });
}
```

逻辑：

1. 先查 MySQL
2. 再查 MongoDB
3. 回填数据
4. 返回完整对象

---

# 📌 三、标签系统 + Neo4j 图搜索

---

## 1️⃣ MySQL 标签字段

```sql id="s98hfh"
ALTER TABLE book
ADD COLUMN tags VARCHAR(500);
```

示例：

```
"计算机,系统,底层"
```

---

## 2️⃣ Neo4j 标签节点模型

```java id="k9wx01"
@Node("Tag")
public class TagNode {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Relationship(type = "PARENT_OF")
    private List<TagNode> children;
}
```

形成：

* 标签层次树
* 父子关系图

例如：

```
计算机 → 编程 → Python
小说 → 文学 → 日本文学
```

---

# 📌 四、Neo4j 2 跳搜索

---

## 查询 2 跳内标签

```java id="v03lfk"
@Query("""
MATCH (t:Tag {name: $name})
MATCH (t)-[:PARENT_OF*0..2]-(related:Tag)
RETURN DISTINCT related.name
""")
List<String> findRelatedTagsWithin2Hops(@Param("name") String name);
```

特点：

* 0~2 跳范围
* 看作无向图
* 获取所有关联标签

---

# 📌 五、最终整合查询逻辑

---

## Service 实现

```java id="mb9m4l"
@Override
@Transactional(readOnly = true)
public List<BookDTO> searchBooksByTag(String tagName) {

    // 1. 从 Neo4j 获取 2 跳内标签
    List<String> relatedTags =
        tagDao.getRelatedTagsWithin2Hops(tagName);

    // 2. 在 MySQL 查询含任意标签的书
    List<Book> books =
        bookDao.findByTagsIn(relatedTags);

    // 3. 转换 DTO 返回
    return books.stream()
        .map(bookMapper::toDTO)
        .toList();
}
```

---

# 📌 六、执行流程

```text id="a3h7lu"
用户输入标签
   ↓
Neo4j 查询 2 跳内相关标签
   ↓
MySQL 查询匹配标签的书
   ↓
MongoDB 回填大字段
   ↓
返回完整 BookDTO
```

---

# 📌 七、架构优势分析

---

## 1️⃣ MySQL

* 强事务
* 结构化数据
* 关系查询

---

## 2️⃣ MongoDB

* 适合大字段
* 文档结构灵活
* 支持非结构化扩展

---

## 3️⃣ Neo4j

* 图结构关系查询高效
* 2 跳搜索性能好
* 层级关系天然表达

---

# 📌 八、对比单数据库方案

| 单 MySQL | 多数据库融合  |
| ------- | ------- |
| 大字段占页   | 文档数据库分离 |
| 标签字符串匹配 | 图搜索     |
| 扩展性差    | 扩展性强    |

