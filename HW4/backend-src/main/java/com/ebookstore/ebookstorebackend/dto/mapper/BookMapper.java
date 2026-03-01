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
        
        // 不再从 Book 取 stock，由 Service 层拼装
        return new BookDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getPrice(),
            book.getCover(),
            book.getDescription(),
            book.getIsbn(),
            null,  // stock 留空，由 Service 从 BookStock 拼装
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
        // stock 不再设置到 Book，由 Service 单独维护 BookStock
        book.setPublisher(bookDTO.getPublisher());
        book.setDeleted(bookDTO.getDeleted() != null ? bookDTO.getDeleted() : false);
        
        return book;
    }
}