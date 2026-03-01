package com.ebookstore.ebookstorebackend.dao;

import com.ebookstore.ebookstorebackend.entity.BookStock;

import java.util.List;
import java.util.Optional;

public interface BookStockDao {
    
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
    
    /**
     * 保存或更新库存
     */
    BookStock save(BookStock bookStock);
    
    /**
     * 原子扣减库存（仅当库存足够时返回 true）
     * @param bookId 图书ID
     * @param quantity 扣减数量
     * @return 扣减成功返回 true，库存不足或记录不存在返回 false
     */
    boolean decrementStock(Long bookId, Integer quantity);
    
    /**
     * 原子增加库存
     * @param bookId 图书ID
     * @param quantity 增加数量
     * @return 增加成功返回 true，记录不存在返回 false
     */
    boolean incrementStock(Long bookId, Integer quantity);
}
