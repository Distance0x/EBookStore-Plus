package com.ebookstore.ebookstorebackend.service;

import com.ebookstore.ebookstorebackend.dto.OrderDTO;
import com.ebookstore.ebookstorebackend.dto.PurchaseStatisticsDTO;
import com.ebookstore.ebookstorebackend.entity.User;
import com.ebookstore.ebookstorebackend.dto.BookSalesStatisticsDTO;
import com.ebookstore.ebookstorebackend.dto.UserConsumptionStatisticsDTO;
import com.ebookstore.ebookstorebackend.dto.StatisticsPageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    OrderDTO createOrderFromCart(Long userId, String shippingAddress, String contactPhone);
    OrderDTO getOrderById(Long orderId);
    OrderDTO getOrderByOrderNumber(String orderNumber);
    List<OrderDTO> getOrdersByUser(User user);
    OrderDTO updateOrderStatus(Long orderId, String status);
    void deleteOrder(Long orderId);
    List<OrderDTO> getOrderDetails(Long orderId);
    
    // 删除用户所有订单
    void deleteAllOrdersByUser(Long userId);
    
    // 支付订单 - 扣减库存和用户余额
    OrderDTO payOrder(Long orderId);
    
    // 管理员功能 - 获取所有订单
    List<OrderDTO> getAllOrders();
    
    // 用户搜索订单功能
    List<OrderDTO> searchUserOrders(Long userId, String bookTitle, LocalDateTime startTime, LocalDateTime endTime);
    
    // 管理员搜索订单功能
    List<OrderDTO> searchAllOrders(String bookTitle, LocalDateTime startTime, LocalDateTime endTime);
      // 取消订单功能 - 只允许取消未支付订单，取消后恢复库存
    OrderDTO cancelOrder(Long orderId);
    
    // 更新订单联系信息 - 只允许修改未支付订单的联系信息
    OrderDTO updateOrderContactInfo(Long orderId, Long userId, String address, String phone);
    
    // 购书统计功能
    PurchaseStatisticsDTO getUserPurchaseStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    BookSalesStatisticsDTO getBookSalesStatistics(LocalDateTime startTime, LocalDateTime endTime);

    // 管理员用户消费统计
    UserConsumptionStatisticsDTO getUserConsumptionStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    // 分页相关方法
    // 用户获取订单列表（分页）
    Page<OrderDTO> getUserOrdersByPage(Long userId, Pageable pageable);
    
    // 用户搜索订单（分页）
    Page<OrderDTO> searchUserOrdersByPage(Long userId, String bookTitle, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    // 用户购买统计（分页）
    Page<PurchaseStatisticsDTO> getUserPurchaseStatisticsByPage(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    // 管理员获取所有订单（分页）
    Page<OrderDTO> getAllOrdersByPage(Pageable pageable);
    
    // 管理员搜索订单（分页）
    Page<OrderDTO> searchAllOrdersByPage(String bookTitle, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    // 管理员书籍销量统计（分页）
    Page<BookSalesStatisticsDTO> getBookSalesStatisticsByPage(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
      // 管理员用户消费统计（分页）
    Page<UserConsumptionStatisticsDTO> getUserConsumptionStatisticsByPage(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
      // 带全局统计信息的方法
    // 用户购买统计（分页，包含全局统计）
    StatisticsPageResponseDTO<PurchaseStatisticsDTO> getUserPurchaseStatisticsWithGlobal(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    // 管理员书籍销量统计（分页，包含全局统计）
    StatisticsPageResponseDTO<BookSalesStatisticsDTO> getBookSalesStatisticsWithGlobal(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    // 管理员用户消费统计（分页，包含全局统计）
    StatisticsPageResponseDTO<UserConsumptionStatisticsDTO> getUserConsumptionStatisticsWithGlobal(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}