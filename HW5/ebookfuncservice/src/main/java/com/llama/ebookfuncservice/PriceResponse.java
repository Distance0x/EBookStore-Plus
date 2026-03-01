package com.llama.ebookfuncservice;

import java.math.BigDecimal;


public class PriceResponse {
    private BigDecimal price;       // 输入的单价
    private Integer quantity;       // 输入的数量
    private BigDecimal total;       // 计算后的总价 (price × quantity)
    private String status;

    public PriceResponse() {
        this.status = "success";
    }

    public PriceResponse(BigDecimal price, Integer quantity, BigDecimal total) {
        this.price = price;
        this.quantity = quantity;
        this.total = total;
        this.status = "success";
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PriceResponse{price=" + price + ", quantity=" + quantity +
                ", total=" + total + ", status='" + status + "'}";
    }
}
