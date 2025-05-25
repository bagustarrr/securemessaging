package com.securemsg.repository;

import com.securemsg.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

// Репозиторий пользователей
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}
