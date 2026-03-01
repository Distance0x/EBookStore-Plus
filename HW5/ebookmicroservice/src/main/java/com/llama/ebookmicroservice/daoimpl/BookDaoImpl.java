package com.llama.ebookmicroservice.daoimpl;

import com.llama.ebookmicroservice.dao.BookDao;
import com.llama.ebookmicroservice.entity.Book;
import com.llama.ebookmicroservice.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookDaoImpl implements BookDao {
    
    private final BookRepository bookRepository;
    
    @Autowired
    public BookDaoImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    @Override
    public List<Book> findByTitleAndDeletedFalse(String title) {
        return bookRepository.findByTitleAndDeletedFalse(title);
    }
    
    @Override
    public List<Book> findByTitleContainingIgnoreCaseAndDeletedFalse(String title) {
        return bookRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(title);
    }
}

