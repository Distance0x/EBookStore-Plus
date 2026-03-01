package com.ebookstore.ebookstorebackend.daoimpl;

import com.ebookstore.ebookstorebackend.dao.OrderItemDao;
import com.ebookstore.ebookstorebackend.entity.OrderItem;
import com.ebookstore.ebookstorebackend.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemDaoImpl implements OrderItemDao {
    
    private final OrderItemRepository orderItemRepository;
    
    @Autowired
    public OrderItemDaoImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }
    
    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }
    
    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
    
    @Override
    public void deleteById(Long id) {
        orderItemRepository.deleteById(id);
    }
    
    @Override
    public void deleteByOrderId(Long orderId) {
        orderItemRepository.deleteByOrderId(orderId);
    }
    
    @Override
    public void deleteByUserId(Long userId) {
        orderItemRepository.deleteByUserId(userId);
    }
}