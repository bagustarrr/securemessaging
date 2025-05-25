package com.securemsg.service;

import com.securemsg.model.Message;
import com.securemsg.model.User;
import com.securemsg.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

// Сервис для работы с сообщениями
@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatService chatService;

    public MessageService(MessageRepository messageRepository, ChatService chatService) {
        this.messageRepository = messageRepository;
        this.chatService = chatService;
    }

    // Получить все сообщения чата по возрастанию времени
    public List<Message> getMessagesByChatId(String chatId) {
        return messageRepository.findByChatIdOrderByTimestampAsc(chatId);
    }

    // Добавить новое сообщение в чат
    public Optional<Message> addMessage(String chatId, User sender, String content) {
        // Проверка участия пользователя в чате
        if (!chatService.isUserInChat(chatId, sender.getId())) {
            return Optional.empty();
        }
        Message message = new Message();
        message.setChatId(chatId);
        message.setSenderId(sender.getId());
        message.setSenderName(sender.getName());
        message.setContent(content);
        message.setTimestamp(new Date());
        messageRepository.save(message);
        return Optional.of(message);
    }
}
