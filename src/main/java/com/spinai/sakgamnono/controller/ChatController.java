package com.spinai.sakgamnono.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public String broadcastMessage(String message) {
        // 단순히 메시지만을 변환. 구독중인 사용자에게 브로드캐스팅함.
        return message;
    }
}
