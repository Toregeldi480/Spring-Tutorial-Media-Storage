package com.media_storage.user_service.service;

import com.media_storage.user_service.dto.AuthDto;
import com.media_storage.user_service.entity.User;
import com.media_storage.user_service.exception.UserAlreadyExistsException;
import com.media_storage.user_service.repository.UserRepository;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(AuthDto authDto) {
        String username = authDto.getUsername();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, authDto.getPassword()));
        return userRepository.findByUsername(username).orElseThrow();
    }

    public User register(AuthDto authDto) {
        User user = new User();
        String username = authDto.getUsername();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException(String.format("User With Username %s Already Exists", username));
        }

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(authDto.getPassword()));

        return userRepository.save(user);
    }

    public static class TokenCookies {
        private String jwtToken;
        private Duration accessAge;
        private Duration refreshAge;

        public TokenCookies(String jwtToken, Duration accessAge, Duration refreshAge) {
            this.jwtToken = jwtToken;
            this.accessAge = accessAge;
            this.refreshAge = refreshAge;
        }

        public ResponseCookie getAccessToken() {
            return ResponseCookie.from("accessToken", jwtToken)
                    .httpOnly(true)
                    .secure(true)
                    .partitioned(true)
                    .path("/")
                    .maxAge(accessAge)
                    .sameSite("None")
                    .build();
        }
        public ResponseCookie getRefreshToken() {
            return ResponseCookie.from("refreshToken", jwtToken)
                    .httpOnly(true)
                    .secure(true)
                    .partitioned(true)
                    .path("/auth/refresh")
                    .maxAge(refreshAge)
                    .sameSite("None")
                    .build();
        }
    }
}
