package com.ebookstore.ebookstorebackend.dto.mapper;

import com.ebookstore.ebookstorebackend.entity.OrderItem;
import com.ebookstore.ebookstorebackend.dto.OrderItemDTO;
import com.ebookstore.ebookstorebackend.service.BookService;
import com.ebookstore.ebookstorebackend.dto.BookDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * OrderItem实体和OrderItemDTO之间的转换器
 */
@Component
public class OrderItemMapper {

    @Autowired
    private BookService bookService;

    public OrderItemDTO toDTO(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setBookId(orderItem.getBookId());
        dto.setPrice(orderItem.getPrice());
        dto.setQuantity(orderItem.getQuantity());
          // 通过 BookService 获取书籍信息（包括已删除的书籍）
        try {
            Optional<BookDTO> bookOpt = bookService.getBookByIdIncludingDeleted(orderItem.getBookId());
            if (bookOpt.isPresent()) {
                BookDTO book = bookOpt.get();
                dto.setBookTitle(book.getTitle());
                dto.setBookAuthor(book.getAuthor());
                dto.setBookCover(book.getCover());
            } else {
                // 设置默认值
                dto.setBookTitle("未知书籍");
                dto.setBookAuthor("未知作者");
                dto.setBookCover("");
            }
        } catch (Exception e) {
            // 异常处理
            dto.setBookTitle("获取失败");
            dto.setBookAuthor("获取失败");
            dto.setBookCover("");
        }
        
        return dto;
    }

    public OrderItem toEntity(OrderItemDTO orderItemDTO) {
        if (orderItemDTO == null) {
            return null;
        }
        
        OrderItem orderItem = new OrderItem();
        orderItem.setId(orderItemDTO.getId());
        orderItem.setBookId(orderItemDTO.getBookId());
        orderItem.setPrice(orderItemDTO.getPrice());
        orderItem.setQuantity(orderItemDTO.getQuantity());
        
        return orderItem;
    }
}