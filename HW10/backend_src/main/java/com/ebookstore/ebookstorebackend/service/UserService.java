package com.ebookstore.ebookstorebackend.service;
import com.ebookstore.ebookstorebackend.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

public interface UserService {
  boolean authenticate(String username, String password);
  Optional<UserDTO> getUserProfile(String account);
  UserDTO updateUserProfile(String account, UserDTO user);
  UserDTO createUser(String username, String password, String email);
  boolean isEmailExists(String email);
  boolean isUsernameExists(String username);
    // 管理员相关方法
  boolean isAdmin(String account);
  boolean isUserActive(String account);  void disableUserById(Long userId); // 基于ID禁用用户
  void enableUserById(Long userId); // 基于ID启用用户
  List<UserDTO> getAllUsers(); // 管理员获取所有用户
  Page<UserDTO> getUsersByPage(Pageable pageable); // 分页获取所有用户
}
