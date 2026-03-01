package com.llama.ebookmicroservice.dto;

import java.util.List;
import java.util.Map;

public class AuthorResponseDTO {
    private String searchTitle;
    private String author;
    private Boolean found;
    private Integer count;
    private List<Map<String, String>> books;

    public AuthorResponseDTO() {
    }

    public AuthorResponseDTO(String searchTitle, String author, Boolean found, Integer count, List<Map<String, String>> books) {
        this.searchTitle = searchTitle;
        this.author = author;
        this.found = found;
        this.count = count;
        this.books = books;
    }

    public String getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(String searchTitle) {
        this.searchTitle = searchTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Boolean getFound() {
        return found;
    }

    public void setFound(Boolean found) {
        this.found = found;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Map<String, String>> getBooks() {
        return books;
    }

    public void setBooks(List<Map<String, String>> books) {
        this.books = books;
    }
}

