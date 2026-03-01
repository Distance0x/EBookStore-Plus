package com.ebookstore.ebookstorebackend.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "user_auth")
public class UserAuth {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String account;

  @Column(nullable = false)
  private String password;


  public UserAuth(Long id, Long userId, String account, String password) {
    this.id = id;
    this.userId = userId;
    this.account = account;
    this.password = password;
  }

  public UserAuth() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
