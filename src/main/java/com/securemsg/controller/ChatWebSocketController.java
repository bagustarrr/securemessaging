package com.securemsg.controller;

import com.securemsg.dto.ChatMessageRequest;
import com.securemsg.dto.ChatMessageResponse;
import com.securemsg.model.ChatRoom;
import com.securemsg.model.User;
import com.securemsg.repository.UserRepository;
import com.securemsg.service.ChatService;
import com.securemsg.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void handleChatMessage(ChatMessageRequest messageRequest, Principal principal) {
        String senderIin = principal.getName();
        String chatId = messageRequest.getChatId();
        String content = messageRequest.getContent().trim();
        if (content.isEmpty()) {
            return; // don't send empty messages
        }
        // Verify the sender is part of the chat (and chat exists)
        ChatRoom chat = chatService.getChatForUser(chatId, senderIin);
        // Save message to DB
        messageService.saveMessage(chatId, senderIin, content);
        // Prepare a response payload
        ChatMessageResponse response = new ChatMessageResponse();
        response.setChatId(chatId);
        response.setSender(senderIin);
        User sender = userRepository.findById(senderIin).orElse(null);
        response.setSenderName(sender != null ? sender.getFullName() : "Unknown");
        response.setContent(content);
        response.setTimestamp(new SimpleDateFormat("HH:mm").format(new Date()));
        // Send message to appropriate destinations
        if ("GROUP".equals(chat.getType())) {
            // broadcast to all subscribers of this group's topic
            messagingTemplate.convertAndSend("/topic/chatroom/" + chatId, response);
        } else {
            // send to each user’s personal queue (for private chats between two users)
            for (String participantIin : chat.getParticipants()) {
                messagingTemplate.convertAndSendToUser(participantIin, "/queue/messages", response);
            }
        }
    }
}
