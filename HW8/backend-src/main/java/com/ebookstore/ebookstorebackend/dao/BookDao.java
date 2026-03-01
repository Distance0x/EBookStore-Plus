package com.ebookstore.ebookstorebackend.dao;

import com.ebookstore.ebookstorebackend.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface BookDao {
    List<Book> findAll();
    Page<Book> findAll(Pageable pageable);
    Optional<Book> findById(Long id);
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    Book save(Book book);
    void deleteById(Long id);
    
    // 软删除相关方法
    List<Book> findByDeletedFalseOrderByIdAsc();
    Page<Book> findByDeletedFalse(Pageable pageable);
    List<Book> findByTitleContainingAndDeletedFalseOrderByIdAsc(String title);
    List<Book> findByAuthorContainingAndDeletedFalseOrderByIdAsc(String author);
    Optional<Book> findByIdAndDeletedFalse(Long id);
    List<Book> findByIdIn(List<Long> ids);

    /**
    * 根据标签列表搜索书籍
    */
    List<Book> findByTagsIn(List<String> tags);
}