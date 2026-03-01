package com.ebookstore.ebookstorebackend.serviceimpl;

import com.ebookstore.ebookstorebackend.dao.UserDao;
import com.ebookstore.ebookstorebackend.dao.UserAuthDao;
import com.ebookstore.ebookstorebackend.entity.User;
import com.ebookstore.ebookstorebackend.entity.UserAuth;
import com.ebookstore.ebookstorebackend.utils.PasswordService;
import com.ebookstore.ebookstorebackend.service.UserInitializationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 简化的用户初始化服务
 */
@Service
public class UserInitializationServiceImpl implements UserInitializationService, CommandLineRunner {
    
    
    private final PasswordService passwordService;
    private final UserDao userDao;
    private final UserAuthDao userAuthDao;
    
    @Autowired
    public UserInitializationServiceImpl(PasswordService passwordService, UserDao userDao, UserAuthDao userAuthDao) {
        this.passwordService = passwordService;
        this.userDao = userDao;
        this.userAuthDao = userAuthDao;
    }
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultUsers();
    }
    
    @Override
    @Transactional
    public void initializeDefaultUsers() {
        // 检查管理员是否已存在
        if (userDao.findByAccount("admin").isEmpty()) {
            createDefaultAdmin();
        }
        else System.out.println("admin已存在");
    }
    
    private void createDefaultAdmin() {
        try {
            // 创建管理员用户
            User admin = new User();
            admin.setAccount("admin");
            admin.setName("管理员");
            admin.setEmail("admin@ebookstore.com");
            admin.setPhone("18099995555");
            admin.setAddress("东川路800号");
            admin.setRole(User.Role.admin);
            admin.setStatus(User.Status.active);
            admin.setBalance(new BigDecimal("999999.00"));
            
            User savedAdmin = userDao.save(admin);
            
            // 创建管理员认证信息
            UserAuth adminAuth = new UserAuth();
            adminAuth.setUserId(savedAdmin.getId());
            adminAuth.setAccount("admin");
            adminAuth.setPassword(passwordService.hashPassword("admin123"));
            
            userAuthDao.save(adminAuth);
            
            System.out.println("默认管理员创建成功: " + "admin");
        } catch (Exception e) {
            System.err.println("创建默认管理员失败: " + e.getMessage());
        }
    }
}
