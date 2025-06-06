package com.securemsg.controller;

import com.securemsg.model.Chat;
import com.securemsg.model.User;
import com.securemsg.service.ChatService;
import com.securemsg.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

// Контроллер панели администратора
@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ChatService chatService;
    private final UserService userService;

    public AdminController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    // Страница панели администратора
    @GetMapping
    public String adminPage(@AuthenticationPrincipal User currentUser, Model model) {
        List<Chat> groupChats = chatService.getAllGroupChats();
        model.addAttribute("groupChats", groupChats);
        model.addAttribute("user", currentUser);
        // Имена создателей для отображения
        Map<String, String> creatorNames = new HashMap<>();
        for (Chat chat : groupChats) {
            if (chat.getCreatedBy() != null) {
                String creatorName = userService.getNameById(chat.getCreatedBy());
                creatorNames.put(chat.getId(), creatorName);
            }
        }
        model.addAttribute("creatorNames", creatorNames);
        // Список всех пользователей для выбора участников
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin";
    }

    // Создание нового группового чата
    @PostMapping
    public String createGroupChat(@AuthenticationPrincipal User currentUser,
                                  @RequestParam("name") String name,
                                  @RequestParam(value = "participants", required = false) List<String> participantIds) {
        if (name == null || name.trim().isEmpty() || participantIds == null || participantIds.isEmpty()) {
            return "redirect:/admin?error=invalid";
        }
        chatService.createGroupChat(name.trim(), participantIds, currentUser.getId());
        return "redirect:/admin?success=new";
    }

    // Удаление группового чата
    @PostMapping("/delete")
    public String deleteGroupChat(@AuthenticationPrincipal User currentUser,
                                  @RequestParam("chatId") String chatId) {
        boolean deleted = chatService.deleteChat(chatId, currentUser);
        if (!deleted) {
            return "redirect:/admin?error=nodelete";
        }
        return "redirect:/admin?success=del";
    }
}
