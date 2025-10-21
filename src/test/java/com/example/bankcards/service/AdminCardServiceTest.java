package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardCreateRequestDto;
import com.example.bankcards.dto.response.CardCreateResponseDto;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardEntityRepository;
import com.example.bankcards.repository.ClientEntityRepository;
import com.example.bankcards.service.impl.AdminCardService;
import com.example.bankcards.util.card_generator.CardGenerationResult;
import com.example.bankcards.util.card_generator.ICardNumberGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCardServiceTest {

    @Mock
    private CardEntityRepository cardEntityRepository;

    @Mock
    private ICardNumberGenerator cardNumberGenerator;

    @Mock
    private ClientEntityRepository clientEntityRepository;

    @InjectMocks
    private AdminCardService adminCardService;

    private static CardCreateRequestDto buildRequest(UUID ownerId) {
        CardCreateRequestDto requestDto = new CardCreateRequestDto();
        requestDto.setOwnerId(ownerId);
        return requestDto;
    }

    private static CardGenerationResult buildGenerationResult() {
        return new CardGenerationResult(
                "4123456789012345",
                "encrypted-pan",
                "a".repeat(64),
                "2345",
                12,
                2030
        );
    }

    @Test
    @DisplayName("createCard returns CREATED response with payload when client exists")
    void createCardSuccess() {
        UUID ownerId = UUID.randomUUID();
        CardCreateRequestDto request = buildRequest(ownerId);
        CardGenerationResult generated = buildGenerationResult();

        when(clientEntityRepository.existsClientEntityById(ownerId)).thenReturn(true);
        when(cardNumberGenerator.generate()).thenReturn(generated);
        when(cardEntityRepository.save(any(CardEntity.class))).thenAnswer(invocation -> {
            CardEntity toSave = invocation.getArgument(0);
            return CardEntity.builder()
                    .id(100L)
                    .ownerId(toSave.getOwnerId())
                    .panEncrypted(toSave.getPanEncrypted())
                    .last4(toSave.getLast4())
                    .panHash(toSave.getPanHash())
                    .status(toSave.getStatus())
                    .balance(toSave.getBalance())
                    .expiryMonth(toSave.getExpiryMonth())
                    .expiryYear(toSave.getExpiryYear())
                    .build();
        });

        ResponseEntity<?> response = adminCardService.createCard(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertInstanceOf(CardCreateResponseDto.class, response.getBody());
        CardCreateResponseDto body = (CardCreateResponseDto) response.getBody();

        assertThat(body.getId()).isEqualTo(100L);
        assertThat(body.getOwnerId()).isEqualTo(ownerId);
        assertThat(body.getMaskedPan()).isEqualTo("**** **** **** 2345");
        assertThat(body.getExpiryMonth()).isEqualTo(12);
        assertThat(body.getExpiryYear()).isEqualTo(2030);
        assertThat(body.getPlainPan()).isEqualTo(generated.plainPan());

        ArgumentCaptor<CardEntity> captor = ArgumentCaptor.forClass(CardEntity.class);
        verify(cardEntityRepository).save(captor.capture());
        CardEntity persisted = captor.getValue();

        assertThat(persisted.getOwnerId()).isEqualTo(ownerId);
        assertThat(persisted.getPanEncrypted()).isEqualTo(generated.encryptedPan());
        assertThat(persisted.getPanHash()).isEqualTo(generated.panHash());
        assertThat(persisted.getLast4()).isEqualTo(generated.last4());
        assertThat(persisted.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(persisted.getBalance()).isEqualTo(0L);

        verify(cardNumberGenerator).generate();
    }

    @Test
    @DisplayName("createCard returns 204 No Content when client not found")
    void createCardClientMissing() {
        UUID ownerId = UUID.randomUUID();
        CardCreateRequestDto request = buildRequest(ownerId);

        when(clientEntityRepository.existsClientEntityById(ownerId)).thenReturn(false);

        ResponseEntity<?> response = adminCardService.createCard(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(cardNumberGenerator, never()).generate();
        verify(cardEntityRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCard returns 409 Conflict when generated PAN already exists")
    void createCardDuplicatePan() {
        UUID ownerId = UUID.randomUUID();
        CardCreateRequestDto request = buildRequest(ownerId);
        CardGenerationResult generated = buildGenerationResult();

        when(clientEntityRepository.existsClientEntityById(ownerId)).thenReturn(true);
        when(cardNumberGenerator.generate()).thenReturn(generated);
        when(cardEntityRepository.save(any(CardEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseEntity<?> response = adminCardService.createCard(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Card with generated number already exists", response.getBody());
    }

    @Test
    @DisplayName("createCard returns 500 Internal Server Error on unexpected failures")
    void createCardUnexpectedError() {
        UUID ownerId = UUID.randomUUID();
        CardCreateRequestDto request = buildRequest(ownerId);

        when(clientEntityRepository.existsClientEntityById(ownerId)).thenReturn(true);
        when(cardNumberGenerator.generate()).thenThrow(new RuntimeException("generator failed"));

        ResponseEntity<?> response = adminCardService.createCard(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("generator failed", response.getBody());
    }
}
