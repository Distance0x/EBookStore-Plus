package com.ebookstore.ebookstorebackend.repository;

import com.ebookstore.ebookstorebackend.entity.TagNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TagNodeRepository extends Neo4jRepository<TagNode, String> {
    
    /**
     * 核心查询：找到指定标签及其 2 跳内关联的所有标签名称
     * [*0..2] 表示 0 到 2 跳的路径（包含自己）
     */
    @Query("MATCH (t:Tag {name: $tagName})-[*0..2]-(related:Tag) " +
           "RETURN DISTINCT related.name")
    List<String> findRelatedTagsWithin2Hops(String tagName);
    
    /**
     * 获取所有标签名称（供前端展示标签列表）
     */
    @Query("MATCH (t:Tag) RETURN t.name ORDER BY t.name")
    List<String> findAllTagNames();
    
    /**
     * 查找某标签的直接子标签
     */
    @Query("MATCH (t:Tag {name: $tagName})-[:PARENT_OF]->(child:Tag) " +
           "RETURN child.name")
    List<String> findChildTags(String tagName);
    
    /**
     * 查找某标签的直接父标签
     */
    @Query("MATCH (t:Tag {name: $tagName})<-[:PARENT_OF]-(parent:Tag) " +
           "RETURN parent.name")
    List<String> findParentTags(String tagName);
    
    /**
     * 根据名称查找标签
     */
    Optional<TagNode> findByName(String name);
}