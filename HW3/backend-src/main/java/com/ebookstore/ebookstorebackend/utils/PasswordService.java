package com.ebookstore.ebookstorebackend.utils;

/**
 * 密码加密和验证服务接口
 */
public interface PasswordService {
    
    /**
     * 对明文密码进行哈希加密
     * @param plainPassword 明文密码
     * @return 哈希后的密码
     */
    String hashPassword(String plainPassword);
    
    /**
     * 验证明文密码与哈希密码是否匹配
     * @param plainPassword 明文密码
     * @param hashedPassword 哈希密码
     * @return 是否匹配
     */
    boolean verifyPassword(String plainPassword, String hashedPassword);
}
