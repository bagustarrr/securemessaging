package com.securemsg.controller;

import com.securemsg.model.Chat;
import com.securemsg.model.User;
import com.securemsg.model.Message;
import com.securemsg.service.ChatService;
import com.securemsg.service.MessageService;
import com.securemsg.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// Контроллер страницы чата и личных сообщений
@Controller
public class ChatController {
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;

    public ChatController(ChatService chatService, MessageService messageService, UserService userService) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.userService = userService;
    }

    // Страница чатов
    @GetMapping("/chat")
    public String chatPage(@AuthenticationPrincipal User currentUser,
                           @RequestParam(required = false) String chatId,
                           Model model) {
        String currentUserId = currentUser.getId();

        // Все чаты пользователя
        List<Chat> chats = chatService.getChatsForUser(currentUserId);
        model.addAttribute("chats", chats);
        model.addAttribute("user", currentUser);

        // Имена чатов
        Map<String, String> chatNames = new HashMap<>();
        for (Chat chat : chats) {
            if ("GROUP".equals(chat.getType())) {
                chatNames.put(chat.getId(), chat.getName());
            } else if ("PRIVATE".equals(chat.getType())) {
                for (String uid : chat.getParticipants()) {
                    if (!uid.equals(currentUserId)) {
                        chatNames.put(chat.getId(), userService.getNameById(uid));
                        break;
                    }
                }
            }
        }
        model.addAttribute("chatNames", chatNames);

        // Если чат не указан — редирект на первый
        if (chatId == null || chatId.isEmpty()) {
            if (!chats.isEmpty()) {
                return "redirect:/chat?chatId=" + chats.get(0).getId();
            } else {
                return "chat";
            }
        }

        // Проверка доступа
        Optional<Chat> chatOpt = chatService.getChatForUser(chatId, currentUserId);
        if (chatOpt.isEmpty()) {
            if (!chats.isEmpty()) {
                return "redirect:/chat";
            } else {
                return "chat";
            }
        }

        // Данные выбранного чата
        Chat chat = chatOpt.get();
        model.addAttribute("chat", chat);
        model.addAttribute("currentChatName", chatNames.get(chat.getId()));

        // Получение и расшифровка сообщений
        List<Message> messages = messageService.getMessagesByChatId(chat.getId());
        model.addAttribute("messages", messages);

        return "chat";
    }

    // Создание нового личного чата
    @PostMapping("/chat/new")
    public String newPersonalChat(@AuthenticationPrincipal User currentUser,
                                  @RequestParam("email") String otherEmail) {
        if (otherEmail == null || otherEmail.trim().isEmpty()) {
            return "redirect:/chat?error=nouser";
        }

        if (otherEmail.equalsIgnoreCase(currentUser.getEmail())) {
            return "redirect:/chat?error=self";
        }

        Optional<User> otherUserOpt = userService.findByEmail(otherEmail);
        if (otherUserOpt.isEmpty()) {
            return "redirect:/chat?error=nouser";
        }

        User otherUser = otherUserOpt.get();

        // Проверка, существует ли уже такой чат
        Chat existing = chatService.findPrivateChat(currentUser.getId(), otherUser.getId());
        if (existing != null) {
            return "redirect:/chat?error=exists";
        }

        Chat chat = chatService.getOrCreatePrivateChat(currentUser.getId(), otherUser.getId());
        return "redirect:/chat?chatId=" + chat.getId();
    }

}
