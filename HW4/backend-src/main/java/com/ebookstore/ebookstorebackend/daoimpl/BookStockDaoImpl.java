package com.ebookstore.ebookstorebackend.daoimpl;

import com.ebookstore.ebookstorebackend.dao.BookStockDao;
import com.ebookstore.ebookstorebackend.entity.BookStock;
import com.ebookstore.ebookstorebackend.repository.BookStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class BookStockDaoImpl implements BookStockDao {
    
    private final BookStockRepository bookStockRepository;
    
    @Autowired
    public BookStockDaoImpl(BookStockRepository bookStockRepository) {
        this.bookStockRepository = bookStockRepository;
    }
    
    @Override
    public Optional<BookStock> findByBookId(Long bookId) {
        return bookStockRepository.findByBookId(bookId);
    }
    
    @Override
    public List<BookStock> findByBookIdIn(List<Long> bookIds) {
        return bookStockRepository.findByBookIdIn(bookIds);
    }
    
    @Override
    public boolean existsByBookId(Long bookId) {
        return bookStockRepository.existsByBookId(bookId);
    }
    
    @Override
    public BookStock save(BookStock bookStock) {
        return bookStockRepository.save(bookStock);
    }
    
    @Override
    @Transactional
    public boolean decrementStock(Long bookId, Integer quantity) {
        Optional<BookStock> stockOpt = bookStockRepository.findByBookId(bookId);
        if (stockOpt.isEmpty()) {
            return false;
        }
        
        BookStock bookStock = stockOpt.get();
        if (bookStock.getStock() < quantity) {
            return false;  // 库存不足
        }
        
        bookStock.setStock(bookStock.getStock() - quantity);
        bookStockRepository.save(bookStock);
        return true;
    }
    
    @Override
    @Transactional
    public boolean incrementStock(Long bookId, Integer quantity) {
        Optional<BookStock> stockOpt = bookStockRepository.findByBookId(bookId);
        if (stockOpt.isEmpty()) {
            return false;
        }
        
        BookStock bookStock = stockOpt.get();
        bookStock.setStock(bookStock.getStock() + quantity);
        bookStockRepository.save(bookStock);
        return true;
    }
}
