// src/main/java/com/ebookstore/ebookstorebackend/service/impl/BookServiceImpl.java
package com.ebookstore.ebookstorebackend.serviceimpl;

import com.ebookstore.ebookstorebackend.entity.Book;
import com.ebookstore.ebookstorebackend.dto.BookDTO;
import com.ebookstore.ebookstorebackend.dto.mapper.BookMapper;
import com.ebookstore.ebookstorebackend.dao.BookDao;
import com.ebookstore.ebookstorebackend.dao.TagDao;
import com.ebookstore.ebookstorebackend.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);
    
    private final BookDao bookDao;
    private final TagDao tagDao;
    private final BookMapper bookMapper;
    
    @Autowired
    public BookServiceImpl(BookDao bookDao, TagDao tagDao, BookMapper bookMapper) {
        this.bookDao = bookDao;
        this.tagDao = tagDao;
        this.bookMapper = bookMapper;
    }    // 用户端接口 - 只返回未删除的书籍
    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> getAllBooks() {
        List<Book> books = bookDao.findByDeletedFalseOrderByIdAsc();
        return books.stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> getBooksByPage(Pageable pageable) {
        Page<Book> bookPage = bookDao.findByDeletedFalse(pageable);
        return bookPage.map(bookMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BookDTO> getBookById(Long id) {
        Optional<Book> book = bookDao.findByIdAndDeletedFalse(id);
        return book.map(bookMapper::toDTO);
    }
      
    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> searchBooks(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return getAllBooks();
        }
          // 使用Set去重，因为书籍可能同时匹配标题和作者
        Set<Book> resultSet = new HashSet<>();
        resultSet.addAll(bookDao.findByTitleContainingAndDeletedFalseOrderByIdAsc(keyword));
        resultSet.addAll(bookDao.findByAuthorContainingAndDeletedFalseOrderByIdAsc(keyword));
        
        return resultSet.stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());
    }    
    // 购物车和订单用接口 - 可查询已删除的书籍    @Override
    @Transactional(readOnly = true)
    public Optional<BookDTO> getBookByIdIncludingDeleted(Long id) {
        Optional<Book> book = bookDao.findById(id);
        return book.map(bookMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> getBooksByIdsIncludingDeleted(List<Long> ids) {
        List<Book> books = bookDao.findByIdIn(ids);
        return books.stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public BookDTO createBook(BookDTO bookDTO) {
        Book book = bookMapper.toEntity(bookDTO);
        book.setId(null); // 确保是新建而不是更新
        book.setDeleted(false); // 新书默认不删除
        Book savedBook = bookDao.save(book);
        return bookMapper.toDTO(savedBook);
    }      
    
    @Override
    @Transactional
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        Book existingBook = bookDao.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在，ID: " + id));
        
        // 更新图书信息
        existingBook.setTitle(bookDTO.getTitle());
        existingBook.setAuthor(bookDTO.getAuthor());
        existingBook.setPrice(bookDTO.getPrice());
        existingBook.setCover(bookDTO.getCover());
        existingBook.setDescription(bookDTO.getDescription());
        existingBook.setIsbn(bookDTO.getIsbn());
        existingBook.setStock(bookDTO.getStock());
        existingBook.setPublisher(bookDTO.getPublisher());
        existingBook.setTags(bookDTO.getTags());  // 更新标签
        
        Book savedBook = bookDao.save(existingBook);
        return bookMapper.toDTO(savedBook);
    }      
    
    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookDao.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在，ID: " + id));
        book.markAsDeleted();
        bookDao.save(book);
    }
    
    // ========== 标签搜索功能 (Neo4j) ==========
    
    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> searchBooksByTag(String tagName) {
        // 1. 从 Neo4j (通过 TagDao) 获取该标签及 2 跳内关联的所有标签
        logger.info("开始按标签搜索书籍，标签: {}", tagName);
        List<String> relatedTags = tagDao.findRelatedTagsWithin2Hops(tagName);
        
        if (relatedTags.isEmpty()) {
            // 如果 Neo4j 中没有该标签，至少用原始标签搜索
            relatedTags = List.of(tagName);
            logger.warn("Neo4j 中未找到标签 [{}]，使用原始标签搜索", tagName);
        }
        
        // 2. 从 MySQL (通过 BookDao) 搜索包含这些标签的书籍
        logger.info("在 MySQL 中搜索包含标签 {} 的书籍", relatedTags);
        List<Book> books = bookDao.findByTagsIn(relatedTags);
        logger.info("MySQL 返回书籍数量: {}", books.size());
        
        // 3. 转换为 DTO 返回（MongoDB 数据已在 DAO 层填充）
        return books.stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        return tagDao.findAllTagNames();
    }
}