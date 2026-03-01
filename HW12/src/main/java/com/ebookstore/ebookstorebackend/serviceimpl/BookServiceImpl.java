// src/main/java/com/ebookstore/ebookstorebackend/service/impl/BookServiceImpl.java
package com.ebookstore.ebookstorebackend.serviceimpl;

import com.ebookstore.ebookstorebackend.entity.Book;
import com.ebookstore.ebookstorebackend.dto.BookDTO;
import com.ebookstore.ebookstorebackend.dto.mapper.BookMapper;
import com.ebookstore.ebookstorebackend.dao.BookDao;
import com.ebookstore.ebookstorebackend.service.BookService;
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
      private final BookDao bookDao;
    private final BookMapper bookMapper;
    
    @Autowired
    public BookServiceImpl(BookDao bookDao, BookMapper bookMapper) {
        this.bookDao = bookDao;
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
    }      @Override
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
        
        Book savedBook = bookDao.save(existingBook);
        return bookMapper.toDTO(savedBook);
    }      @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookDao.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在，ID: " + id));
        book.markAsDeleted();
        bookDao.save(book);
    }
}