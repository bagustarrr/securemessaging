package com.securemsg.controller;

import com.securemsg.model.Message;
import com.securemsg.model.User;
import com.securemsg.service.ChatService;
import com.securemsg.service.MessageService;
import com.securemsg.service.UserService;
import com.securemsg.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Optional;

@Controller
public class ChatWsController {
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatWsController(ChatService chatService,
                            MessageService messageService,
                            UserService userService,
                            SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{chatId}")
    public void handleChatMessage(@DestinationVariable String chatId,
                                  @Payload String content,
                                  Principal principal) {
        if (principal == null) {
            return;
        }

        Optional<User> userOpt = userService.findByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return;
        }

        User sender = userOpt.get();
        Optional<Message> savedMessage = messageService.addMessage(chatId, sender, content);

        if (savedMessage.isPresent()) {
            Message message = savedMessage.get();

            // Расшифровка перед отправкой клиенту
            try {
                message.setContent(EncryptionUtil.decrypt(message.getContent()));
            } catch (Exception e) {
                message.setContent("[Decryption failed]");
            }

            messagingTemplate.convertAndSend("/topic/chat/" + chatId, message);
        }
    }
}
