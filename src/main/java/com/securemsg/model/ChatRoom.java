package com.securemsg.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("chatrooms")
public class ChatRoom {
    @Id
    private String id = new ObjectId().toHexString();  // generate unique ID

    private String name;
    private String type;           // "GROUP" or "PRIVATE"
    private List<String> participants;  // IINs of users in this chat
    private String createdBy;      // IIN of creator (for groups)
}
