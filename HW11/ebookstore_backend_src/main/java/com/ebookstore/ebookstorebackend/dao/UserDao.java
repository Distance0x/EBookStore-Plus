package com.ebookstore.ebookstorebackend.dao;

import com.ebookstore.ebookstorebackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

public interface UserDao {
    Optional<User> findByAccount(String account);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);    List<User> findAll();
    Page<User> findAll(Pageable pageable);
    User save(User user);
    void deleteById(Long id);
    boolean existsById(Long id);
}