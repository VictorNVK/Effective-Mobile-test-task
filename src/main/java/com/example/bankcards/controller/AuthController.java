package com.example.bankcards.controller;

import com.example.bankcards.dto.request.AuthRequestDto;
import com.example.bankcards.dto.request.RefreshTokenRequestDto;
import com.example.bankcards.dto.response.AuthResponseDto;
import com.example.bankcards.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDto requestDto) {
        return authService.login(requestDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequestDto requestDto) {
        return authService.refresh(requestDto);
    }
}
