package com.spinai.sakgamnono.controller;

import com.spinai.sakgamnono.domain.User;
import com.spinai.sakgamnono.dto.LoginRequest;
import com.spinai.sakgamnono.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import com.spinai.sakgamnono.jwt.JwtUtil;
import org.springframework.http.ResponseCookie;

import java.net.ResponseCache;
import java.time.Duration;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserRepository userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody LoginRequest req) {
        // 1) 닉네임 중복 체크
        Optional<User> existing = userRepo.findByNickname(req.getNickname());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Nickname already taken");
        }

        // 2) 새 User 저장
        User user = new User();
        user.setNickname(req.getNickname());
        user.setPassword(req.getPassword()); // 숫자만 있는지 나중에 검증 로직 추가 가능
        userRepo.save(user);

        return ResponseEntity.ok("Signup success");

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        // 1) 닉네임으로 User 찾기
        Optional<User> userOpt = userRepo.findByNickname(req.getNickname());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        User user = userOpt.get();
        // 2) 비밀번호 체크
        if (!user.getPassword().equals(req.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong password");
        }

        // 3) JWT 생성
        String token = jwtUtil.generateToken(user.getId(), user.getNickname());

        // 4) 쿠키 생성
        ResponseCookie cookie = ResponseCookie.from("jwtToken", token)
            .httpOnly(true)
            .secure(false) // https를 사용할 때는 true로 변경
            .path("/")
            .maxAge(Duration.ofHours(24))
            .sameSite("Lax")
            .build();

        return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(Map.of("message", "로그인 성공 (쿠키 발행 완료)"));
    }

    @RestController
    @RequestMapping("/api/test")
    public class TestController {

      @GetMapping("/secure")
      public ResponseEntity<?> secureHello() {
        // 현재 인증이 되어 있어야 접근 가능
        // (SecurityConfig에서 .anyRequest().authenticated())
          return ResponseEntity.ok("You are authenticated!");
    }
}

}
