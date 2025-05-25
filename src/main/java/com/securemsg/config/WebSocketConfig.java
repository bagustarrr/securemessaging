package com.securemsg.config;

import com.securemsg.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final ChatService chatService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket handshake endpoint (SockJS enabled for fallback)
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for messages from client to server
        registry.setApplicationDestinationPrefixes("/app");
        // Enable simple broker for topics (broadcast) and queues (personal)
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Intercept subscriptions to ensure user belongs to the chat they subscribe to
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public org.springframework.messaging.Message<?> preSend(
                    org.springframework.messaging.Message<?> message,
                    org.springframework.messaging.MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
                    String dest = accessor.getDestination();
                    if (dest != null && dest.startsWith("/topic/chatroom/")) {
                        // Extract chatId from destination, e.g., "/topic/chatroom/123"
                        String chatId = dest.substring("/topic/chatroom/".length());
                        String userIin = (accessor.getUser() != null ? accessor.getUser().getName() : null);
                        if (userIin == null || !chatService.isUserInChat(chatId, userIin)) {
                            throw new AccessDeniedException("Not allowed to subscribe to this chat");
                        }
                    }
                }
                return message;
            }
        });
    }
}
