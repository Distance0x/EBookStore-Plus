package com.llama.ebookmicroservice.service;

import com.llama.ebookmicroservice.dto.AuthorResponseDTO;

public interface BookService {
    /**
     * 根据书名查询作者
     * @param title 书名
     * @param exact 是否精确匹配（默认false，模糊匹配）
     * @return 作者响应DTO
     */
    AuthorResponseDTO getAuthorByTitle(String title, boolean exact);
}

