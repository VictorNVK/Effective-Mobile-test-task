package com.example.bankcards.service;

import com.example.bankcards.dto.request.AuthRequestDto;
import com.example.bankcards.dto.request.RefreshTokenRequestDto;
import com.example.bankcards.dto.response.AuthResponseDto;
import org.springframework.http.ResponseEntity;

public interface IAuthService {

    ResponseEntity<?> login(AuthRequestDto request);

    ResponseEntity<?> refresh(RefreshTokenRequestDto request);
}
