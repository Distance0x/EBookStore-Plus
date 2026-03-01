package com.ebookstore.ebookstorebackend.repository;

import com.ebookstore.ebookstorebackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
  Optional<User> findByAccount(String account);
  Optional<User> findByEmail(String email);
}
