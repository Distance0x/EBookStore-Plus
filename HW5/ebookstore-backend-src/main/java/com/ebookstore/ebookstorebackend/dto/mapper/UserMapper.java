package com.ebookstore.ebookstorebackend.dto.mapper;

import com.ebookstore.ebookstorebackend.entity.User;
import com.ebookstore.ebookstorebackend.dto.UserDTO;
import org.springframework.stereotype.Component;


@Component
public class UserMapper {    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserDTO(
            user.getName(),
            user.getId(),
            user.getAddress(),
            user.getEmail(),
            user.getPhone(),
            user.getAccount(),
            user.getRole(),
            user.getStatus(),
            user.getBalance()
        );
    }    public User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        
        User user = new User();
        user.setId(userDTO.getId());
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAddress(userDTO.getAddress());
        user.setAccount(userDTO.getAccount());
        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : User.Role.user);
        user.setStatus(userDTO.getStatus() != null ? userDTO.getStatus() : User.Status.active);
        user.setBalance(userDTO.getBalance() != null ? userDTO.getBalance() : new java.math.BigDecimal("100000.00"));
        
        return user;
    }
}