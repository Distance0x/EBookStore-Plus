package com.ebookstore.ebookstorebackend.dto.mapper;

import com.ebookstore.ebookstorebackend.entity.Cart;
import com.ebookstore.ebookstorebackend.dto.CartDTO;
import org.springframework.stereotype.Component;


@Component
public class CartMapper {

    public CartDTO toDTO(Cart cart) {
        if (cart == null) {
            return null;
        }
        
        return new CartDTO(
            cart.getId(),
            cart.getBook().getId(),
            cart.getBook().getTitle(), // 使用 title 作为 name
            cart.getBook().getAuthor(),
            cart.getBook().getPrice(),
            cart.getBook().getCover(),
            cart.getQuantity(),
            cart.getAddedAt()
        );
    }


    public Cart toEntity(CartDTO cartDTO) {
        if (cartDTO == null) {
            return null;
        }
        
        Cart cart = new Cart();
        cart.setId(cartDTO.getId());
        cart.setQuantity(cartDTO.getQuantity());
        cart.setAddedAt(cartDTO.getAddedAt());

        
        return cart;
    }
}