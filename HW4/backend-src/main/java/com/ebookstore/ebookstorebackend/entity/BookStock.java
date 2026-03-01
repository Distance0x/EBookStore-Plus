package com.ebookstore.ebookstorebackend.entity;

import jakarta.persistence.*;

/**
 * 图书库存实体，与 book 表通过外键关联
 */
@Entity
@Table(name = "book_stock")
public class BookStock {
    
    @Id
    @Column(name = "book_id", nullable = false)
    private Long bookId;
    
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;
    
    // 无参构造
    public BookStock() {
    }
    
    // 带参构造
    public BookStock(Long bookId, Integer stock) {
        this.bookId = bookId;
        this.stock = stock != null ? stock : 0;
    }
    
    // Getters and Setters
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    @Override
    public String toString() {
        return "BookStock{" +
                "bookId=" + bookId +
                ", stock=" + stock +
                '}';
    }
}
