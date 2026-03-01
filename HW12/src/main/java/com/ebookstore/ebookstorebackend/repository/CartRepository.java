package com.ebookstore.ebookstorebackend.repository;

import com.ebookstore.ebookstorebackend.entity.Cart;
import com.ebookstore.ebookstorebackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // 根据用户ID查找购物车
    List<Cart> findByUser(User user);

    // 根据用户ID和图书ID查找购物车中的某个图书
    Optional<Cart> findByUserIdAndBookId(Long userId, Long bookId);

    @Modifying
    // 删除购物车中的某个图书
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId AND c.book.id = :bookId")
    void deleteByUserIdAndBookId(Long userId, Long bookId);

    @Modifying
    // 清空用户的购物车
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId")
    void deleteByUserId(Long userId);

}
