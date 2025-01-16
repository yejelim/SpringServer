package com.spinai.sakgamnono.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;
    private String content;
    private String role; // "user" or "assistant"
    private LocalDateTime createdAt;

    // 필수 기본 생성자
    public ChatMessage() {}

    // 편의 생성자
    public ChatMessage(String nickname, String content, String role, LocalDateTime createdAt) {
        this.nickname = nickname;
        this.content = content;
        this.role = role;
        this.createdAt = createdAt;
    }
    
    // getters, setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
