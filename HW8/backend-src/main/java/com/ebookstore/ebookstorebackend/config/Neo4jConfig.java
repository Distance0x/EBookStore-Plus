package com.ebookstore.ebookstorebackend.config;

import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Neo4j 配置类
 * 解决多数据源环境下的事务管理器冲突问题
 */
@Configuration
@EnableNeo4jRepositories(
    basePackages = "com.ebookstore.ebookstorebackend.repository",
    transactionManagerRef = "neo4jTransactionManager"
)
public class Neo4jConfig {
    
    /**
     * 指定使用 neo4j 数据库
     * 解决 "default database" 和 "neo4j" 数据库冲突的问题
     */
    @Bean
    public DatabaseSelectionProvider databaseSelectionProvider() {
        return DatabaseSelectionProvider.createStaticDatabaseSelectionProvider("neo4j");
    }
    
    /**
     * 创建 Neo4j 专用的事务管理器
     * 在多数据源环境中,明确指定 Neo4j 的事务管理器,避免与 JPA 的事务管理器冲突
     */
    @Bean(name = "neo4jTransactionManager")
    public PlatformTransactionManager neo4jTransactionManager(
            Driver driver,
            DatabaseSelectionProvider databaseSelectionProvider) {
        return new Neo4jTransactionManager(driver, databaseSelectionProvider);
    }
}
