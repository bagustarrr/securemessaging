package com.securemsg.repository;

import com.securemsg.model.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    // Find all chat rooms where the participants list contains the given IIN
    List<ChatRoom> findByParticipantsContaining(String iin);
    // Find all chat rooms of a given type (e.g., all "GROUP" chats)
    List<ChatRoom> findByType(String type);
}
