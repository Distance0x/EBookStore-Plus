package com.ebookstore.ebookstorebackend.repository;

import com.ebookstore.ebookstorebackend.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {    // 原有方法：搜索所有书籍（包括已删除的）
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    // 用户端：获取所有未删除的书籍（按ID升序）
    List<Book> findByDeletedFalseOrderByIdAsc();
    
    // 用户端：分页查询未删除的书籍
    Page<Book> findByDeletedFalse(Pageable pageable);
    
    // 搜索未删除的书籍（按标题）
    List<Book> findByTitleContainingAndDeletedFalseOrderByIdAsc(String title);
    
    // 搜索未删除的书籍（按作者）
    List<Book> findByAuthorContainingAndDeletedFalseOrderByIdAsc(String author);
    
    // 获取未删除的单本书籍（用户端详情页）
    Optional<Book> findByIdAndDeletedFalse(Long id);
    
    // 批量获取书籍（包括已删除的，用于购物车和订单）
    List<Book> findByIdIn(List<Long> ids);
}