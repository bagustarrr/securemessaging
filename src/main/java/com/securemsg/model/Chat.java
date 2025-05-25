package com.securemsg.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

// Модель чата (личный или групповой)
@Document(collection = "chats")
public class Chat {
    @Id
    private String id;
    private String name;              // название группы (для группового чата)
    private String type;              // тип: "PRIVATE" или "GROUP"
    private List<String> participants; // список ID участников
    private String createdBy;         // ID создателя (для группового чата)

    public Chat() {
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public List<String> getParticipants() {
        return participants;
    }
    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }
    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
