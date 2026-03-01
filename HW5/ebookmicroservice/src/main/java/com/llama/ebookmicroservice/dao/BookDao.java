package com.llama.ebookmicroservice.dao;

import com.llama.ebookmicroservice.entity.Book;
import java.util.List;

public interface BookDao {
    // 精确匹配书名查询（未删除）
    List<Book> findByTitleAndDeletedFalse(String title);
    
    // 模糊匹配书名查询（未删除）
    List<Book> findByTitleContainingIgnoreCaseAndDeletedFalse(String title);
}

