package com.ebookstore.ebookstorebackend.dto.mapper;

import com.ebookstore.ebookstorebackend.entity.Book;
import com.ebookstore.ebookstorebackend.dto.BookDTO;
import org.springframework.stereotype.Component;

// 转换器

@Component
public class BookMapper {    public BookDTO toDTO(Book book) {
        if (book == null) {
            return null;
        }
        
        return new BookDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getPrice(),
            book.getCover(),
            book.getDescription(),
            book.getIsbn(),
            book.getStock(),
            book.getPublisher(),
            book.getDeleted()
        );
    }    public Book toEntity(BookDTO bookDTO) {
        if (bookDTO == null) {
            return null;
        }
        
        Book book = new Book();
        book.setId(bookDTO.getId());
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setPrice(bookDTO.getPrice());
        book.setCover(bookDTO.getCover());
        book.setDescription(bookDTO.getDescription());
        book.setIsbn(bookDTO.getIsbn());
        book.setStock(bookDTO.getStock());
        book.setPublisher(bookDTO.getPublisher());
        book.setDeleted(bookDTO.getDeleted() != null ? bookDTO.getDeleted() : false);
        
        return book;
    }
}