package com.ebookstore.ebookstorebackend.dto;

import org.springframework.data.domain.Page;
import java.math.BigDecimal;

/**
 * 统计分页响应DTO，包含分页数据和全局统计信息
 */
public class StatisticsPageResponseDTO<T> {
    private Page<T> page;
    private GlobalStatistics globalStatistics;    public StatisticsPageResponseDTO() {}

    public StatisticsPageResponseDTO(Page<T> page, GlobalStatistics globalStatistics) {
        this.page = page;
        this.globalStatistics = globalStatistics;
    }

    public Page<T> getPage() {
        return page;
    }

    public void setPage(Page<T> page) {
        this.page = page;
    }

    public GlobalStatistics getGlobalStatistics() {
        return globalStatistics;
    }

    public void setGlobalStatistics(GlobalStatistics globalStatistics) {
        this.globalStatistics = globalStatistics;
    }

    /**
     * 全局统计信息
     */
    public static class GlobalStatistics {
        private Integer totalOrderCount;     // 总订单数
        private Integer totalBooksSold;      // 总销售册数
        private BigDecimal totalRevenue;     // 总收入
        private Integer totalBookTypes;      // 总书籍种类数

        public GlobalStatistics() {}

        public GlobalStatistics(Integer totalOrderCount, Integer totalBooksSold, 
                               BigDecimal totalRevenue, Integer totalBookTypes) {
            this.totalOrderCount = totalOrderCount;
            this.totalBooksSold = totalBooksSold;
            this.totalRevenue = totalRevenue;
            this.totalBookTypes = totalBookTypes;
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

        public Integer getTotalBookTypes() {
            return totalBookTypes;
        }

        public void setTotalBookTypes(Integer totalBookTypes) {
            this.totalBookTypes = totalBookTypes;
        }
    }
}
