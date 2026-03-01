package com.ebookstore.ebookstorebackend.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "book_info")
public class BookInfo {
    @Id
    private Long bookId;  // 对应 MySQL 中的 book.id
    
    private String cover;       // 封面图片 URL 或 Base64
    private String description; // 内容介绍
    
    // 构造函数
    public BookInfo() {}

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BookInfo(Long bookId, String cover, String description) {
        this.bookId = bookId;
        this.cover = cover;
        this.description = description;
    }
}
