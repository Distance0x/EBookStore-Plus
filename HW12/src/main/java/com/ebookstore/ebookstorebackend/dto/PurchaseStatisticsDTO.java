package com.ebookstore.ebookstorebackend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购书统计数据传输对象
 */
public class PurchaseStatisticsDTO {
    private Integer totalBooks;  // 购买书籍总数量
    private BigDecimal totalAmount;  // 购买总金额
    private List<BookStatisticsDTO> bookStatistics;  // 每本书的统计信息
    
    public PurchaseStatisticsDTO() {}
    
    public PurchaseStatisticsDTO(Integer totalBooks, BigDecimal totalAmount, List<BookStatisticsDTO> bookStatistics) {
        this.totalBooks = totalBooks;
        this.totalAmount = totalAmount;
        this.bookStatistics = bookStatistics;
    }
    
    // Getters and Setters
    public Integer getTotalBooks() {
        return totalBooks;
    }
    
    public void setTotalBooks(Integer totalBooks) {
        this.totalBooks = totalBooks;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public List<BookStatisticsDTO> getBookStatistics() {
        return bookStatistics;
    }
    
    public void setBookStatistics(List<BookStatisticsDTO> bookStatistics) {
        this.bookStatistics = bookStatistics;
    }
    
    /**
     * 单本书的统计信息
     */
    public static class BookStatisticsDTO {
        private String bookTitle;
        private String bookAuthor;
        private String bookCover;
        private Integer quantity;  // 购买数量
        private BigDecimal totalPrice;  // 该书总花费
        
        public BookStatisticsDTO() {}
        
        public BookStatisticsDTO(String bookTitle, String bookAuthor, String bookCover, Integer quantity, BigDecimal totalPrice) {
            this.bookTitle = bookTitle;
            this.bookAuthor = bookAuthor;
            this.bookCover = bookCover;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
        }
        
        // Getters and Setters
        public String getBookTitle() {
            return bookTitle;
        }
        
        public void setBookTitle(String bookTitle) {
            this.bookTitle = bookTitle;
        }
        
        public String getBookAuthor() {
            return bookAuthor;
        }
        
        public void setBookAuthor(String bookAuthor) {
            this.bookAuthor = bookAuthor;
        }
        
        public String getBookCover() {
            return bookCover;
        }
        
        public void setBookCover(String bookCover) {
            this.bookCover = bookCover;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public BigDecimal getTotalPrice() {
            return totalPrice;
        }
        
        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }
}
