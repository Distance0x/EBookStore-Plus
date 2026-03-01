package com.ebookstore.ebookstorebackend.controller;

import com.ebookstore.ebookstorebackend.dto.CartDTO;
import com.ebookstore.ebookstorebackend.entity.User;
import com.ebookstore.ebookstorebackend.service.CartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// map的命名  要有意义 定义接口的时候好操作
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CartController {
    @Autowired
    private CartService cartService;    @GetMapping
    public ResponseEntity<?> getCartItems(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        User user = (User) session.getAttribute("user");
        List<CartDTO> cartItems = cartService.getCartItems(user.getAccount());
        return ResponseEntity.ok(cartItems);
    }    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        try {
            User user = (User) session.getAttribute("user");
            Long bookId = Long.valueOf(request.get("bookId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            CartDTO result = cartService.addToCart(user.getAccount(), bookId, quantity);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("message", "添加到购物车成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
      @PutMapping("/update")
    public ResponseEntity<?> updateCartItem(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        try {
            User user = (User) session.getAttribute("user");
            Long bookId = Long.valueOf(request.get("bookId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            CartDTO result = cartService.updateCartItem(user.getAccount(), bookId, quantity);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("message", "购物车更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestParam Long bookId, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        User user = (User) session.getAttribute("user");
        cartService.removeFromCart(user.getAccount(), bookId);
        return ResponseEntity.ok().build();
    }    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        User user = (User) session.getAttribute("user");
        cartService.clearCart(user.getAccount());
        return ResponseEntity.ok().build();
        // 等价于return new ResponseEntity<>(HttpStatus.OK);  // 显式创建空响应
    }    
    // 检查购物车中是否有已删除的书籍
    @GetMapping("/check-deleted")
    public ResponseEntity<?> checkDeletedBooks(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        User user = (User) session.getAttribute("user");
        boolean hasDeleted = cartService.hasDeletedBooks(user.getAccount());
        List<Long> deletedBookIds = cartService.getDeletedBookIds(user.getAccount());
        
        Map<String, Object> response = new HashMap<>();
        response.put("hasDeletedBooks", hasDeleted);
        response.put("deletedBookIds", deletedBookIds);
        response.put("canCheckout", cartService.canCheckout(user.getAccount()));
        
        return ResponseEntity.ok(response);
    }    
    // 清理购物车中已删除的书籍
    @DeleteMapping("/remove-deleted")
    public ResponseEntity<?> removeDeletedBooks(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        User user = (User) session.getAttribute("user");
        cartService.removeDeletedBooks(user.getAccount());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "已删除的书籍已从购物车中移除");
          return ResponseEntity.ok(response);
    }

    // 检查购物车中的库存状态
    @GetMapping("/check-stock")
    public ResponseEntity<?> checkCartStock(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        User user = (User) session.getAttribute("user");
        Map<String, Object> stockInfo = cartService.checkCartStock(user.getAccount());
        
        return ResponseEntity.ok(stockInfo);
    }


}
