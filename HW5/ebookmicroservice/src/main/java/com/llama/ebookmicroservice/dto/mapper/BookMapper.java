package com.llama.ebookmicroservice.dto.mapper;

import com.llama.ebookmicroservice.entity.Book;
import com.llama.ebookmicroservice.dto.BookDTO;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {
    
    public BookDTO toDTO(Book book) {
        if (book == null) {
            return null;
        }
        
        return new BookDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getDeleted()
        );
    }
    
    public Book toEntity(BookDTO bookDTO) {
        if (bookDTO == null) {
            return null;
        }
        
        Book book = new Book();
        book.setId(bookDTO.getId());
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setDeleted(bookDTO.getDeleted() != null ? bookDTO.getDeleted() : false);
        
        return book;
    }
}

