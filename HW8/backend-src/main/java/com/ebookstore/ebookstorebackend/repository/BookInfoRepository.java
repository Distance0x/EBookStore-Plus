package com.ebookstore.ebookstorebackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ebookstore.ebookstorebackend.entity.BookInfo;

public interface BookInfoRepository extends MongoRepository<BookInfo, Long> {
    
  // 很多方法自动实现，直接调用即可
}