package com.llama.ebookmicroservice.controller;

import com.llama.ebookmicroservice.dto.AuthorResponseDTO;
import com.llama.ebookmicroservice.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
//@CrossOrigin
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 根据书名查询作者
     * @param title 书名
     * @param exact 是否精确匹配（默认false，模糊匹配）
     */
    @GetMapping("/by-title")
    public ResponseEntity<AuthorResponseDTO> getAuthorByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "false") boolean exact) {

        AuthorResponseDTO response = bookService.getAuthorByTitle(title, exact);
        return ResponseEntity.ok(response);
    }
}

