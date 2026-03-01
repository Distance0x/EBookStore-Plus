package com.ebookstore.ebookstorebackend.dao;

import com.ebookstore.ebookstorebackend.entity.UserAuth;
import java.util.Optional;

public interface UserAuthDao {
    Optional<UserAuth> findByAccount(String account);
    UserAuth save(UserAuth userAuth);
    void deleteById(Long id);
}