package com.ebookstore.ebookstorebackend.dao;

import com.ebookstore.ebookstorebackend.entity.OrderItem;
import java.util.List;

public interface OrderItemDao {
    OrderItem save(OrderItem orderItem);
    List<OrderItem> findByOrderId(Long orderId);
    void deleteById(Long id);
    void deleteByOrderId(Long orderId);
    void deleteByUserId(Long userId);
}