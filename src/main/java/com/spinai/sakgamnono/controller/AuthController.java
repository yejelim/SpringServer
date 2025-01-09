package com.spinai.sakgamnono.controller;

import com.spinai.sakgamnono.domain.User;
import com.spinai.sakgamnono.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserRepository userRepo;

    public AuthController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // DTO class (추후 더 구체화)
    static class SignupRequest {
        public String nickname;
        public String password;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        // 1) 닉네임 중복 체크
        Optional<User> existing = userRepo.findByNickname(req.nickname);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Nickname already taken");
        }

        // 2) 새 User 저장
        User user = new User();
        user.setNickname(req.nickname);
        user.setPassword(req.password); // 숫자만 있는지 나중에 검증 로직 추가 가능
        userRepo.save(user);

        return ResponseEntity.ok("Signup success");

    }
}
