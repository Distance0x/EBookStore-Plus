package com.ebookstore.ebookstorebackend.repository;

import com.ebookstore.ebookstorebackend.entity.BookStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookStockRepository extends JpaRepository<BookStock, Long> {
    
    /**
     * 根据 bookId 查询库存
     */
    Optional<BookStock> findByBookId(Long bookId);
    
    /**
     * 批量查询库存（用于列表/分页拼装）
     */
    List<BookStock> findByBookIdIn(List<Long> bookIds);
    
    /**
     * 检查是否存在库存记录
     */
    boolean existsByBookId(Long bookId);
}
