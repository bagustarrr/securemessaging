package com.securemsg.controller;

import com.securemsg.model.ChatRoom;
import com.securemsg.model.User;
import com.securemsg.service.ChatService;
import com.securemsg.service.MessageService;
import com.securemsg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final MessageService messageService;

    @GetMapping
    public String adminDashboard(@AuthenticationPrincipal User currentUser, Model model) {
        List<User> users = (List<User>) userRepository.findAll();
        List<ChatRoom> groups = chatService.getAllGroupChats();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", users);
        model.addAttribute("groups", groups);
        return "admin";
    }

    @PostMapping("/create-group")
    public String createGroupChat(@AuthenticationPrincipal User currentUser,
                                  @RequestParam String name,
                                  @RequestParam List<String> participants) {
        if (name == null || name.trim().isEmpty() || participants == null || participants.isEmpty()) {
            return "redirect:/admin?errorCreate";
        }
        // Verify that each participant IIN corresponds to an existing user
        for (String iin : participants) {
            Optional<User> userOpt = userRepository.findById(iin);
            if (userOpt.isEmpty()) {
                // Redirect with invalid participant IIN indicated
                return "redirect:/admin?invalidParticipant=" + iin;
            }
        }
        // Create the group chat (includes currentUser by default inside service)
        chatService.createGroupChat(name, participants, currentUser.getIin());
        return "redirect:/admin?created";
    }

    @PostMapping("/delete-group/{chatId}")
    public String deleteGroupChat(@PathVariable String chatId) {
        // Delete all messages and then the chat room
        messageService.deleteMessagesByChat(chatId);
        chatService.deleteChat(chatId);
        return "redirect:/admin?deleted";
    }
}
