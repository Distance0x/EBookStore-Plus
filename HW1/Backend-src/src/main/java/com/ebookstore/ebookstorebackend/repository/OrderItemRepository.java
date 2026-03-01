package com.ebookstore.ebookstorebackend.repository;

import com.ebookstore.ebookstorebackend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    
    @Modifying
    @Query("DELETE FROM OrderItem oi WHERE oi.order.id = :orderId")
    void deleteByOrderId(Long orderId);
    
    @Modifying
    @Query("DELETE FROM OrderItem oi WHERE oi.order.user.id = :userId")
    void deleteByUserId(Long userId);
}