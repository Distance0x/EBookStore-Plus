package com.ebookstore.ebookstorebackend.daoimpl;

import com.ebookstore.ebookstorebackend.dao.TagDao;
import com.ebookstore.ebookstorebackend.repository.TagNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 标签数据访问实现 (Neo4j)
 * 使用 @Transactional 并指定 neo4jTransactionManager 来确保使用正确的事务管理器
 */
@Repository
@Transactional(transactionManager = "neo4jTransactionManager")
public class TagDaoImpl implements TagDao {
    
    private static final Logger logger = LoggerFactory.getLogger(TagDaoImpl.class);
    
    private final TagNodeRepository tagNodeRepository;
    
    @Autowired
    public TagDaoImpl(TagNodeRepository tagNodeRepository) {
        this.tagNodeRepository = tagNodeRepository;
    }
    
    @Override
    public List<String> findRelatedTagsWithin2Hops(String tagName) {
        logger.info("===== Neo4j: 查询标签 [{}] 的 2 跳关联标签 =====", tagName);
        
        try {
            List<String> relatedTags = tagNodeRepository.findRelatedTagsWithin2Hops(tagName);
            logger.info("Neo4j 返回关联标签: {}", relatedTags);
            return relatedTags;
        } catch (Exception e) {
            logger.error("Neo4j 查询失败: {}", e.getMessage());
            return Collections.singletonList(tagName);  // 失败时返回原标签
        }
    }
    
    @Override
    public List<String> findAllTagNames() {
        logger.info("===== Neo4j: 获取所有标签 =====");
        
        try {
            List<String> tags = tagNodeRepository.findAllTagNames();
            logger.info("Neo4j 返回标签数量: {}", tags.size());
            return tags;
        } catch (Exception e) {
            logger.error("Neo4j 查询失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<String> findChildTags(String tagName) {
        logger.info("===== Neo4j: 查询标签 [{}] 的子标签 =====", tagName);
        
        try {
            List<String> childTags = tagNodeRepository.findChildTags(tagName);
            logger.info("Neo4j 返回子标签: {}", childTags);
            return childTags;
        } catch (Exception e) {
            logger.error("Neo4j 查询失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<String> findParentTags(String tagName) {
        logger.info("===== Neo4j: 查询标签 [{}] 的父标签 =====", tagName);
        
        try {
            List<String> parentTags = tagNodeRepository.findParentTags(tagName);
            logger.info("Neo4j 返回父标签: {}", parentTags);
            return parentTags;
        } catch (Exception e) {
            logger.error("Neo4j 查询失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
