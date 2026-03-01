package com.ebookstore.ebookstorebackend.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;

import com.ebookstore.ebookstorebackend.entity.User;
import com.ebookstore.ebookstorebackend.entity.UserAuth;
import com.ebookstore.ebookstorebackend.dto.UserDTO;
import com.ebookstore.ebookstorebackend.dto.mapper.UserMapper;
import com.ebookstore.ebookstorebackend.dao.UserDao;
import com.ebookstore.ebookstorebackend.dao.UserAuthDao;
import com.ebookstore.ebookstorebackend.service.UserService;
import com.ebookstore.ebookstorebackend.utils.PasswordService;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
// make it a Spring-managed Bean.
public class UserServiceImpl implements UserService{
  private final UserDao userDao;
  private final UserAuthDao userAuthDao;
  private final UserMapper userMapper;
  private final PasswordService passwordService;

  @Autowired
  public UserServiceImpl(UserDao userDao, UserAuthDao userAuthDao, UserMapper userMapper, PasswordService passwordService) {
    this.userDao = userDao;
    this.userAuthDao = userAuthDao;
    this.userMapper = userMapper;
    this.passwordService = passwordService;
  }  @Override
  public boolean authenticate(String username, String password){
    Optional<UserAuth> userAuthOpt = userAuthDao.findByAccount(username);
    if (userAuthOpt.isPresent()){
      UserAuth userAuth = userAuthOpt.get();
      // 使用密码服务验证哈希密码
      return passwordService.verifyPassword(password, userAuth.getPassword());
    }
    return false;
  }

  @Override
  public Optional<UserDTO> getUserProfile(String account){
    Optional<User> user = userDao.findByAccount(account);
    return user.map(userMapper::toDTO);
  }

  @Override
  @Transactional
  public UserDTO updateUserProfile(String account, UserDTO userDTO){
    User existingUser = userDao.findByAccount(account)
    .orElseThrow(() -> new RuntimeException("User not found"));
        
    // 只更新允许修改的字段
    existingUser.setName(userDTO.getName());
    existingUser.setEmail(userDTO.getEmail());
    existingUser.setPhone(userDTO.getPhone());
    existingUser.setAddress(userDTO.getAddress());
        
    User updatedUser = userDao.save(existingUser);
    return userMapper.toDTO(updatedUser);
  }    @Override
    @Transactional
    public UserDTO createUser(String username, String password, String email) {
        try {
            // 再次检查用户名是否已存在（双重检查）
            if (userDao.findByAccount(username).isPresent()) {
                throw new RuntimeException("用户名已存在");
            }
            
            // 检查UserAuth表中用户名是否已存在
            if (userAuthDao.findByAccount(username).isPresent()) {
                throw new RuntimeException("用户名已存在");
            }
            
            // 检查邮箱是否已存在
            if (userDao.findByEmail(email).isPresent()) {
                throw new RuntimeException("邮箱已被注册");
            }
              // 创建新用户实体（User表）
            User user = new User();
            user.setAccount(username);
            user.setName(username); // 默认使用用户名作为显示名称
            user.setEmail(email);
            user.setPhone("");  // 默认空值
            user.setAddress(""); // 默认空值
            user.setRole(User.Role.user); // 注册用户默认为普通用户
            user.setStatus(User.Status.active); // 默认为活跃状态
            // 注意：User表不存储密码
            
            // 保存用户到User表
            User savedUser = userDao.save(user);
            System.out.println("用户信息保存成功，ID: " + savedUser.getId());
              // 创建认证信息实体（UserAuth表）
            UserAuth userAuth = new UserAuth();
            userAuth.setUserId(savedUser.getId()); // 关联到User表的ID
            userAuth.setAccount(username);
            // 使用密码服务对密码进行哈希加密
            String hashedPassword = passwordService.hashPassword(password);
            userAuth.setPassword(hashedPassword);
            
            // 保存认证信息到UserAuth表
            UserAuth savedUserAuth = userAuthDao.save(userAuth);
            System.out.println("用户认证信息保存成功，ID: " + savedUserAuth.getId());
              // 转换为DTO返回
            UserDTO userDTO = new UserDTO();
            userDTO.setId(savedUser.getId());
            userDTO.setAccount(savedUser.getAccount());
            userDTO.setName(savedUser.getName());
            userDTO.setEmail(savedUser.getEmail());
            userDTO.setPhone(savedUser.getPhone());
            userDTO.setAddress(savedUser.getAddress());
            userDTO.setRole(savedUser.getRole());
            userDTO.setStatus(savedUser.getStatus());
            
            return userDTO;
            
        } catch (Exception e) {
            System.out.println("创建用户失败: " + e.getMessage());
            throw new RuntimeException("创建用户失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isEmailExists(String email) {
        return userDao.findByEmail(email).isPresent();
    }    @Override
    public boolean isUsernameExists(String username) {
        return userDao.findByAccount(username).isPresent();
    }

    // 新增管理员相关方法实现
    @Override
    public boolean isAdmin(String account) {
        Optional<User> userOpt = userDao.findByAccount(account);
        return userOpt.isPresent() && userOpt.get().isAdmin();
    }

    @Override
    public boolean isUserActive(String account) {
        Optional<User> userOpt = userDao.findByAccount(account);
        return userOpt.isPresent() && userOpt.get().isActive();    }

    @Override
    @Transactional
    public void disableUserById(Long userId) {
        User user = userDao.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(User.Status.disabled);
        userDao.save(user);
    }

    @Override
    @Transactional
    public void enableUserById(Long userId) {
        User user = userDao.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(User.Status.active);
        userDao.save(user);
    }    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userDao.findAll();
        return users.stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public Page<UserDTO> getUsersByPage(Pageable pageable) {
        Page<User> users = userDao.findAll(pageable);
        return users.map(userMapper::toDTO);
    }

}
