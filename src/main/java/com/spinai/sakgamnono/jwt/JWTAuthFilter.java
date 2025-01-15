package com.spinai.sakgamnono.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.StringUtils;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.spinai.sakgamnono.jwt.JwtUtil; // your own


public class JWTAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JWTAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil =  jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {

        // 1) 쿠키 배열 가져오기
        Cookie[] cookies  = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("jwtToken")) {
                    String token = c.getValue();
                    try {
                        DecodedJWT decoded = jwtUtil.verifyToken(token);
                        // 토큰 유효하면 SecurityContext에 인증정보 설정
                        Long userId = decoded.getClaim("userId").asLong();
                        String nickname = decoded.getClaim("nickname").asString();

                        UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                userId, // principal
                                null,
                                Collections.emptyList() // 권한 등 필요 시 지정
                            );
                        SecurityContextHolder.getContext().setAuthentication(auth);

                    } catch (JWTVerificationException e) {
                        // 토큰 검증 실패 → 401
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                    break; // 쿠키 찾았으면 break
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
