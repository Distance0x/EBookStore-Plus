package com.ebookstore.ebookstorebackend.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheConfig.class);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // JSON 序列化配置
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        Jackson2JsonRedisSerializer<Object> serializer = 
            new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        
        // 缓存配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(2))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(serializer)
            )
            .disableCachingNullValues();
        
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
        
        // 包装 CacheManager 添加缓存命中日志
        return new CacheManager() {
            @Override
            public Cache getCache(String name) {
                Cache cache = redisCacheManager.getCache(name);
                return cache == null ? null : new Cache() {
                    @Override
                    public String getName() {
                        return cache.getName();
                    }
                    
                    @Override
                    public Object getNativeCache() {
                        return cache.getNativeCache();
                    }
                    
                    @Override
                    public ValueWrapper get(Object key) {
                        ValueWrapper result = cache.get(key);
                        if (result != null) {
                            logger.info("[CACHE HIT] Cache: '{}', Key: '{}'", name, key);
                        }
                        return result;
                    }
                    
                    @Override
                    public <T> T get(Object key, Class<T> type) {
                        T result = cache.get(key, type);
                        if (result != null) {
                            logger.info("[CACHE HIT] Cache: '{}', Key: '{}'", name, key);
                        }
                        return result;
                    }
                    
                    @Override
                    public <T> T get(Object key, java.util.concurrent.Callable<T> valueLoader) {
                        return cache.get(key, valueLoader);
                    }
                    
                    @Override
                    public void put(Object key, Object value) {
                        cache.put(key, value);
                    }
                    
                    @Override
                    public ValueWrapper putIfAbsent(Object key, Object value) {
                        return cache.putIfAbsent(key, value);
                    }
                    
                    @Override
                    public void evict(Object key) {
                        cache.evict(key);
                    }
                    
                    @Override
                    public void clear() {
                        cache.clear();
                    }
                };
            }
            
            @Override
            public java.util.Collection<String> getCacheNames() {
                return redisCacheManager.getCacheNames();
            }
        };
    }
    
    /**
     * Redis 异常处理器 - 当 Redis 不可用时自动降级到数据库
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                logger.warn("[REDIS ERROR] Cache GET failed, fallback to DB. Cache: '{}', Key: '{}', Error: {}", 
                    cache.getName(), key, exception.getMessage());
                // 不抛出异常，让方法继续执行查询数据库
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                logger.warn("[REDIS ERROR] Cache PUT failed, continue without cache. Cache: '{}', Key: '{}', Error: {}", 
                    cache.getName(), key, exception.getMessage());
                // 不抛出异常，数据库操作正常进行
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                logger.warn("[REDIS ERROR] Cache EVICT failed, continue without cache. Cache: '{}', Key: '{}', Error: {}", 
                    cache.getName(), key, exception.getMessage());
                // 不抛出异常，数据库操作正常进行
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                logger.warn("[REDIS ERROR] Cache CLEAR failed, continue without cache. Cache: '{}', Error: {}", 
                    cache.getName(), exception.getMessage());
                // 不抛出异常，数据库操作正常进行
            }
        };
    }
}