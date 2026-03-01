package com.ebookstore.ebookstorebackend.dao;

import com.ebookstore.ebookstorebackend.entity.Cart;
import com.ebookstore.ebookstorebackend.entity.User;
import java.util.List;
import java.util.Optional;

public interface CartDao {
    List<Cart> findByUser(User user);
    Optional<Cart> findByUserIdAndBookId(Long userId, Long bookId);
    Cart save(Cart cart);
    void deleteByUserIdAndBookId(Long userId, Long bookId);
    void deleteByUserId(Long userId);
}