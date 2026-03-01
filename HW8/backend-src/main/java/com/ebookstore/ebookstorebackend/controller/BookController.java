package com.ebookstore.ebookstorebackend.controller;

import com.ebookstore.ebookstorebackend.dto.BookDTO;
import com.ebookstore.ebookstorebackend.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class BookController {
    
    private final BookService bookService;
    
    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    
    // 获取所有图书（用户端，只返回未删除的）
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> books = bookService.getAllBooks();
        return new ResponseEntity<>(books, HttpStatus.OK);
    }      // 分页获取图书（用户端，只返回未删除的）
    @GetMapping("/page")
    public ResponseEntity<Page<BookDTO>> getBooksByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<BookDTO> books = bookService.getBooksByPage(pageable);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }
    
    // 根据ID获取图书（用户端，只返回未删除的）
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(book -> new ResponseEntity<>(book, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    // 搜索图书（用户端，只搜索未删除的）
    @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> searchBooks(@RequestParam String keyword) {
        List<BookDTO> books = bookService.searchBooks(keyword);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }    
    // 购物车和订单用：根据ID获取图书（包括已删除的）
    @GetMapping("/cart/{id}")
    public ResponseEntity<BookDTO> getBookByIdIncludingDeleted(@PathVariable Long id) {
        return bookService.getBookByIdIncludingDeleted(id)
                .map(book -> new ResponseEntity<>(book, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    // 购物车和订单用：批量获取图书（包括已删除的）
    @PostMapping("/cart/batch")
    public ResponseEntity<List<BookDTO>> getBooksByIdsIncludingDeleted(@RequestBody List<Long> ids) {
        List<BookDTO> books = bookService.getBooksByIdsIncludingDeleted(ids);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }
    
    // 管理员功能：创建新图书
    @PostMapping("/admin")
    public ResponseEntity<BookDTO> createBook(@RequestBody BookDTO bookDTO) {
        try {
            BookDTO createdBook = bookService.createBook(bookDTO);
            return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    // 管理员功能：更新图书
    @PutMapping("/admin/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO) {
        try {
            BookDTO updatedBook = bookService.updateBook(id, bookDTO);
            return new ResponseEntity<>(updatedBook, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
      // 管理员功能：软删除图书
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // 按标签搜索书籍
    
    @GetMapping("/search/tag")
    public ResponseEntity<List<BookDTO>> searchByTag(@RequestParam String tag) {
        List<BookDTO> books = bookService.searchBooksByTag(tag);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }
    
    // 获取所有标签（供前端展示）
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        List<String> tags = bookService.getAllTags();
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }
}