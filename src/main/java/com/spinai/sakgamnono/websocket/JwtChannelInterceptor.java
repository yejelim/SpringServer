package com.spinai.sakgamnono.websocket;

import com.spinai.sakgamnono.jwt.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * STOMP 메시지(Connect, Subscribe, Send 등) InboundChannel 시
 * 쿠키 기반 JWT를 검증하여 Principal을 설정
 */
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public JwtChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // CONNECT 프레임만 쿠키 파싱
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> cookieHeaders = accessor.getNativeHeader("Cookie");
            if (cookieHeaders != null && !cookieHeaders.isEmpty()) {
                // ex) "ajs_anonymous_id=...; jwtToken=eyJ..."
                String cookieStr = cookieHeaders.get(0);
                String token = parseJwtCookie(cookieStr);
                if (token != null) {
                    try {
                        DecodedJWT decoded = jwtUtil.verifyToken(token);
                        String nickname = decoded.getClaim("nickname").asString();
                        // Principal
                        accessor.setUser(new StompPrincipal(nickname));
                        System.out.println("[JwtChannelInterceptor] STOMP principal = " + nickname);
                    } catch (JWTVerificationException e) {
                        System.out.println("[JwtChannelInterceptor] JWT verify fail. e=" + e);
                        // 검증 실패 -> 그냥 익명으로
                    }
                }
            }
        }

        return message;
    }

    /**
     * Cookie 문자열에서 "jwtToken=..." 부분만 추출
     */
    private String parseJwtCookie(String cookieStr) {
        // 예) "ajs_anonymous_id=45bd4d...; jwtToken=eyJhbGci..."
        // 세미콜론으로 split
        String[] parts = cookieStr.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("jwtToken=")) {
                return part.substring("jwtToken=".length());
            }
        }
        return null;
    }
}
