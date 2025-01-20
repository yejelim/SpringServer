package com.spinai.sakgamnono.controller;

import com.spinai.sakgamnono.domain.ChatMessage;
import com.spinai.sakgamnono.repository.ChatMessageRepository;
import com.spinai.sakgamnono.service.OpenAIService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;          // <-- (중요) ResponseEntity import
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;   // <-- @RestController, @GetMapping 등
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;                                // <-- (중요) Map.of(...) 용

/**
 * 웹소켓 메시지를 수신하여, OpenAIService를 통해 GPT 응답을 받고,
 * 브라우저에 다시 전송하는 간단한 테스트 코드
 */
@RestController
public class ChatController {

    private final OpenAIService openAIService;
    private final ChatMessageRepository chatRepo;

    // 생성자 주입
    public ChatController(OpenAIService openAIService, ChatMessageRepository chatRepo) {
        this.openAIService = openAIService;
        this.chatRepo = chatRepo;
    }

    /**
     * 클라이언트에서 "/app/chat"로 메시지를 보내면 이 메서드가 실행됨
     * -> OpenAIService 호출 -> 응답을 "/topic/public" 구독중인 모든 클라이언트에게 전송
     */
    @MessageMapping("/chat")
    @SendTo("/topic/public")
    public String handleMessage(String userInput, Principal principal) {
        // WebSocket STOMP의 principal 인식 확인 디버깅용
        System.out.println("handleMessage called. principal=" + principal);
        
        // 1) GPT 호출 (공통)
        String aiResponse = openAIService.callOpenAI(userInput);

        // 2) principal != null (로그인 상태) -> DB에 저장
        if (principal != null) {
            System.out.println("Detected user principal:") + principal.getName());
            String nickname = principal.getName();

            // user message
            ChatMessage userMsg = new ChatMessage(nickname, userInput, "user", LocalDateTime.now());
            chatRepo.save(userMsg);

            // GPT 응답도 저장
            ChatMessage botMsg = new ChatMessage(nickname, aiResponse, "assistant", LocalDateTime.now());
            chatRepo.save(botMsg);

        } else {
            System.out.println("익명 사용자 메시지: " + userInput);
        }

        // 3) return -> /topic/public 브로드캐스트
        return aiResponse;
    }

    /**
     * [추가] 로그인 사용자 전용 대화내역 불러오기
     * GET /api/chat/history
     */
    @GetMapping("/api/chat/history")
    public ResponseEntity<?> loadChatHistory(Principal principal) {
        // 1) 비로그인 상태면 401 + JSON
        if (principal == null) {
            // JSON 형태로 응답
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다.")); 
        }

        // 2) DB에서 nickname 기준으로 메시지 불러오기
        String nickname = principal.getName();
        List<ChatMessage> messages = chatRepo.findByNicknameOrderByCreatedAtAsc(nickname);

        // 3) JSON으로 반환 (200 OK)
        return ResponseEntity.ok(messages);
    }
}
