package com.example.bankcards.service.impl;

import com.example.bankcards.dto.response.ApplicationResponseDto;
import com.example.bankcards.entity.ApplicationEntity;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.ClientEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.ApplicationEntityRepository;
import com.example.bankcards.repository.CardEntityRepository;
import com.example.bankcards.repository.ClientEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCardServiceTest {

    @Mock
    private CardEntityRepository cardEntityRepository;

    @Mock
    private ClientEntityRepository clientEntityRepository;

    @Mock
    private ApplicationEntityRepository applicationEntityRepository;

    @InjectMocks
    private UserCardService userCardService;

    private UserDetails userDetails;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userDetails = User.withUsername("john")
                .password("secret")
                .authorities("USER")
                .build();
    }

    @Test
    void requestCardBlock_createsApplicationAndBlocksCard() {
        Long cardId = 42L;

        ClientEntity client = ClientEntity.builder()
                .id(userId)
                .login("john")
                .password("secret")
                .build();

        CardEntity card = CardEntity.builder()
                .id(cardId)
                .ownerId(userId)
                .status(CardStatus.ACTIVE)
                .balance(100L)
                .build();

        ApplicationEntity savedApplication = ApplicationEntity.builder()
                .id(UUID.randomUUID())
                .accountId(userId)
                .cardId(cardId)
                .approved(Boolean.FALSE)
                .build();

        when(clientEntityRepository.findByLogin("john")).thenReturn(Optional.of(client));
        when(cardEntityRepository.findByIdAndOwnerId(cardId, userId)).thenReturn(Optional.of(card));
        when(applicationEntityRepository.existsByCardIdAndApprovedIsFalse(cardId)).thenReturn(false);
        when(applicationEntityRepository.save(any(ApplicationEntity.class))).thenReturn(savedApplication);

        ResponseEntity<?> response = userCardService.requestCardBlock(cardId, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(ApplicationResponseDto.class);

        ArgumentCaptor<CardEntity> cardCaptor = ArgumentCaptor.forClass(CardEntity.class);
        verify(cardEntityRepository).save(cardCaptor.capture());
        assertThat(cardCaptor.getValue().getStatus()).isEqualTo(CardStatus.BLOCKED);

        ArgumentCaptor<ApplicationEntity> applicationCaptor = ArgumentCaptor.forClass(ApplicationEntity.class);
        verify(applicationEntityRepository).save(applicationCaptor.capture());
        assertThat(applicationCaptor.getValue().getAccountId()).isEqualTo(userId);
        assertThat(applicationCaptor.getValue().getCardId()).isEqualTo(cardId);
        assertThat(applicationCaptor.getValue().getApproved()).isFalse();
    }

    @Test
    void requestCardBlock_returnsConflictWhenDuplicateRequestExists() {
        Long cardId = 10L;

        ClientEntity client = ClientEntity.builder()
                .id(userId)
                .login("john")
                .password("secret")
                .build();

        when(clientEntityRepository.findByLogin("john")).thenReturn(Optional.of(client));
        when(cardEntityRepository.findByIdAndOwnerId(cardId, userId)).thenReturn(Optional.of(CardEntity.builder()
                .id(cardId)
                .ownerId(userId)
                .status(CardStatus.ACTIVE)
                .build()));
        when(applicationEntityRepository.existsByCardIdAndApprovedIsFalse(cardId)).thenReturn(true);

        ResponseEntity<?> response = userCardService.requestCardBlock(cardId, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(cardEntityRepository, never()).save(any(CardEntity.class));
        verify(applicationEntityRepository, never()).save(any(ApplicationEntity.class));
    }

    @Test
    void requestCardBlock_returnsNotFoundWhenCardMissing() {
        Long cardId = 11L;

        ClientEntity client = ClientEntity.builder()
                .id(userId)
                .login("john")
                .password("secret")
                .build();

        when(clientEntityRepository.findByLogin("john")).thenReturn(Optional.of(client));
        when(cardEntityRepository.findByIdAndOwnerId(cardId, userId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userCardService.requestCardBlock(cardId, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(applicationEntityRepository, never()).save(any(ApplicationEntity.class));
    }
}
