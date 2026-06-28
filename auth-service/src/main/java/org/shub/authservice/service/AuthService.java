package org.shub.authservice.service;

import org.shub.authservice.dto.AuthResponse;
import org.shub.authservice.dto.LoginRequest;
import org.shub.authservice.dto.RegisterRequest;
import org.shub.authservice.entity.User;
import org.shub.authservice.exception.DuplicateEmailException;
import org.shub.authservice.exception.InvalidCredentialsException;
import org.shub.authservice.kafka.EventPublisher;
import org.shub.authservice.repository.UserRepository;
import org.shub.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shared.UserRegisteredEvent;

import java.time.Instant;

@Service
@RequiredArgsConstructor

public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EventPublisher eventPublisher;

    public AuthResponse register(RegisterRequest request)
    {
        //Email check
        if(userRepository.existsByEmail(request.email()))
        {
            throw new DuplicateEmailException("Email already exists.");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user);

        //Equivalent of _publishEndpoint.Publish(new UserRegisteredEvent { ... })
        eventPublisher.publish(
                "user.registered",
                user.getId().toString(),
                new UserRegisteredEvent(user.getId(), user.getUsername(), user.getEmail(), Instant.now())
        );

        return new AuthResponse(token, user.getUsername(), user.getId());

    }

    public AuthResponse login(LoginRequest request) {
        // Equivalent of `_context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email)`
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials."));

        // Equivalent of `!BCrypt.Net.BCrypt.Verify(dto.Password, user.PasswordHash)`
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials.");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getId());
    }
}
