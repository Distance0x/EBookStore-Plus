package com.ebookstore.ebookstorebackend.entity;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.HashSet;
import java.util.Set;

@Node("Tag")
public class TagNode {
    
    @Id
    private String name;  // 标签名称作为唯一标识
    
    // 子标签关系（该标签包含哪些子分类）
    @Relationship(type = "PARENT_OF", direction = Relationship.Direction.OUTGOING)
    private Set<TagNode> children = new HashSet<>();
    
    public TagNode() {}
    
    public TagNode(String name) {
        this.name = name;
    }
    
    // Getter 和 Setter
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Set<TagNode> getChildren() {
        return children;
    }
    
    public void setChildren(Set<TagNode> children) {
        this.children = children;
    }
    
    public void addChild(TagNode child) {
        this.children.add(child);
    }
}