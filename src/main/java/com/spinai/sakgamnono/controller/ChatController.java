package com.spinai.sakgamnono.controller;

import com.spinai.sakgamnono.service.OpenAIService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * 웹소켓 메시지를 수신하여, OpenAIService를 통해 GPT 응답을 받고,
 * 브라우저에 다시 전송하는 간단한 테스트 코드
 */

 @Controller
 public class ChatController {
 
     private final OpenAIService openAIService;
 
     public ChatController(OpenAIService openAIService) {
         this.openAIService = openAIService;
     }
 
     /**
      * 클라이언트에서 "/app/chat"로 메시지를 보내면 이 메서드가 실행됨
      * -> OpenAIService로 호출 -> 응답을 "/topic/public" 구독중인 모든 클라이언트에게 전송
      */
     @MessageMapping("/chat")
     @SendTo("/topic/public")
     public String handleMessage(String userInput) {
         // 1) 사용자 메시지
         System.out.println("User input: " + userInput);
 
         // 2) OpenAI API 호출 (단일 메시지 버전)
         String aiResponse = openAIService.callOpenAI(userInput);
 
         // 3) 리턴된 문자열이 브라우저("/topic/public" 구독자)에게 브로드캐스트됨
         return aiResponse;
     }
 }