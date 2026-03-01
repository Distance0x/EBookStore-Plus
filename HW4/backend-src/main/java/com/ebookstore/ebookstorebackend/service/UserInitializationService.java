package com.ebookstore.ebookstorebackend.service;

/**
 * 用户初始化服务接口
 */
public interface UserInitializationService {
    
    /**
     * 初始化默认用户（管理员和普通用户）
     * 在系统启动时调用，创建配置文件中定义的默认用户
     */
    void initializeDefaultUsers();
}
