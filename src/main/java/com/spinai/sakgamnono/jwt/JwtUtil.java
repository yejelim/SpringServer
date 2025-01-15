package com.spinai.sakgamnono.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    
    private static final String SECRET_KEY = "MySuperSecretKey123";
    // 향후 configuration 정의로 옮기기

    // 토큰 유효기간 1day로 설정
    private static final long EXPIRE_TIME_MS = 1000 * 60 * 60 * 24L;

    public String generateToken(Long userId, String nickname) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(EXPIRE_TIME_MS);

        return JWT.create()
                .withIssuer("sakgamnono")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiry))
                .withClaim("userId", userId)
                .withClaim("nickname", nickname)
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    public DecodedJWT verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .withIssuer("sakgamnono")
                .build()
                .verify(token);
    }
}
