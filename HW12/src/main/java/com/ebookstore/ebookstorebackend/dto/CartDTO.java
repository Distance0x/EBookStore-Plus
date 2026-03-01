package com.ebookstore.ebookstorebackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartDTO {
    private Long id;
    private Long bookId;
    private String name;  // 前端使用的字段名
    private String author;
    private BigDecimal price;
    private String cover;
    private Integer quantity;
    private BigDecimal subtotal;
    private LocalDateTime addedAt;

    public CartDTO() {}

    public CartDTO(Long id, Long bookId, String name, String author, 
                  BigDecimal price, String cover, Integer quantity, LocalDateTime addedAt) {
        this.id = id;
        this.bookId = bookId;
        this.name = name;
        this.author = author;
        this.price = price;
        this.cover = cover;
        this.quantity = quantity;
        this.addedAt = addedAt;
        // 自动计算小计
        this.subtotal = price.multiply(new BigDecimal(quantity));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        // 当价格变化时重新计算小计
        if (this.quantity != null) {
            this.subtotal = price.multiply(new BigDecimal(this.quantity));
        }
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        // 当数量变化时重新计算小计
        if (this.price != null) {
            this.subtotal = this.price.multiply(new BigDecimal(quantity));
        }
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
