package com.ebookstore.ebookstorebackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;


@Entity
@Table(name = "user")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;
    
  @Column(nullable = false, unique = true)
  private String email;
    
  @Column
  private String phone;
    
  @Column(columnDefinition = "TEXT")
  private String address;
    
  @Column(nullable = false, unique = true)
  private String account;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role = Role.user;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status = Status.active;

  @Column(nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 100000.00")
  private BigDecimal balance = new BigDecimal("100000.00");

  // 枚举定义
  public enum Role {
    user, admin
  }

  public enum Status {
    active, disabled
  }
  // 无参构造函数
  public User() {
  }
  // 带参构造函数
  public User(String name, String email, String phone, String address, String account) {
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.address = address;
    this.account = account;
    this.role = Role.user; // 默认为普通用户
    this.status = Status.active; // 默认为活跃状态
    this.balance = new BigDecimal("100000.00"); // 默认余额
  }
  // 管理员构造函数
  public User(String name, String email, String phone, String address, String account, Role role) {
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.address = address;
    this.account = account;
    this.role = role;
    this.status = Status.active;
    this.balance = new BigDecimal("100000.00"); // 默认余额
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
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public Status getStatus() {
    return status;
  }
  public void setStatus(Status status) {
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
    return this.role == Role.admin;
  }

  public boolean isActive() {
    return this.status == Status.active;
  }

  public boolean isDisabled() {
    return this.status == Status.disabled;
  }
}
