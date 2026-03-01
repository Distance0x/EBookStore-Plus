package com.ebookstore.ebookstorebackend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户消费统计数据传输对象
 */
public class UserConsumptionStatisticsDTO {
    private List<UserConsumptionDTO> userConsumptions;  // 各用户消费详情
    private Integer totalUsers;                         // 统计用户总数
    private Integer totalOrders;                        // 总订单数
    private BigDecimal totalRevenue;                    // 总收入

    public UserConsumptionStatisticsDTO() {}

    public UserConsumptionStatisticsDTO(List<UserConsumptionDTO> userConsumptions, Integer totalUsers,
                                        Integer totalOrders, BigDecimal totalRevenue) {
        this.userConsumptions = userConsumptions;
        this.totalUsers = totalUsers;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

    // Getters and Setters
    public List<UserConsumptionDTO> getUserConsumptions() {
        return userConsumptions;
    }

    public void setUserConsumptions(List<UserConsumptionDTO> userConsumptions) {
        this.userConsumptions = userConsumptions;
    }

    public Integer getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    /**
     * 单个用户的消费统计信息
     */
    public static class UserConsumptionDTO {
        private Long userId;
        private String username;
        private String name;
        private String email;
        private String phone;
        private BigDecimal totalSpent;          // 累计消费金额
        private Integer orderCount;             // 订单数量
        private Integer totalBooksCount;        // 购买书籍总数量
        private BigDecimal averageOrderValue;   // 平均订单金额

        public UserConsumptionDTO() {}

        public UserConsumptionDTO(Long userId, String username, String name, String email, String phone,
                                  BigDecimal totalSpent, Integer orderCount, Integer totalBooksCount,
                                  BigDecimal averageOrderValue) {
            this.userId = userId;
            this.username = username;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.totalSpent = totalSpent;
            this.orderCount = orderCount;
            this.totalBooksCount = totalBooksCount;
            this.averageOrderValue = averageOrderValue;
        }

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public BigDecimal getTotalSpent() {
            return totalSpent;
        }

        public void setTotalSpent(BigDecimal totalSpent) {
            this.totalSpent = totalSpent;
        }

        public Integer getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(Integer orderCount) {
            this.orderCount = orderCount;
        }

        public Integer getTotalBooksCount() {
            return totalBooksCount;
        }

        public void setTotalBooksCount(Integer totalBooksCount) {
            this.totalBooksCount = totalBooksCount;
        }

        public BigDecimal getAverageOrderValue() {
            return averageOrderValue;
        }

        public void setAverageOrderValue(BigDecimal averageOrderValue) {
            this.averageOrderValue = averageOrderValue;
        }
    }
}