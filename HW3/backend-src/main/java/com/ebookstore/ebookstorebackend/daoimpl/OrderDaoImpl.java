package com.ebookstore.ebookstorebackend.daoimpl;

import com.ebookstore.ebookstorebackend.dao.OrderDao;
import com.ebookstore.ebookstorebackend.entity.Order;
import com.ebookstore.ebookstorebackend.entity.User;
import com.ebookstore.ebookstorebackend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderDaoImpl implements OrderDao {
    
    private final OrderRepository orderRepository;
    
    @Autowired
    public OrderDaoImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Override
    @Transactional
    public Order save(Order order) {
//        int result = 10 / 0;
        return orderRepository.save(order);

    }
    
    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }
    
    @Override
    public List<Order> findByUser(User user) {
        return orderRepository.findByUser(user);
    }
    
    @Override
    public List<Order> findByUserOrderByCreateTimeDesc(User user) {
        return orderRepository.findByUserOrderByCreateTimeDesc(user);
    }
    
    @Override
    public void delete(Order order) {
        orderRepository.delete(order);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        orderRepository.deleteAllByUserId(userId);
    }
    
    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
    
    // 用户订单搜索实现
    @Override
    public List<Order> findUserOrdersByTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return orderRepository.findUserOrdersByTimeRange(userId, startTime, endTime);
    }
    
    @Override
    public List<Order> findUserOrdersByBookTitle(Long userId, String bookTitle) {
        return orderRepository.findUserOrdersByBookTitle(userId, bookTitle);
    }
    
    @Override
    public List<Order> findUserOrdersByBookTitleAndTimeRange(Long userId, String bookTitle, LocalDateTime startTime, LocalDateTime endTime) {
        return orderRepository.findUserOrdersByBookTitleAndTimeRange(userId, bookTitle, startTime, endTime);
    }
    
    // 管理员订单搜索实现
    @Override
    public List<Order> findOrdersByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return orderRepository.findOrdersByTimeRange(startTime, endTime);
    }
    
    @Override
    public List<Order> findOrdersByBookTitle(String bookTitle) {
        return orderRepository.findOrdersByBookTitle(bookTitle);
    }
    
    @Override
    public List<Order> findOrdersByBookTitleAndTimeRange(String bookTitle, LocalDateTime startTime, LocalDateTime endTime) {
        return orderRepository.findOrdersByBookTitleAndTimeRange(bookTitle, startTime, endTime);
    }
    
    // 用户购书统计实现
    @Override
    public List<Order> findCompletedOrdersByUserAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return orderRepository.findCompletedOrdersByUserAndTimeRange(userId, startTime, endTime);
    }    @Override
    public List<Order> findCompletedOrdersByTimeRange(LocalDateTime startTime, LocalDateTime endTime){
        return orderRepository.findCompletedOrdersByTimeRange(startTime, endTime);
    }
    
    // 分页方法实现
    @Override
    public Page<Order> findByUserOrderByCreateTimeDesc(User user, Pageable pageable) {
        return orderRepository.findByUserOrderByCreateTimeDesc(user, pageable);
    }
    
    @Override
    public Page<Order> findAllByOrderByCreateTimeDesc(Pageable pageable) {
        return orderRepository.findAllByOrderByCreateTimeDesc(pageable);
    }
    
    @Override
    public Page<Order> findUserOrdersByTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return orderRepository.findUserOrdersByTimeRange(userId, startTime, endTime, pageable);
    }
    
    @Override
    public Page<Order> findUserOrdersByBookTitle(Long userId, String bookTitle, Pageable pageable) {
        return orderRepository.findUserOrdersByBookTitle(userId, bookTitle, pageable);
    }
    
    @Override
    public Page<Order> findUserOrdersByBookTitleAndTimeRange(Long userId, String bookTitle, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return orderRepository.findUserOrdersByBookTitleAndTimeRange(userId, bookTitle, startTime, endTime, pageable);
    }
    
    @Override
    public Page<Order> findOrdersByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return orderRepository.findOrdersByTimeRange(startTime, endTime, pageable);
    }
    
    @Override
    public Page<Order> findOrdersByBookTitle(String bookTitle, Pageable pageable) {
        return orderRepository.findOrdersByBookTitle(bookTitle, pageable);
    }
    
    @Override
    public Page<Order> findOrdersByBookTitleAndTimeRange(String bookTitle, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return orderRepository.findOrdersByBookTitleAndTimeRange(bookTitle, startTime, endTime, pageable);
    }
    
    @Override
    public Page<Order> findCompletedOrdersByUserAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return orderRepository.findCompletedOrdersByUserAndTimeRange(userId, startTime, endTime, pageable);
    }
    
    @Override
    public Page<Order> findCompletedOrdersByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return orderRepository.findCompletedOrdersByTimeRange(startTime, endTime, pageable);
    }
}