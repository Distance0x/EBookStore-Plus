package com.ebookstore.ebookstorebackend.service;

import com.ebookstore.ebookstorebackend.dto.BookDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface BookService {
  // 用户端：只获取未删除的书籍
  List<BookDTO> getAllBooks();
  
  // 分页获取图书
  Page<BookDTO> getBooksByPage(Pageable pageable);
    
  // 通过ID获取单本图书（用户端，只返回未删除的）
  Optional<BookDTO> getBookById(Long id);
    // 搜索图书（用户端，只搜索未删除的）
  List<BookDTO> searchBooks(String keyword);
  
  // 购物车和订单用：获取书籍（包括已删除的）
  Optional<BookDTO> getBookByIdIncludingDeleted(Long id);
  List<BookDTO> getBooksByIdsIncludingDeleted(List<Long> ids);
    // 管理员功能
  BookDTO createBook(BookDTO bookDTO); // 添加新图书
  BookDTO updateBook(Long id, BookDTO bookDTO); // 更新图书信息
  void deleteBook(Long id); // 软删除图书
}
