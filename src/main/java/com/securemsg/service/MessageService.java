package com.securemsg.service;

import com.securemsg.model.Message;
import com.securemsg.model.User;
import com.securemsg.repository.MessageRepository;
import com.securemsg.util.EncryptionUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatService chatService;

    public MessageService(MessageRepository messageRepository, ChatService chatService) {
        this.messageRepository = messageRepository;
        this.chatService = chatService;
    }

    public List<Message> getMessagesByChatId(String chatId) {
        List<Message> messages = messageRepository.findByChatIdOrderByTimestampAsc(chatId);
        for (Message msg : messages) {
            try {
                msg.setContent(EncryptionUtil.decrypt(msg.getContent()));
            } catch (Exception e) {
                msg.setContent("[Ошибка расшифровки]");
            }
        }
        return messages;
    }

    public Optional<Message> addMessage(String chatId, User sender, String content) {
        if (!chatService.isUserInChat(chatId, sender.getId())) {
            return Optional.empty();
        }

        try {
            String encrypted = EncryptionUtil.encrypt(content);

            Message message = new Message();
            message.setChatId(chatId);
            message.setSenderId(sender.getId());
            message.setSenderName(sender.getName());
            message.setContent(encrypted);
            message.setTimestamp(new Date());

            messageRepository.save(message);
            return Optional.of(message);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
