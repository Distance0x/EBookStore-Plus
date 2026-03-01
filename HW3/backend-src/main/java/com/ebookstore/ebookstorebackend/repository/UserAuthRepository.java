package com.ebookstore.ebookstorebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ebookstore.ebookstorebackend.entity.UserAuth;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long>{
  Optional<UserAuth> findByAccount(String account);
}
