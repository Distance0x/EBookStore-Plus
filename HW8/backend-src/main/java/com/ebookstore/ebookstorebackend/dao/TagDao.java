package com.ebookstore.ebookstorebackend.dao;

import java.util.List;

/**
 * 标签数据访问接口 (Neo4j)
 */
public interface TagDao {
    
    /**
     * 查找指定标签及其 2 跳内关联的所有标签名称
     */
    List<String> findRelatedTagsWithin2Hops(String tagName);
    
    /**
     * 获取所有标签名称
     */
    List<String> findAllTagNames();
    
    /**
     * 查找某标签的直接子标签
     */
    List<String> findChildTags(String tagName);
    
    /**
     * 查找某标签的直接父标签
     */
    List<String> findParentTags(String tagName);
}
