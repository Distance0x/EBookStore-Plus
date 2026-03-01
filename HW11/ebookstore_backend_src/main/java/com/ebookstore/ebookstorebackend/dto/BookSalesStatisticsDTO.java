package com.ebookstore.ebookstorebackend.dto;

import java.math.BigDecimal;
import java.util.List;



public class BookSalesStatisticsDTO {

    private List<BookSalesDTO> bookSales;  // 各书籍销量详情
    private Integer totalOrderCount; // 总订单数
    private Integer totalBooksSold; // 总销售册数
    private BigDecimal totalRevenue; // 总收入 总价格

    public BookSalesStatisticsDTO() {
    }

    public BookSalesStatisticsDTO(List<BookSalesDTO> bookSales, Integer totalOrderCount, Integer totalBooksSold, BigDecimal totalRevenue) {
        this.bookSales = bookSales;
        this.totalOrderCount = totalOrderCount;
        this.totalBooksSold = totalBooksSold;
        this.totalRevenue = totalRevenue;
    }

    public List<BookSalesDTO> getBookSales() {
        return bookSales;
    }

    public void setBookSales(List<BookSalesDTO> bookSales) {
        this.bookSales = bookSales;
    }

    public Integer getTotalOrderCount() {
        return totalOrderCount;
    }

    public void setTotalOrderCount(Integer totalOrderCount) {
        this.totalOrderCount = totalOrderCount;
    }

    public Integer getTotalBooksSold() {
        return totalBooksSold;
    }

    public void setTotalBooksSold(Integer totalBooksSold) {
        this.totalBooksSold = totalBooksSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    // 单本书销售统计信息
    public static class BookSalesDTO {
        private Long bookId;
        private String bookTitle;
        private String bookAuthor;
        private String bookCover;
        private String publisher;
        private BigDecimal price;
        private Integer quantitySold;      // 销售数量
        private BigDecimal revenue;        // 该书总收入
        private Integer orderCount;        // 包含该书的订单数

        public BookSalesDTO() {}


        public BookSalesDTO(Long bookId, String bookTitle, String bookAuthor, String bookCover,
                            String publisher, BigDecimal price, Integer quantitySold,
                            BigDecimal revenue, Integer orderCount) {
            this.bookId = bookId;
            this.bookTitle = bookTitle;
            this.bookAuthor = bookAuthor;
            this.bookCover = bookCover;
            this.publisher = publisher;
            this.price = price;
            this.quantitySold = quantitySold;
            this.revenue = revenue;
            this.orderCount = orderCount;
        }

        // Getters and Setters
        public Long getBookId() {
            return bookId;
        }

        public void setBookId(Long bookId) {
            this.bookId = bookId;
        }

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

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getQuantitySold() {
            return quantitySold;
        }

        public void setQuantitySold(Integer quantitySold) {
            this.quantitySold = quantitySold;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }

        public Integer getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(Integer orderCount) {
            this.orderCount = orderCount;
        }
    }

}
