package com.llama.ebookmicroservice.repository;

import com.llama.ebookmicroservice.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    // 精确匹配书名查询作者（未删除）
    List<Book> findByTitleAndDeletedFalse(String title);

    // 模糊匹配书名查询作者（未删除）
    List<Book> findByTitleContainingIgnoreCaseAndDeletedFalse(String title);

}

