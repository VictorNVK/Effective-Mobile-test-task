package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateUserRequestDto;
import com.example.bankcards.dto.response.UserGetResponseDto;
import com.example.bankcards.entity.ClientEntity;
import com.example.bankcards.repository.ClientEntityRepository;
import com.example.bankcards.service.impl.AdminUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private ClientEntityRepository clientEntityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    @DisplayName("deleteUser removes user when exists")
    void deleteUserSuccess() {
        UUID id = UUID.randomUUID();
        when(clientEntityRepository.existsClientEntityById(id)).thenReturn(true);

        ResponseEntity<?> response = adminUserService.deleteUser(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(clientEntityRepository).deleteClientEntityById(id);
    }

    @Test
    @DisplayName("deleteUser returns 404 when user missing")
    void deleteUserNotFound() {
        UUID id = UUID.randomUUID();
        when(clientEntityRepository.existsClientEntityById(id)).thenReturn(false);

        ResponseEntity<?> response = adminUserService.deleteUser(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(clientEntityRepository, times(0)).deleteClientEntityById(any());
    }

    @Test
    @DisplayName("addUser creates new user when login free")
    void addUserSuccess() {
        CreateUserRequestDto dto = new CreateUserRequestDto();
        dto.setLogin("new.user");
        dto.setPassword("secret");

        ClientEntity saved = ClientEntity.builder()
                .id(UUID.randomUUID())
                .login(dto.getLogin())
                .password("encoded")
                .build();

        when(clientEntityRepository.findByLogin(dto.getLogin())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(clientEntityRepository.save(any(ClientEntity.class))).thenReturn(saved);

        ResponseEntity<?> response = adminUserService.addUser(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertInstanceOf(UserGetResponseDto.class, response.getBody());
        UserGetResponseDto body = (UserGetResponseDto) response.getBody();
        assertThat(body.getLogin()).isEqualTo(dto.getLogin());

        ArgumentCaptor<ClientEntity> captor = ArgumentCaptor.forClass(ClientEntity.class);
        verify(clientEntityRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
    }

    @Test
    @DisplayName("addUser returns 409 when login exists")
    void addUserDuplicate() {
        CreateUserRequestDto dto = new CreateUserRequestDto();
        dto.setLogin("existing");
        dto.setPassword("pass");

        when(clientEntityRepository.findByLogin(dto.getLogin()))
                .thenReturn(Optional.of(ClientEntity.builder().id(UUID.randomUUID()).build()));

        ResponseEntity<?> response = adminUserService.addUser(dto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(clientEntityRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("updateUser updates mutable fields")
    void updateUserSuccess() {
        UUID id = UUID.randomUUID();
        CreateUserRequestDto dto = new CreateUserRequestDto();
        dto.setLogin("updated");
        dto.setPassword("newpass");

        ClientEntity existing = ClientEntity.builder()
                .id(id)
                .login("old")
                .password("encoded")
                .build();

        when(clientEntityRepository.getClientEntityById(id)).thenReturn(Optional.of(existing));
        when(clientEntityRepository.findByLogin(dto.getLogin())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("new-encoded");
        when(clientEntityRepository.save(any(ClientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = adminUserService.updateUser(id, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserGetResponseDto body = (UserGetResponseDto) response.getBody();
        assertThat(body.getLogin()).isEqualTo("updated");

        ArgumentCaptor<ClientEntity> captor = ArgumentCaptor.forClass(ClientEntity.class);
        verify(clientEntityRepository).save(captor.capture());
        ClientEntity saved = captor.getValue();
        assertThat(saved.getLogin()).isEqualTo("updated");
        assertThat(saved.getPassword()).isEqualTo("new-encoded");
    }

    @Test
    @DisplayName("updateUser returns 404 when user not found")
    void updateUserNotFound() {
        UUID id = UUID.randomUUID();
        when(clientEntityRepository.getClientEntityById(id)).thenReturn(Optional.empty());

        ResponseEntity<?> response = adminUserService.updateUser(id, new CreateUserRequestDto());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(clientEntityRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("updateUser returns 409 when login already in use by another user")
    void updateUserDuplicateLogin() {
        UUID id = UUID.randomUUID();
        CreateUserRequestDto dto = new CreateUserRequestDto();
        dto.setLogin("taken");

        ClientEntity existing = ClientEntity.builder()
                .id(id)
                .login("old")
                .password("encoded")
                .build();

        when(clientEntityRepository.getClientEntityById(id)).thenReturn(Optional.of(existing));
        when(clientEntityRepository.findByLogin("taken"))
                .thenReturn(Optional.of(ClientEntity.builder().id(UUID.randomUUID()).build()));

        ResponseEntity<?> response = adminUserService.updateUser(id, dto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(clientEntityRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("getUser returns dto when found")
    void getUserSuccess() {
        UUID id = UUID.randomUUID();
        ClientEntity entity = ClientEntity.builder()
                .id(id)
                .login("user")
                .password("encoded")
                .build();

        when(clientEntityRepository.getClientEntityById(id)).thenReturn(Optional.of(entity));

        ResponseEntity<?> response = adminUserService.getUser(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(UserGetResponseDto.class, response.getBody());
        UserGetResponseDto body = (UserGetResponseDto) response.getBody();
        assertThat(body.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("getUser returns 404 when missing")
    void getUserNotFound() {
        UUID id = UUID.randomUUID();
        when(clientEntityRepository.getClientEntityById(id)).thenReturn(Optional.empty());

        ResponseEntity<?> response = adminUserService.getUser(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("getUsers returns paginated list")
    void getUsersSuccess() {
        ClientEntity entity = ClientEntity.builder()
                .id(UUID.randomUUID())
                .login("user")
                .password("encoded")
                .build();

        Page<ClientEntity> page = new PageImpl<>(List.of(entity));
        when(clientEntityRepository.findAll(any(Pageable.class))).thenReturn(page);

        ResponseEntity<?> response = adminUserService.getUsers(0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(List.class, response.getBody());
        @SuppressWarnings("unchecked")
        List<UserGetResponseDto> content = (List<UserGetResponseDto>) response.getBody();
        assertThat(content).hasSize(1);
        assertThat(content.get(0).getLogin()).isEqualTo("user");
    }
}
