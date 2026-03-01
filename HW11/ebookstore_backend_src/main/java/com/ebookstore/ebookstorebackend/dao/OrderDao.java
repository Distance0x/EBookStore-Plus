package com.ebookstore.ebookstorebackend.dao;

import com.ebookstore.ebookstorebackend.entity.Order;
import com.ebookstore.ebookstorebackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderDao {
    Order save(Order order);
    Optional<Order> findById(Long id);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUser(User user);
    List<Order> findByUserOrderByCreateTimeDesc(User user);
    void delete(Order order);
    void deleteAllByUserId(Long userId);
    List<Order> findAll(); // 管理员获取所有订单

    // 用户订单搜索
    List<Order> findUserOrdersByTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    List<Order> findUserOrdersByBookTitle(Long userId, String bookTitle);
    List<Order> findUserOrdersByBookTitleAndTimeRange(Long userId, String bookTitle, LocalDateTime startTime, LocalDateTime endTime);

    // 管理员订单搜索
    List<Order> findOrdersByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    List<Order> findOrdersByBookTitle(String bookTitle);
    List<Order> findOrdersByBookTitleAndTimeRange(String bookTitle, LocalDateTime startTime, LocalDateTime endTime);
    
    // 用户购书统计
    List<Order> findCompletedOrdersByUserAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    List<Order> findCompletedOrdersByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    // 分页相关方法
    // 用户分页获取订单
    Page<Order> findByUserOrderByCreateTimeDesc(User user, Pageable pageable);
    
    // 管理员分页获取所有订单
    Page<Order> findAllByOrderByCreateTimeDesc(Pageable pageable);
    
    // 用户分页搜索订单
    Page<Order> findUserOrdersByTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    Page<Order> findUserOrdersByBookTitle(Long userId, String bookTitle, Pageable pageable);
    Page<Order> findUserOrdersByBookTitleAndTimeRange(Long userId, String bookTitle, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    // 管理员分页搜索订单
    Page<Order> findOrdersByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    Page<Order> findOrdersByBookTitle(String bookTitle, Pageable pageable);
    Page<Order> findOrdersByBookTitleAndTimeRange(String bookTitle, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    // 用户购书统计分页
    Page<Order> findCompletedOrdersByUserAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    Page<Order> findCompletedOrdersByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}