package com.llama.ebookfuncservice;

import java.math.BigDecimal;

public class PriceRequest {
    private BigDecimal price;      // 单价
    private Integer quantity;      // 数量

    public PriceRequest() {}

    public PriceRequest(BigDecimal price, Integer quantity) {
        this.price = price;
        this.quantity = quantity;
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

    @Override
    public String toString() {
        return "PriceRequest{price=" + price + ", quantity=" + quantity + '}';
    }
}
