package com.ebookstore.ebookstorebackend.dto;

import java.math.BigDecimal;

public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private String cover;    private String description;
    private String isbn;
    private Integer stock;
    private String publisher;
    private String tags;
    private Boolean deleted;

    public BookDTO() {
    }    public BookDTO(Long id, String title, String author, BigDecimal price, String cover, String description) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.cover = cover;
        this.description = description;
        this.deleted = false;
    }
    
    public BookDTO(Long id, String title, String author, BigDecimal price, String cover, String description,
                   String isbn, Integer stock, String publisher) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.cover = cover;
        this.description = description;
        this.isbn = isbn;
        this.stock = stock;
        this.publisher = publisher;
        this.deleted = false;
    }
    
    public BookDTO(Long id, String title, String author, BigDecimal price, String cover, String description,
                   String isbn, Integer stock, String publisher, Boolean deleted) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.cover = cover;
        this.description = description;
        this.isbn = isbn;
        this.stock = stock;
        this.publisher = publisher;
        this.deleted = deleted;
    }
    
    public BookDTO(Long id, String title, String author, BigDecimal price, String cover, String description,
                   String isbn, Integer stock, String publisher, String tags, Boolean deleted) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.cover = cover;
        this.description = description;
        this.isbn = isbn;
        this.stock = stock;
        this.publisher = publisher;
        this.tags = tags;
        this.deleted = deleted;
    }

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
    }    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    
    public Boolean isDeleted() {
        return deleted != null && deleted;
    }
}
