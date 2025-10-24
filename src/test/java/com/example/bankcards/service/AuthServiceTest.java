package com.example.bankcards.service;

import com.example.bankcards.dto.request.AuthRequestDto;
import com.example.bankcards.dto.request.RefreshTokenRequestDto;
import com.example.bankcards.dto.response.AuthResponseDto;
import com.example.bankcards.entity.AdminEntity;
import com.example.bankcards.entity.ClientEntity;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.exception.JwtValidationException;
import com.example.bankcards.repository.AdminEntityRepository;
import com.example.bankcards.repository.ClientEntityRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.JwtTokenType;
import com.example.bankcards.service.impl.AuthService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AdminEntityRepository adminRepository;
    @Mock
    private ClientEntityRepository clientRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("login issues tokens for admin when credentials are valid")
    void loginAdminSuccess() {
        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setLogin("admin@test.com");
        requestDto.setPassword("password");
        requestDto.setRole(RoleType.ADMIN);

        AdminEntity admin = AdminEntity.builder()
                .id(1L)
                .username("admin@test.com")
                .password("encoded")
                .build();

        when(adminRepository.findByUsername(requestDto.getLogin())).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches(requestDto.getPassword(), admin.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(admin.getUsername(), RoleType.ADMIN)).thenReturn("access");
        when(jwtService.generateRefreshToken(admin.getUsername(), RoleType.ADMIN)).thenReturn("refresh");
        when(jwtService.getAccessTokenTtl()).thenReturn(900L);
        when(jwtService.getRefreshTokenTtl()).thenReturn(2592000L);

        ResponseEntity<?> response = authService.login(requestDto);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(AuthResponseDto.class);
        AuthResponseDto body = (AuthResponseDto) response.getBody();
        assertThat(body.getAccessToken()).isEqualTo("access");
        assertThat(body.getRefreshToken()).isEqualTo("refresh");
        assertThat(body.getTokenType()).isEqualTo("Bearer");
        assertThat(body.getAccessTokenExpiresIn()).isEqualTo(900L);
        assertThat(body.getRefreshTokenExpiresIn()).isEqualTo(2592000L);
    }

    @Test
    @DisplayName("login throws when password is invalid")
    void loginInvalidPassword() {
        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setLogin("user@test.com");
        requestDto.setPassword("bad");
        requestDto.setRole(RoleType.USER);

        ClientEntity client = ClientEntity.builder()
                .id(java.util.UUID.randomUUID())
                .login("user@test.com")
                .password("encoded")
                .build();

        when(clientRepository.findByLogin(requestDto.getLogin())).thenReturn(Optional.of(client));
        when(passwordEncoder.matches(requestDto.getPassword(), client.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(requestDto));
    }

    @Test
    @DisplayName("refresh issues new tokens when refresh token is valid")
    void refreshSuccess() {
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
        requestDto.setRefreshToken("refresh-token");

        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("admin@test.com");
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 60_000));

        when(jwtService.parseClaims(requestDto.getRefreshToken())).thenReturn(claims);
        when(jwtService.extractTokenType(claims)).thenReturn(JwtTokenType.REFRESH);
        when(jwtService.extractRole(claims)).thenReturn(RoleType.ADMIN);
        when(userDetailsService.loadUserByUsername("admin@test.com"))
                .thenReturn(User.withUsername("admin@test.com").password("encoded").roles("ADMIN").build());
        when(jwtService.isTokenValid(eq(requestDto.getRefreshToken()), any(), eq(JwtTokenType.REFRESH))).thenReturn(true);
        when(jwtService.generateAccessToken("admin@test.com", RoleType.ADMIN)).thenReturn("new-access");
        when(jwtService.generateRefreshToken("admin@test.com", RoleType.ADMIN)).thenReturn("new-refresh");
        when(jwtService.getAccessTokenTtl()).thenReturn(900L);
        when(jwtService.getRefreshTokenTtl()).thenReturn(2592000L);

        ResponseEntity<?> response = authService.refresh(requestDto);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(AuthResponseDto.class);
        AuthResponseDto body = (AuthResponseDto) response.getBody();
        assertThat(body.getAccessToken()).isEqualTo("new-access");
        assertThat(body.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    @DisplayName("refresh throws when refresh token has wrong type")
    void refreshInvalidType() {
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
        requestDto.setRefreshToken("bad-token");

        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(jwtService.parseClaims(requestDto.getRefreshToken())).thenReturn(claims);
        when(jwtService.extractTokenType(claims)).thenReturn(JwtTokenType.ACCESS);

        assertThrows(JwtValidationException.class, () -> authService.refresh(requestDto));
    }
}
