package com.ebookstore.ebookstorebackend.controller;

import com.ebookstore.ebookstorebackend.dto.*;
import com.ebookstore.ebookstorebackend.entity.User;
import com.ebookstore.ebookstorebackend.service.BookService;
import com.ebookstore.ebookstorebackend.service.OrderService;
import com.ebookstore.ebookstorebackend.service.UserService;
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
@RequestMapping("/api/admin")
// @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AdminController {

    @Autowired
    private BookService bookService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;

    // 检查管理员权限的私有方法
    private boolean checkAdminPermission(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return false;
        }
        
        User user = (User) session.getAttribute("user");
        return user.getRole() == User.Role.admin;
    }    
    // 添加新图书
    @PostMapping("/books")
    public ResponseEntity<?> createBook(@RequestBody BookDTO bookDTO, HttpServletRequest request) {
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            BookDTO createdBook = bookService.createBook(bookDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // 更新图书信息
    @PutMapping("/books/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO, HttpServletRequest request) {
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            BookDTO updatedBook = bookService.updateBook(id, bookDTO);
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // 删除图书
    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id, HttpServletRequest request) {
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            bookService.deleteBook(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "图书删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }    
    // 获取所有用户
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 分页获取所有用户
    @GetMapping("/users/page")
    public ResponseEntity<?> getUsersByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<UserDTO> users = userService.getUsersByPage(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

      // 更新用户状态（统一的启用/禁用接口）
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long userId, @RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        if (!checkAdminPermission(httpRequest)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            String status = request.get("status");
            if ("ACTIVE".equals(status)) {
                userService.enableUserById(userId);
            } else if ("DISABLED".equals(status)) {
                userService.disableUserById(userId);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("error", "无效的状态值");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "用户状态已更新");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);        }
    }

    // 获取所有订单（分页）
    @GetMapping("/orders/page")
    public ResponseEntity<?> getAllOrdersByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderDTO> orders = orderService.getAllOrdersByPage(pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 获取所有订单
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders(HttpServletRequest request) {
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            List<OrderDTO> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 更新订单状态
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status, HttpServletRequest request) {
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // 管理员搜索订单（分页）
    @GetMapping("/orders/search/page")
    public ResponseEntity<?> searchAllOrdersByPage(
            @RequestParam(required = false) String bookTitle,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            // 转换时间参数
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime + "T23:59:59");
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderDTO> orders = orderService.searchAllOrdersByPage(bookTitle, startDateTime, endDateTime, pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 管理员搜索订单
    @GetMapping("/orders/search")
    public ResponseEntity<?> searchAllOrders(
            @RequestParam(required = false) String bookTitle,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            // 转换时间参数
            java.time.LocalDateTime startDateTime = null;
            java.time.LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime + "T23:59:59");
            }
            
            List<OrderDTO> orders = orderService.searchAllOrders(bookTitle, startDateTime, endDateTime);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }    // 管理员统计热门书籍销量（分页）
    @GetMapping("/statistics/books/page")
    public ResponseEntity<?> getBookSalesStatisticsByPage(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "totalSales") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {

        // 检查管理员权限
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            // 转换时间参数
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;

            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime + "T23:59:59");
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<BookSalesStatisticsDTO> statistics = orderService.getBookSalesStatisticsByPage(startDateTime, endDateTime, pageable);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取统计数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 管理员统计热门书籍销量
    @GetMapping("/statistics/books")
    public ResponseEntity<?> getBookSalesStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {

        // 检查管理员权限
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            // 转换时间参数
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;

            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime + "T23:59:59");
            }

            BookSalesStatisticsDTO statistics = orderService.getBookSalesStatistics(startDateTime, endDateTime);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取统计数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 管理员用户消费统计（分页）
    @GetMapping("/statistics/users/page")
    public ResponseEntity<?> getUserConsumptionStatisticsByPage(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "totalAmount") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {

        // 检查管理员权限
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            // 转换时间参数
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;

            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime + "T23:59:59");
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<UserConsumptionStatisticsDTO> statistics = orderService.getUserConsumptionStatisticsByPage(startDateTime, endDateTime, pageable);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取用户消费统计失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/statistics/users")
    public ResponseEntity<?> getUserConsumptionStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {

        // 检查管理员权限
        if (!checkAdminPermission(request)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "权限不足，需要管理员权限");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            // 转换时间参数
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;

            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime + "T23:59:59");
            }            UserConsumptionStatisticsDTO statistics = orderService.getUserConsumptionStatistics(startDateTime, endDateTime);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取用户消费统计失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 书籍销量统计（分页，包含全局统计信息）
     */
    @GetMapping("/statistics/books-sales-with-global")
    public ResponseEntity<?> getBookSalesStatisticsWithGlobal(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "revenue") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;

            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime + "T23:59:59");
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            StatisticsPageResponseDTO<BookSalesStatisticsDTO> statistics = orderService.getBookSalesStatisticsWithGlobal(startDateTime, endDateTime, pageable);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取统计数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 用户消费统计（分页，包含全局统计信息）
     */
    @GetMapping("/statistics/user-consumption-with-global")
    public ResponseEntity<?> getUserConsumptionStatisticsWithGlobal(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "totalSpent") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;

            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime + "T23:59:59");
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            StatisticsPageResponseDTO<UserConsumptionStatisticsDTO> statistics = orderService.getUserConsumptionStatisticsWithGlobal(startDateTime, endDateTime, pageable);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取用户消费统计失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
