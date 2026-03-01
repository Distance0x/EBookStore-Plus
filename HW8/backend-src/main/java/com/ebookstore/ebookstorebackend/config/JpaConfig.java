package com.ebookstore.ebookstorebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

/**
 * JPA 配置类
 * 将 JPA 事务管理器设置为 Primary，确保未指定事务管理器的 @Transactional 注解使用 JPA 事务管理器
 * 这样可以避免与 Neo4j 事务管理器冲突
 */
@Configuration
public class JpaConfig {

    /**
     * 配置 JPA 事务管理器为主要事务管理器
     * @param entityManagerFactory JPA EntityManagerFactory
     * @return JPA 事务管理器
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
