package com.ebookstore.ebookstorebackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 默认用户配置属性类
 */
@Component
@ConfigurationProperties(prefix = "app.users")
public class DefaultUsersProperties {
    
    private DefaultUser defaultAdmin;
    private List<DefaultUser> defaultUsers;
    
    public static class DefaultUser {
        private String username;
        private String password;
        private String email;
        private String name;
        
        // Getters and Setters
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    // Getters and Setters
    public DefaultUser getDefaultAdmin() {
        return defaultAdmin;
    }
    
    public void setDefaultAdmin(DefaultUser defaultAdmin) {
        this.defaultAdmin = defaultAdmin;
    }
    
    public List<DefaultUser> getDefaultUsers() {
        return defaultUsers;
    }
    
    public void setDefaultUsers(List<DefaultUser> defaultUsers) {
        this.defaultUsers = defaultUsers;
    }
}
