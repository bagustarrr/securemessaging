package com.securemsg.repository;

import com.securemsg.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// Репозиторий сообщений
public interface MessageRepository extends MongoRepository<Message, String> {
    // Получить все сообщения чата (по времени возрастания)
    List<Message> findByChatIdOrderByTimestampAsc(String chatId);

    // Удалить все сообщения чата (например, при удалении чата)
    void deleteByChatId(String chatId);
}
