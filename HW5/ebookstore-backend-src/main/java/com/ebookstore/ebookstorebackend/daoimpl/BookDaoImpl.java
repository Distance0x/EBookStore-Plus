package com.ebookstore.ebookstorebackend.daoimpl;

import com.ebookstore.ebookstorebackend.dao.BookDao;
import com.ebookstore.ebookstorebackend.entity.Book;
import com.ebookstore.ebookstorebackend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Component
@Repository
public class BookDaoImpl implements BookDao {
    
    private final BookRepository bookRepository;
    
    @Autowired
    public BookDaoImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }
    
    @Override
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }
    
    @Override
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }
    
    @Override
    public List<Book> findByTitleContainingIgnoreCase(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
    
    @Override
    public List<Book> findByAuthorContainingIgnoreCase(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }
    
    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }
      @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }
    
    // 软删除相关方法实现
    @Override
    public List<Book> findByDeletedFalseOrderByIdAsc() {
        return bookRepository.findByDeletedFalseOrderByIdAsc();
    }
    
    @Override
    public Page<Book> findByDeletedFalse(Pageable pageable) {
        return bookRepository.findByDeletedFalse(pageable);
    }
    
    @Override
    public List<Book> findByTitleContainingAndDeletedFalseOrderByIdAsc(String title) {
        return bookRepository.findByTitleContainingAndDeletedFalseOrderByIdAsc(title);
    }
    
    @Override
    public List<Book> findByAuthorContainingAndDeletedFalseOrderByIdAsc(String author) {
        return bookRepository.findByAuthorContainingAndDeletedFalseOrderByIdAsc(author);
    }
    
    @Override
    public Optional<Book> findByIdAndDeletedFalse(Long id) {
        return bookRepository.findByIdAndDeletedFalse(id);
    }
    
    @Override
    public List<Book> findByIdIn(List<Long> ids) {
        return bookRepository.findByIdIn(ids);
    }
}
