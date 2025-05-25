package com.securemsg.controller;

import com.securemsg.model.ChatRoom;
import com.securemsg.model.Message;
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

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserRepository userRepository;

    @GetMapping("/chat")
    public String chatHome(@AuthenticationPrincipal User currentUser, Model model) {
        // List all chats for this user (to display in sidebar)
        List<ChatRoom> chats = chatService.getChatsForUser(currentUser.getIin());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("chats", chats);
        model.addAttribute("selectedChat", null);
        return "chat";
    }

    @GetMapping("/chat/{chatId}")
    public String openChat(@AuthenticationPrincipal User currentUser,
                           @PathVariable String chatId, Model model) {
        // Ensure the user has access to this chat
        ChatRoom chatRoom;
        try {
            chatRoom = chatService.getChatForUser(chatId, currentUser.getIin());
        } catch (IllegalArgumentException e) {
            // Chat not found or user not in chat -> redirect to error page or home
            return "redirect:/error";
        }
        // Load messages in this chat
        List<Message> messages = messageService.getMessagesForChat(chatId);
        // Determine chat title for display
        String chatTitle;
        if ("GROUP".equals(chatRoom.getType())) {
            chatTitle = chatRoom.getName();
        } else {
            // For private chat, find the other participant's name
            String otherIIN = chatRoom.getParticipants().stream()
                    .filter(iin -> !iin.equals(currentUser.getIin()))
                    .findFirst().orElse(currentUser.getIin());
            User otherUser = userRepository.findById(otherIIN).orElse(null);
            chatTitle = (otherUser != null ? otherUser.getFullName() : "Private Chat");
        }
        // Prepare model for view
        List<ChatRoom> chats = chatService.getChatsForUser(currentUser.getIin());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("chats", chats);
        model.addAttribute("selectedChat", chatRoom);
        model.addAttribute("chatTitle", chatTitle);
        model.addAttribute("messages", messages);
        return "chat";
    }

    @PostMapping("/chat/new")
    public String startNewChat(@AuthenticationPrincipal User currentUser,
                               @RequestParam("otherIIN") String otherIIN) {
        if (otherIIN == null || otherIIN.isBlank()) {
            return "redirect:/chat";
        }
        if (otherIIN.equals(currentUser.getIin())) {
            // Cannot start a chat with oneself
            return "redirect:/chat?errorSelf";
        }
        // Check that target user exists
        if (!userRepository.existsById(otherIIN)) {
            return "redirect:/chat?errorUserNotFound";
        }
        // Create or get existing private chat then redirect to it
        ChatRoom chatRoom = chatService.getOrCreatePrivateChat(currentUser.getIin(), otherIIN);
        return "redirect:/chat/" + chatRoom.getId();
    }
}
