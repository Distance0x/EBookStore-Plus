package com.ebookstore.ebookstorebackend.controller;

import com.ebookstore.ebookstorebackend.dto.BookDTO;
import com.ebookstore.ebookstorebackend.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class BookGraphQLController {

    private static final Logger log = LoggerFactory.getLogger(BookGraphQLController.class);
    private final BookService bookService;

    @Autowired
    public BookGraphQLController(BookService bookService) {
        this.bookService = bookService;
    }

    @QueryMapping
    public List<BookDTO> searchBooks(@Argument String keyword) {
        log.info("GraphQL searchBooks keyword={}", keyword);
        return bookService.searchBooks(keyword);
    }
}