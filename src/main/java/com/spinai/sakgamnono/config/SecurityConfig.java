package com.spinai.sakgamnono.config;

import com.spinai.sakgamnono.jwt.JWTAuthFilter;
import com.spinai.sakgamnono.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 아래 3개는 Security 설정에 필요한 import
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;  // <-- (중요) 세션 정책

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // .signup.html과 같은 정적 파일의 접근은 허용
                .requestMatchers("/**").permitAll()  
                .requestMatchers("/api/auth/**").permitAll() 
                .anyRequest().authenticated()
            )
            .sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // <-- (중요) 
            )
            .addFilterBefore(new JWTAuthFilter(jwtUtil), 
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
