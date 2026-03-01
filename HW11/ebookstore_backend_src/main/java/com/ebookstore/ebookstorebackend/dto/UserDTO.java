package com.ebookstore.ebookstorebackend.dto;

import com.ebookstore.ebookstorebackend.entity.User;
import java.math.BigDecimal;

public class UserDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;    private String account;
    private User.Role role;
    private User.Status status;
    private BigDecimal balance;
    public UserDTO() {
        // Default constructor
    }    public UserDTO(String name, Long id, String address, String email, String phone, String account) {
        this.name = name;
        this.id = id;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.account = account;
        this.role = User.Role.user; // 默认为普通用户
        this.status = User.Status.active; // 默认为活跃状态
        this.balance = new BigDecimal("100000.00"); // 默认余额
    }    public UserDTO(String name, Long id, String address, String email, String phone, String account, User.Role role, User.Status status) {
        this.name = name;
        this.id = id;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.account = account;
        this.role = role;
        this.status = status;
        this.balance = new BigDecimal("100000.00"); // 默认余额
    }
    
    public UserDTO(String name, Long id, String address, String email, String phone, String account, 
                   User.Role role, User.Status status, BigDecimal balance) {
        this.name = name;
        this.id = id;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.account = account;
        this.role = role;
        this.status = status;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }    
    public String getAccount() {
        return account;
    }    public void setAccount(String account) {
        this.account = account;
    }

    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }

    public User.Status getStatus() {
        return status;
    }    public void setStatus(User.Status status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    // 便利方法
    public boolean isAdmin() {
        return this.role == User.Role.admin;
    }

    public boolean isActive() {
        return this.status == User.Status.active;
    }
      // 将DTO转换为实体对象
    public User getUser() {
        User user = new User();
        user.setId(this.id);
        user.setName(this.name);
        user.setEmail(this.email);
        user.setPhone(this.phone);
        user.setAddress(this.address);
        user.setAccount(this.account);
        user.setRole(this.role != null ? this.role : User.Role.user);
        user.setStatus(this.status != null ? this.status : User.Status.active);
        user.setBalance(this.balance != null ? this.balance : new BigDecimal("100000.00"));
        return user;
    }
}
