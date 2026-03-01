package com.ebookstore.ebookstorebackend.daoimpl;

import com.ebookstore.ebookstorebackend.dao.UserAuthDao;
import com.ebookstore.ebookstorebackend.entity.UserAuth;
import com.ebookstore.ebookstorebackend.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserAuthDaoImpl implements UserAuthDao {
    
    private final UserAuthRepository userAuthRepository;
    
    @Autowired
    public UserAuthDaoImpl(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }
    
    @Override
    public Optional<UserAuth> findByAccount(String account) {
        return userAuthRepository.findByAccount(account);
    }
    
    @Override
    public UserAuth save(UserAuth userAuth) {
        return userAuthRepository.save(userAuth);
    }
    
    @Override
    public void deleteById(Long id) {
        userAuthRepository.deleteById(id);
    }
}