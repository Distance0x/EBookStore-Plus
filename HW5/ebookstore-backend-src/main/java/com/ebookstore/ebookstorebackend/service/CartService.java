package com.ebookstore.ebookstorebackend.service;

import com.ebookstore.ebookstorebackend.dto.CartDTO;
import java.util.List;
import java.util.Map;

public interface CartService {

    List<CartDTO> getCartItems(String account);

    CartDTO addToCart(String account, Long bookId, Integer quantity);

    // 更新购物车中的图书数量
    CartDTO updateCartItem(String account, Long bookId, Integer quantity);

    void removeFromCart(String account, Long bookId);

    void clearCart(String account);
    
    // 新增：检查购物车中是否有已删除的书籍
    boolean hasDeletedBooks(String account);
    
    // 新增：获取购物车中已删除书籍的列表
    List<Long> getDeletedBookIds(String account);
    
    // 新增：清理购物车中已删除的书籍
    void removeDeletedBooks(String account);
      // 新增：验证购物车是否可以下单（没有已删除的书籍）
    boolean canCheckout(String account);
    
    // 新增：检查购物车中的库存状态
    Map<String, Object> checkCartStock(String account);
}
