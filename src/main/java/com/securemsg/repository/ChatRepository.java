package com.securemsg.repository;

import com.securemsg.model.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

// Репозиторий чатов
public interface ChatRepository extends MongoRepository<Chat, String> {
    // Найти все чаты, где участником является заданный пользователь
    List<Chat> findByParticipantsContaining(String userId);

    // Найти чаты заданного типа
    List<Chat> findByType(String type);

    // Найти групповые чаты, созданные указанным пользователем
    List<Chat> findByTypeAndCreatedBy(String type, String createdBy);

    // Найти личный чат между двумя пользователями (ровно 2 участника)
    @Query("{ 'type': 'PRIVATE', 'participants': { $all: [?0, ?1], $size: 2 } }")
    Optional<Chat> findPrivateChat(String userId1, String userId2);
}
