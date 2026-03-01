package com.ebookstore.ebookstorebackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column
    private String cover;
      
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 20)
    private String isbn;
    
    @Column(columnDefinition = "TEXT")
    private String publisher;
    
    // 添加软删除字段
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    // 无参构造函数
    public Book() {
    }
      // 带参构造函数
    public Book(String title, String author, BigDecimal price, String cover, String description) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.cover = cover;
        this.description = description;
    }
    
    // 完整带参构造函数
    public Book(String title, String author, BigDecimal price, String cover, String description, 
               String isbn, String publisher) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.cover = cover;
        this.description = description;
        this.isbn = isbn;
        this.publisher = publisher;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getCover() {
        return cover;
    }
    
    public void setCover(String cover) {
        this.cover = cover;
    }
    
    public String getDescription() {
        return description;
    }
      public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    // 添加deleted字段的getter和setter
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    
    // 便捷方法
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }
    
    public void markAsDeleted() {
        this.deleted = true;
    }
}
