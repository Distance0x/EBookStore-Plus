// src/main/java/com/ebookstore/ebookstorebackend/service/impl/BookServiceImpl.java
package com.ebookstore.ebookstorebackend.serviceimpl;

import com.ebookstore.ebookstorebackend.entity.Book;
import com.ebookstore.ebookstorebackend.entity.BookStock;
import com.ebookstore.ebookstorebackend.dto.BookDTO;
import com.ebookstore.ebookstorebackend.dto.mapper.BookMapper;
import com.ebookstore.ebookstorebackend.dao.BookDao;
import com.ebookstore.ebookstorebackend.dao.BookStockDao;
import com.ebookstore.ebookstorebackend.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Redis 导入库
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

// 日志
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);
    
    private final BookDao bookDao;
    private final BookMapper bookMapper;
    private final BookStockDao bookStockDao;
    
    @Autowired
    public BookServiceImpl(BookDao bookDao, BookMapper bookMapper, BookStockDao bookStockDao) {
        this.bookDao = bookDao;
        this.bookMapper = bookMapper;
        this.bookStockDao = bookStockDao;
    }
    
    /**
     * 为单个 BookDTO 拼装库存
     */
    private void fillStock(BookDTO dto) {
        if (dto == null || dto.getId() == null) return;
        Integer stock = bookStockDao.findByBookId(dto.getId())
                .map(BookStock::getStock)
                .orElse(0);
        dto.setStock(stock);
    }
    
    /**
     * 批量拼装库存（用于列表/分页）
     */
    private void fillStockBatch(List<BookDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return;
        List<Long> ids = dtos.stream().map(BookDTO::getId).collect(Collectors.toList());
        Map<Long, Integer> stockMap = bookStockDao.findByBookIdIn(ids).stream()
                .collect(Collectors.toMap(BookStock::getBookId, BookStock::getStock));
        dtos.forEach(dto -> dto.setStock(stockMap.getOrDefault(dto.getId(), 0)));
    }    // 用户端接口 - 只返回未删除的书籍

    /**
     * 查询所有书籍 - 缓存基础信息，库存实时填充
     * 缓存键：bookList::all
     * 注意：缓存的 BookDTO.stock 为 null，返回前动态填充
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "bookList", key = "'all'")
    public List<BookDTO> getAllBooks() {
        logger.info("[BOOK LIST CACHE MISS] Querying DB for all books");
        List<Book> books = bookDao.findByDeletedFalseOrderByIdAsc();
        List<BookDTO> dtos = books.stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());
        // ⚠️ 不在这里 fillStockBatch，缓存中 stock = null
        fillStockBatch(dtos);
        return dtos;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> getBooksByPage(Pageable pageable) {
        Page<Book> bookPage = bookDao.findByDeletedFalse(pageable);
        Page<BookDTO> dtoPage = bookPage.map(bookMapper::toDTO);
        fillStockBatch(dtoPage.getContent());
        return dtoPage;
    }
    
    /**
     * 查询单本书详情 - 缓存基础信息，库存实时填充
     * 缓存键：book::书籍ID
     * 注意：缓存的 BookDTO.stock 为 null，返回前动态填充
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "book", key = "#id", unless = "#result == null")
    public BookDTO getBookById(Long id) {
        logger.info("[BOOK CACHE MISS] Querying DB for bookId: {}", id);
        Book book = bookDao.findByIdAndDeletedFalse(id).orElse(null);
        if (book == null) {
            logger.warn("[BOOK NOT FOUND] bookId: {}", id);
            return null;
        }
        BookDTO dto = bookMapper.toDTO(book);
        fillStock(dto);
        return dto;
    }
    /**
     * 搜索书籍 - 缓存基础信息，库存实时填充
     * 缓存键：bookSearch::关键词
     * 注意：缓存的 BookDTO.stock 为 null，返回前动态填充
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "bookSearch", key = "#keyword", unless = "#result == null || #result.isEmpty()")
    public List<BookDTO> searchBooks(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return getAllBooks();
        }
        logger.info("[BOOK SEARCH CACHE MISS] Searching DB for keyword: '{}'", keyword);
        
        // 使用Set去重，因为书籍可能同时匹配标题和作者
        Set<Book> resultSet = new HashSet<>();
        resultSet.addAll(bookDao.findByTitleContainingAndDeletedFalseOrderByIdAsc(keyword));
        resultSet.addAll(bookDao.findByAuthorContainingAndDeletedFalseOrderByIdAsc(keyword));
        
        List<BookDTO> dtos = resultSet.stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());
        // 🔄 返回前实时填充库存
        fillStockBatch(dtos);
        return dtos;
    }    
    // 购物车和订单用接口 - 可查询已删除的书籍    @Override
    @Transactional(readOnly = true)
    public Optional<BookDTO> getBookByIdIncludingDeleted(Long id) {
        Optional<Book> book = bookDao.findById(id);
        Optional<BookDTO> dto = book.map(bookMapper::toDTO);
        dto.ifPresent(this::fillStock);
        return dto;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> getBooksByIdsIncludingDeleted(List<Long> ids) {
        List<Book> books = bookDao.findByIdIn(ids);
        List<BookDTO> dtos = books.stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());
        fillStockBatch(dtos);
        return dtos;
    }
    
    /**
     * 创建书籍 - 清除列表和搜索缓存
     */
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "bookList", allEntries = true),
        @CacheEvict(value = "bookSearch", allEntries = true)
    })
    public BookDTO createBook(BookDTO bookDTO) {
        logger.info("[BOOK CREATE] Creating new book: {}", bookDTO.getTitle());
        
        Book book = bookMapper.toEntity(bookDTO);
        book.setId(null); // 确保是新建而不是更新
        book.setDeleted(false); // 新书默认不删除
        Book savedBook = bookDao.save(book);
        
        // 同步创建库存记录
        Integer initialStock = bookDTO.getStock() != null ? bookDTO.getStock() : 0;
        BookStock bookStock = new BookStock(savedBook.getId(), initialStock);
        bookStockDao.save(bookStock);
        
        BookDTO result = bookMapper.toDTO(savedBook);
        result.setStock(initialStock);
        
        logger.info("[CACHE EVICT] Cleared list/search caches due to new book: {}", savedBook.getId());
        return result;
    }

    /**
     * 更新书籍 - 清除详情、列表、搜索缓存
     */
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "book", key = "#id"),
        @CacheEvict(value = "bookList", allEntries = true),
        @CacheEvict(value = "bookSearch", allEntries = true)
    })
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        logger.info("[BOOK UPDATE] Updating bookId: {}", id);
        
        Book existingBook = bookDao.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在，ID: " + id));
        
        // 更新图书基础信息
        existingBook.setTitle(bookDTO.getTitle());
        existingBook.setAuthor(bookDTO.getAuthor());
        existingBook.setPrice(bookDTO.getPrice());
        existingBook.setCover(bookDTO.getCover());
        existingBook.setDescription(bookDTO.getDescription());
        existingBook.setIsbn(bookDTO.getIsbn());
        existingBook.setPublisher(bookDTO.getPublisher());
        
        Book savedBook = bookDao.save(existingBook);
        
        // 同步更新库存表
        if (bookDTO.getStock() != null) {
            BookStock bookStock = bookStockDao.findByBookId(id)
                    .orElse(new BookStock(id, bookDTO.getStock()));
            bookStock.setStock(bookDTO.getStock());
            bookStockDao.save(bookStock);
        }
        
        BookDTO result = bookMapper.toDTO(savedBook);
        fillStock(result);
        
        logger.info("[CACHE EVICT] Cleared detail/list/search caches for bookId: {}", id);
        return result;
    }

    /**
     * 删除书籍（软删除）- 清除所有相关缓存
     */
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "book", key = "#id"),
        @CacheEvict(value = "bookList", allEntries = true),
        @CacheEvict(value = "bookSearch", allEntries = true)
    })
    public void deleteBook(Long id) {
        logger.info("[BOOK DELETE] Soft deleting bookId: {}", id);
        
        Book book = bookDao.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在，ID: " + id));
        book.markAsDeleted();
        bookDao.save(book);
        
        logger.info("[CACHE EVICT] Cleared all caches for deleted bookId: {}", id);
    }
}