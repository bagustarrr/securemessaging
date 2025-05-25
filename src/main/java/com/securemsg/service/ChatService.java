package com.securemsg.service;

import com.securemsg.model.Chat;
import com.securemsg.model.User;
import com.securemsg.repository.ChatRepository;
import com.securemsg.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Сервис управления чатами
@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public ChatService(ChatRepository chatRepository, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    // Все чаты, в которых состоит пользователь
    public List<Chat> getChatsForUser(String userId) {
        return chatRepository.findByParticipantsContaining(userId);
    }

    // Получить или создать личный чат между двумя пользователями
    public Chat getOrCreatePrivateChat(String userId1, String userId2) {
        // Проверяем существование
        Optional<Chat> existing = chatRepository.findPrivateChat(userId1, userId2);
        if (existing.isPresent()) {
            return existing.get();
        }
        // Создаем новый приватный чат
        Chat chat = new Chat();
        chat.setType("PRIVATE");
        chat.setName(null);
        List<String> participants = new ArrayList<>();
        participants.add(userId1);
        participants.add(userId2);
        chat.setParticipants(participants);
        chat.setCreatedBy(null);
        chatRepository.save(chat);
        return chat;
    }

    // Создать новый групповой чат
    public Optional<Chat> createGroupChat(String name, List<String> participantIds, String creatorId) {
        if (name == null || name.trim().isEmpty() || participantIds == null || participantIds.isEmpty()) {
            return Optional.empty();
        }
        // Убеждаемся, что создатель включен
        if (!participantIds.contains(creatorId)) {
            participantIds.add(creatorId);
        }
        Chat chat = new Chat();
        chat.setType("GROUP");
        chat.setName(name);
        chat.setParticipants(participantIds);
        chat.setCreatedBy(creatorId);
        chatRepository.save(chat);
        return Optional.of(chat);
    }

    // Удалить групповой чат (с проверкой прав)
    public boolean deleteChat(String chatId, User currentUser) {
        Optional<Chat> opt = chatRepository.findById(chatId);
        if (opt.isEmpty()) {
            return false;
        }
        Chat chat = opt.get();
        if (!"GROUP".equals(chat.getType())) {
            return false;
        }
        boolean isAdmin = currentUser.getRoles().contains("ADMIN");
        boolean isTeacher = currentUser.getRoles().contains("TEACHER");
        if (!(isAdmin || (isTeacher && currentUser.getId().equals(chat.getCreatedBy())))) {
            return false;
        }
        // Удаляем все сообщения чата
        messageRepository.deleteByChatId(chatId);
        // Удаляем чат
        chatRepository.delete(chat);
        return true;
    }

    // Проверить участие пользователя в чате
    public boolean isUserInChat(String chatId, String userId) {
        Optional<Chat> opt = chatRepository.findById(chatId);
        return opt.isPresent() && opt.get().getParticipants().contains(userId);
    }

    // Найти чат по ID для пользователя (только если состоит)
    public Optional<Chat> getChatForUser(String chatId, String userId) {
        Optional<Chat> opt = chatRepository.findById(chatId);
        if (opt.isPresent() && opt.get().getParticipants().contains(userId)) {
            return opt;
        }
        return Optional.empty();
    }

    // Все групповые чаты (для админа)
    public List<Chat> getAllGroupChats() {
        return chatRepository.findByType("GROUP");
    }

    // Групповые чаты, созданные данным пользователем
    public List<Chat> getGroupChatsByCreator(String creatorId) {
        return chatRepository.findByTypeAndCreatedBy("GROUP", creatorId);
    }
}
