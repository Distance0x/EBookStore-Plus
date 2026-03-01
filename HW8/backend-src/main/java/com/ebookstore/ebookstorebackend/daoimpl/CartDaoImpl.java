package com.ebookstore.ebookstorebackend.daoimpl;

import com.ebookstore.ebookstorebackend.dao.CartDao;
import com.ebookstore.ebookstorebackend.entity.Cart;
import com.ebookstore.ebookstorebackend.entity.User;
import com.ebookstore.ebookstorebackend.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CartDaoImpl implements CartDao {
    
    private final CartRepository cartRepository;
    
    @Autowired
    public CartDaoImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    
    @Override
    public List<Cart> findByUser(User user) {
        return cartRepository.findByUser(user);
    }
    
    @Override
    public Optional<Cart> findByUserIdAndBookId(Long userId, Long bookId) {
        return cartRepository.findByUserIdAndBookId(userId, bookId);
    }
    
    @Override
    public Cart save(Cart cart) {
        return cartRepository.save(cart);
    }
    
    @Override
    public void deleteByUserIdAndBookId(Long userId, Long bookId) {
        cartRepository.deleteByUserIdAndBookId(userId, bookId);
    }
    
    @Override
    public void deleteByUserId(Long userId) {
        cartRepository.deleteByUserId(userId);
    }
}