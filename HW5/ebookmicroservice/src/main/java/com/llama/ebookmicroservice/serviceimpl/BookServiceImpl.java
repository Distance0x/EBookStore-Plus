package com.llama.ebookmicroservice.serviceimpl;

import com.llama.ebookmicroservice.dao.BookDao;
import com.llama.ebookmicroservice.dto.AuthorResponseDTO;
import com.llama.ebookmicroservice.entity.Book;
import com.llama.ebookmicroservice.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    
    private final BookDao bookDao;
    
    @Autowired
    public BookServiceImpl(BookDao bookDao) {
        this.bookDao = bookDao;
    }
    
    @Override
    @Transactional(readOnly = true)
    public AuthorResponseDTO getAuthorByTitle(String title, boolean exact) {
        List<Book> books;
        if (exact) {
            books = bookDao.findByTitleAndDeletedFalse(title);
        } else {
            books = bookDao.findByTitleContainingIgnoreCaseAndDeletedFalse(title);
        }
        
        if (books.isEmpty()) {
            return new AuthorResponseDTO(
                title,
                "未知作者",
                false,
                0,
                List.of()
            );
        }
        
        // 返回第一个匹配的作者，以及匹配的书籍列表
        List<Map<String, String>> bookList = books.stream()
                .map(b -> {
                    Map<String, String> bookMap = new HashMap<>();
                    bookMap.put("title", b.getTitle());
                    bookMap.put("author", b.getAuthor());
                    return bookMap;
                })
                .collect(Collectors.toList());
        
        return new AuthorResponseDTO(
            title,
            books.get(0).getAuthor(),
            true,
            books.size(),
            bookList
        );
    }
}

