package com.spinai.sakgamnono.repository;

import com.spinai.sakgamnono.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 사용자별 검색 필요시 아래 부분 주석해제
    List<ChatMessage> findByNicknameOrderByCreatedAtAsc(String nickname);
} 