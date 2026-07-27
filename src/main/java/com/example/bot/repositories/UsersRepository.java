package com.example.bot.repositories;

import com.example.bot.entity.Users;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    @Query(value = "SELECT * FROM users WHERE chat_id = :chat_id", nativeQuery = true)
    Optional<Users> findByChatId(@Param("chat_id") Long chatId);

    @Query(value = "SELECT balance FROM users WHERE chat_id = :chat_id", nativeQuery = true)
    Double findBalanceByChatId(@Param("chat_id") Long chatId);


    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET status = :status WHERE chat_id = :chatId", nativeQuery = true)
    void setStatus(@Param("status") String status, @Param("chatId") Long chatId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET bet = :bet WHERE chat_id = :chatId", nativeQuery = true)
    void setBet(@Param("bet") Double bet, @Param("chatId") Long chatId);
}
