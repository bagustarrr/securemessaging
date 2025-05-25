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

// Контроллер панели преподавателя
@Controller
@RequestMapping("/teacher")
public class TeacherController {
    private final ChatService chatService;
    private final UserService userService;

    public TeacherController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    // Страница панели преподавателя
    @GetMapping
    public String teacherPage(@AuthenticationPrincipal User currentUser, Model model) {
        List<Chat> groupChats = chatService.getGroupChatsByCreator(currentUser.getId());
        model.addAttribute("groupChats", groupChats);
        model.addAttribute("user", currentUser);
        // Имена создателей (в данном случае текущий преподаватель)
        Map<String, String> creatorNames = new HashMap<>();
        for (Chat chat : groupChats) {
            if (chat.getCreatedBy() != null) {
                String creatorName = userService.getNameById(chat.getCreatedBy());
                creatorNames.put(chat.getId(), creatorName);
            }
        }
        model.addAttribute("creatorNames", creatorNames);
        // Все пользователи для выбора участников
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "teacher";
    }

    // Создание нового группового чата (преподаватель)
    @PostMapping
    public String createGroupChat(@AuthenticationPrincipal User currentUser,
                                  @RequestParam("name") String name,
                                  @RequestParam(value = "participants", required = false) List<String> participantIds) {
        if (name == null || name.trim().isEmpty() || participantIds == null || participantIds.isEmpty()) {
            return "redirect:/teacher?error=invalid";
        }
        chatService.createGroupChat(name.trim(), participantIds, currentUser.getId());
        return "redirect:/teacher?success=new";
    }

    // Удаление группового чата (преподаватель)
    @PostMapping("/delete")
    public String deleteGroupChat(@AuthenticationPrincipal User currentUser,
                                  @RequestParam("chatId") String chatId) {
        boolean deleted = chatService.deleteChat(chatId, currentUser);
        if (!deleted) {
            return "redirect:/teacher?error=nodelete";
        }
        return "redirect:/teacher?success=del";
    }
}
