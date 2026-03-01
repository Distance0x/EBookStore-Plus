package com.ebookstore.ebookstorebackend.service;

import com.ebookstore.ebookstorebackend.entity.Book;
import com.ebookstore.ebookstorebackend.entity.Order;
import com.ebookstore.ebookstorebackend.entity.OrderItem;

import java.util.List;

public interface OrderItemService {
    OrderItem createOrderItem(Order order, Book book, Integer quantity);
    List<OrderItem> getOrderItemsByOrderId(Long orderId);
    void deleteOrderItem(Long orderItemId);
    void deleteOrderItemsByOrderId(Long orderId);
    void deleteOrderItemsByUserId(Long userId);
}