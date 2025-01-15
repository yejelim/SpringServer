package com.spinai.sakgamnono.websocket;

import com.spinai.sakgamnono.jwt.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.List;

/**
 * STOMP 메시지(Connect, Subscribe, Send 등)가 InboundChannel을 통과할 때,
 * JWT를 검증하여 Principal을 설정하는 Interceptor 예시
 */
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public JwtChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // STOMP 헤더 접근
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // CONNECT 프레임에서 Authorization (혹은 Cookie) 헤더를 찾는 예시
            // 1) Authorization 헤더 방식
            // List<String> authHeaders = accessor.getNativeHeader("Authorization");
            // 2) 쿠키 방식을 쓸 수도 있음
            // List<String> cookieHeaders = accessor.getNativeHeader("Cookie");

            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String authHeader = authHeaders.get(0); // ex) "Bearer eyJ..."
                if (authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    try {
                        DecodedJWT decoded = jwtUtil.verifyToken(token);
                        // userId or nickname 
                        String nickname = decoded.getClaim("nickname").asString();

                        // Principal 설정
                        StompPrincipal principal = new StompPrincipal(nickname);
                        accessor.setUser(principal);

                    } catch (JWTVerificationException e) {
                        // 토큰 검증 실패 → CONNECT 거부
                        // STOMP에서 에러 프레임 or 간단히 null 리턴
                        return null;
                    }
                }
            }
            // Authorization 헤더가 없으면 => 익명( principal=null )
        }

        return message;
    }
}
