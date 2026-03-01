package com.ebookstore.ebookstorebackend.repository;

import com.ebookstore.ebookstorebackend.entity.Order;
import com.ebookstore.ebookstorebackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByUserOrderByCreateTimeDesc(User user);
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // 删除用户所有订单
    @Modifying
    @Query("DELETE FROM Order o WHERE o.user.id = :userId")
    void deleteAllByUserId(Long userId);
    
    // 用户订单搜索查询
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi WHERE o.user.id = :userId AND o.createTime BETWEEN :startTime AND :endTime ORDER BY o.createTime DESC")
    List<Order> findUserOrdersByTimeRange(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi LEFT JOIN Book b ON oi.bookId = b.id WHERE o.user.id = :userId AND b.title LIKE %:bookTitle% ORDER BY o.createTime DESC")
    List<Order> findUserOrdersByBookTitle(@Param("userId") Long userId, @Param("bookTitle") String bookTitle);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi LEFT JOIN Book b ON oi.bookId = b.id WHERE o.user.id = :userId AND b.title LIKE %:bookTitle% AND o.createTime BETWEEN :startTime AND :endTime ORDER BY o.createTime DESC")
    List<Order> findUserOrdersByBookTitleAndTimeRange(@Param("userId") Long userId, @Param("bookTitle") String bookTitle, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 管理员订单搜索查询
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi WHERE o.createTime BETWEEN :startTime AND :endTime ORDER BY o.createTime DESC")
    List<Order> findOrdersByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi LEFT JOIN Book b ON oi.bookId = b.id WHERE b.title LIKE %:bookTitle% ORDER BY o.createTime DESC")
    List<Order> findOrdersByBookTitle(@Param("bookTitle") String bookTitle);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi LEFT JOIN Book b ON oi.bookId = b.id WHERE b.title LIKE %:bookTitle% AND o.createTime BETWEEN :startTime AND :endTime ORDER BY o.createTime DESC")
    List<Order> findOrdersByBookTitleAndTimeRange(@Param("bookTitle") String bookTitle, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    // 用户购书统计查询 - 获取已完成的订单用于统计
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = '已完成' AND (:startTime IS NULL OR o.createTime >= :startTime) AND (:endTime IS NULL OR o.createTime <= :endTime) ORDER BY o.createTime DESC")
    List<Order> findCompletedOrdersByUserAndTimeRange(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);    @Query("SELECT o FROM Order o WHERE o.status = '已完成' AND (:startTime IS NULL OR o.createTime >= :startTime) AND (:endTime IS NULL OR o.createTime <= :endTime) ORDER BY o.createTime DESC")
    List<Order> findCompletedOrdersByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 分页方法
    // 用户分页获取订单
    Page<Order> findByUserOrderByCreateTimeDesc(User user, Pageable pageable);
    
    // 管理员分页获取所有订单
    Page<Order> findAllByOrderByCreateTimeDesc(Pageable pageable);
    
    // 用户分页搜索订单
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi WHERE o.user.id = :userId AND o.createTime BETWEEN :startTime AND :endTime ORDER BY o.createTime DESC")
    Page<Order> findUserOrdersByTimeRange(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi LEFT JOIN Book b ON oi.bookId = b.id WHERE o.user.id = :userId AND b.title LIKE %:bookTitle% ORDER BY o.createTime DESC")
    Page<Order> findUserOrdersByBookTitle(@Param("userId") Long userId, @Param("bookTitle") String bookTitle, Pageable pageable);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi LEFT JOIN Book b ON oi.bookId = b.id WHERE o.user.id = :userId AND b.title LIKE %:bookTitle% AND o.createTime BETWEEN :startTime AND :endTime ORDER BY o.createTime DESC")
    Page<Order> findUserOrdersByBookTitleAndTimeRange(@Param("userId") Long userId, @Param("bookTitle") String bookTitle, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);
    
    // 管理员分页搜索订单
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi WHERE o.createTime BETWEEN :startTime AND :endTime ORDER BY o.createTime DESC")
    Page<Order> findOrdersByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi LEFT JOIN Book b ON oi.bookId = b.id WHERE b.title LIKE %:bookTitle% ORDER BY o.createTime DESC")
    Page<Order> findOrdersByBookTitle(@Param("bookTitle") String bookTitle, Pageable pageable);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderItems oi LEFT JOIN Book b ON oi.bookId = b.id WHERE b.title LIKE %:bookTitle% AND o.createTime BETWEEN :startTime AND :endTime ORDER BY o.createTime DESC")
    Page<Order> findOrdersByBookTitleAndTimeRange(@Param("bookTitle") String bookTitle, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);
    
    // 用户购书统计分页
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = '已完成' AND (:startTime IS NULL OR o.createTime >= :startTime) AND (:endTime IS NULL OR o.createTime <= :endTime) ORDER BY o.createTime DESC")
    Page<Order> findCompletedOrdersByUserAndTimeRange(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.status = '已完成' AND (:startTime IS NULL OR o.createTime >= :startTime) AND (:endTime IS NULL OR o.createTime <= :endTime) ORDER BY o.createTime DESC")
    Page<Order> findCompletedOrdersByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);
}
