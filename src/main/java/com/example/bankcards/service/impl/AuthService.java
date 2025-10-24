package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.AuthRequestDto;
import com.example.bankcards.dto.request.RefreshTokenRequestDto;
import com.example.bankcards.dto.response.AuthResponseDto;
import com.example.bankcards.entity.AdminEntity;
import com.example.bankcards.entity.ClientEntity;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.repository.AdminEntityRepository;
import com.example.bankcards.repository.ClientEntityRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.JwtTokenType;
import com.example.bankcards.exception.JwtValidationException;
import com.example.bankcards.service.IAuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AdminEntityRepository adminRepository;
    private final ClientEntityRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public ResponseEntity<?> login(AuthRequestDto request) {
        String username;
        String encodedPassword;
        RoleType role = request.getRole();

        if (role == RoleType.ADMIN) {
            AdminEntity admin = adminRepository.findByUsername(request.getLogin())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
            username = admin.getUsername();
            encodedPassword = admin.getPassword();
        } else {
            ClientEntity client = clientRepository.findByLogin(request.getLogin())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
            username = client.getLogin();
            encodedPassword = client.getPassword();
        }

        if (!passwordEncoder.matches(request.getPassword(), encodedPassword)) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(username, role);
        String refreshToken = jwtService.generateRefreshToken(username, role);

        AuthResponseDto authResponseDto = AuthResponseDto.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(jwtService.getAccessTokenTtl())
                .refreshToken(refreshToken)
                .refreshTokenExpiresIn(jwtService.getRefreshTokenTtl())
                .tokenType("Bearer")
                .build();
        return ResponseEntity.ok(authResponseDto);
    }

    @Override
    public ResponseEntity<?> refresh(RefreshTokenRequestDto request) {
        Claims claims = jwtService.parseClaims(request.getRefreshToken());
        if (jwtService.extractTokenType(claims) != JwtTokenType.REFRESH) {
            throw new JwtValidationException("Provided token is not a refresh token");
        }
        if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
            throw new JwtValidationException("Refresh token is expired");
        }

        String username = claims.getSubject();
        if (username == null) {
            throw new JwtValidationException("Token subject is empty");
        }

        RoleType role = jwtService.extractRole(claims);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails, JwtTokenType.REFRESH)) {
            throw new JwtValidationException("Refresh token is not valid");
        }

        String newAccessToken = jwtService.generateAccessToken(username, role);
        String newRefreshToken = jwtService.generateRefreshToken(username, role);

        AuthResponseDto authResponseDto = AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .accessTokenExpiresIn(jwtService.getAccessTokenTtl())
                .refreshToken(newRefreshToken)
                .refreshTokenExpiresIn(jwtService.getRefreshTokenTtl())
                .tokenType("Bearer")
                .build();
        return ResponseEntity.ok(authResponseDto);
    }
}
