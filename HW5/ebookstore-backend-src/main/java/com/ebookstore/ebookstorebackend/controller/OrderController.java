package com.ebookstore.ebookstorebackend.controller;

import com.ebookstore.ebookstorebackend.dto.OrderDTO;
import com.ebookstore.ebookstorebackend.dto.PurchaseStatisticsDTO;
import com.ebookstore.ebookstorebackend.dto.StatisticsPageResponseDTO;
import com.ebookstore.ebookstorebackend.entity.User;
import com.ebookstore.ebookstorebackend.service.OrderService;
import com.ebookstore.ebookstorebackend.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
// @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderService orderService;
      @Autowired
    private OrderItemService orderItemService;
      // 从购物车创建订单
    @PostMapping("/create")
    public ResponseEntity<?> createOrderFromCart(
            @RequestParam String shippingAddress,
            @RequestParam String contactPhone,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = (User) session.getAttribute("user");
            OrderDTO order = orderService.createOrderFromCart(user.getId(), shippingAddress, contactPhone);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
      // 获取用户所有订单
    @GetMapping("/user")
    public ResponseEntity<?> getOrdersByUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = (User) session.getAttribute("user");
            List<OrderDTO> orders = orderService.getOrdersByUser(user);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // 获取订单详情
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        try {
            OrderDTO order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // 获取订单详情项
    @GetMapping("/{orderId}/items")
    public ResponseEntity<?> getOrderItems(@PathVariable Long orderId) {
        try {
            List<OrderDTO> orderItems = orderService.getOrderDetails(orderId);
            return ResponseEntity.ok(orderItems);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
      // 更新订单状态
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            OrderDTO order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // 删除单个订单
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "订单删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // 删除订单项
    @DeleteMapping("/items/{orderItemId}")
    public ResponseEntity<?> deleteOrderItem(@PathVariable Long orderItemId) {
        try {
            orderItemService.deleteOrderItem(orderItemId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "订单项删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    // 删除用户所有订单
    @DeleteMapping("/user/all")
    public ResponseEntity<?> deleteAllOrdersByUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = (User) session.getAttribute("user");
            orderService.deleteAllOrdersByUser(user.getId());
            Map<String, String> response = new HashMap<>();
            response.put("message", "用户所有订单已成功删除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // 支付订单
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long orderId, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            OrderDTO order = orderService.payOrder(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // 用户搜索订单
    @GetMapping("/search")
    public ResponseEntity<?> searchUserOrders(
            @RequestParam(required = false) String bookTitle,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = (User) session.getAttribute("user");
            
            // 将字符串时间转换为 LocalDateTime
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime + "T23:59:59");
            }
            
            List<OrderDTO> orders = orderService.searchUserOrders(user.getId(), bookTitle, startDateTime, endDateTime);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
      // 取消订单
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            OrderDTO order = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // 获取用户购买统计
    @GetMapping("/statistics")
    public ResponseEntity<?> getUserPurchaseStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = (User) session.getAttribute("user");
            
            // 将字符串时间转换为 LocalDateTime
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime + "T23:59:59");
            }
            
            PurchaseStatisticsDTO statistics = orderService.getUserPurchaseStatistics(user.getId(), startDateTime, endDateTime);
            return ResponseEntity.ok(statistics);        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // 更新订单联系信息
    @PutMapping("/{orderId}/contact")
    public ResponseEntity<?> updateOrderContactInfo(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            HttpSession session = httpRequest.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = (User) session.getAttribute("user");
            String address = request.get("address");
            String phone = request.get("phone");
            
            if (address == null || address.trim().isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "收货地址不能为空");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (phone == null || phone.trim().isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "联系电话不能为空");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 调用service层更新联系信息
            OrderDTO updatedOrder = orderService.updateOrderContactInfo(orderId, user.getId(), address, phone);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "联系信息更新成功");
            response.put("order", updatedOrder);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
      // 获取用户订单列表（分页）
    @GetMapping("")
    public ResponseEntity<?> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = (User) session.getAttribute("user");
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderDTO> orders = orderService.getUserOrdersByPage(user.getId(), pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }    // 用户搜索订单（分页）
    @GetMapping("/search/page")
    public ResponseEntity<?> searchUserOrdersByPage(
            @RequestParam(required = false) String bookTitle,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = (User) session.getAttribute("user");
            
            // 将字符串时间转换为 LocalDateTime
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime + "T23:59:59");
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderDTO> orders = orderService.searchUserOrdersByPage(user.getId(), bookTitle, startDateTime, endDateTime, pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 获取用户购买统计（分页）
    @GetMapping("/statistics/page")
    public ResponseEntity<?> getUserPurchaseStatisticsByPage(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = (User) session.getAttribute("user");
            
            // 将字符串时间转换为 LocalDateTime
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime + "T23:59:59");
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
              Page<PurchaseStatisticsDTO> statistics = orderService.getUserPurchaseStatisticsByPage(user.getId(), startDateTime, endDateTime, pageable);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 用户购买统计（分页，包含全局统计信息）
     */
    @GetMapping("/purchase-statistics-with-global")
    public ResponseEntity<?> getUserPurchaseStatisticsWithGlobal(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = (User) session.getAttribute("user");
            
            // 将字符串时间转换为 LocalDateTime
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime + "T23:59:59");
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            StatisticsPageResponseDTO<PurchaseStatisticsDTO> statistics = orderService.getUserPurchaseStatisticsWithGlobal(user.getId(), startDateTime, endDateTime, pageable);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}