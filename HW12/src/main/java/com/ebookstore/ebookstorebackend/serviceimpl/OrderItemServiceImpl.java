package com.ebookstore.ebookstorebackend.serviceimpl;

import com.ebookstore.ebookstorebackend.entity.Book;
import com.ebookstore.ebookstorebackend.entity.Order;
import com.ebookstore.ebookstorebackend.entity.OrderItem;
import com.ebookstore.ebookstorebackend.dao.OrderItemDao;
import com.ebookstore.ebookstorebackend.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemDao orderItemDao;    
    @Override
    @Transactional
    public OrderItem createOrderItem(Order order, Book book, Integer quantity) {
        OrderItem orderItem = new OrderItem(
                order,
                book.getId(),
                book.getPrice(),
                quantity
        );
        
        return orderItemDao.save(orderItem);
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemDao.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public void deleteOrderItem(Long orderItemId) {
        orderItemDao.deleteById(orderItemId);
    }

    @Override
    @Transactional
    public void deleteOrderItemsByOrderId(Long orderId) {
        orderItemDao.deleteByOrderId(orderId);
    }

    @Override
    @Transactional
    public void deleteOrderItemsByUserId(Long userId) {
        orderItemDao.deleteByUserId(userId);
    }
}