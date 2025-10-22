package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardCreateRequestDto;
import com.example.bankcards.dto.request.CardUpdateRequestDto;
import com.example.bankcards.dto.response.CardCreateResponseDto;
import com.example.bankcards.dto.response.CardResponseDto;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;
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

    private static CardEntity buildCardEntity(Long id, UUID ownerId, CardStatus status) {
        return CardEntity.builder()
                .id(id)
                .ownerId(ownerId)
                .status(status)
                .panEncrypted("encrypted")
                .panHash("hash-" + id)
                .last4("2345")
                .balance(0L)
                .expiryMonth(12)
                .expiryYear(LocalDate.now().getYear() + 1)
                .build();
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

    @Test
    @DisplayName("activateCard switches status to ACTIVE when card exists")
    void activateCardSuccess() {
        Long cardId = 55L;
        UUID ownerId = UUID.randomUUID();

        CardEntity stored = CardEntity.builder()
                .id(cardId)
                .ownerId(ownerId)
                .status(CardStatus.BLOCKED)
                .panEncrypted("enc")
                .panHash("hash")
                .last4("9876")
                .balance(500L)
                .expiryMonth(8)
                .expiryYear(2031)
                .build();

        when(cardEntityRepository.findById(cardId)).thenReturn(Optional.of(stored));
        when(cardEntityRepository.save(any(CardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = adminCardService.activateCard(cardId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(CardResponseDto.class, response.getBody());
        CardResponseDto body = (CardResponseDto) response.getBody();
        CardResponseDto expectedDto = CardResponseDto.from(stored);
        assertThat(body).usingRecursiveComparison().isEqualTo(expectedDto);

        ArgumentCaptor<CardEntity> captor = ArgumentCaptor.forClass(CardEntity.class);
        verify(cardEntityRepository).save(captor.capture());
        assertEquals(CardStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("blockCard returns current data without save when already BLOCKED")
    void blockCardNoChange() {
        Long cardId = 89L;

        CardEntity stored = CardEntity.builder()
                .id(cardId)
                .ownerId(UUID.randomUUID())
                .status(CardStatus.BLOCKED)
                .panEncrypted("enc")
                .panHash("hash")
                .last4("1111")
                .expiryMonth(1)
                .expiryYear(2030)
                .build();

        when(cardEntityRepository.findById(cardId)).thenReturn(Optional.of(stored));

        ResponseEntity<?> response = adminCardService.blockCard(cardId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(CardResponseDto.class, response.getBody());
        CardResponseDto body = (CardResponseDto) response.getBody();
        assertThat(body).usingRecursiveComparison().isEqualTo(CardResponseDto.from(stored));

        verify(cardEntityRepository, never()).save(any(CardEntity.class));
    }

    @Test
    @DisplayName("blockCard returns 404 when card not found")
    void blockCardNotFound() {
        Long cardId = 999L;
        when(cardEntityRepository.findById(cardId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = adminCardService.blockCard(cardId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("activateCard returns 500 when repository throws")
    void activateCardError() {
        Long cardId = 101L;
        when(cardEntityRepository.findById(cardId)).thenThrow(new RuntimeException("db error"));

        ResponseEntity<?> response = adminCardService.activateCard(cardId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("db error", response.getBody());
    }

    @Test
    @DisplayName("updateCard updates mutable fields and preserves existing PAN data")
    void updateCardSuccess() {
        Long cardId = 77L;
        UUID originalOwner = UUID.randomUUID();
        CardEntity existing = buildCardEntity(cardId, originalOwner, CardStatus.ACTIVE);

        UUID newOwner = UUID.randomUUID();
        LocalDate futureDate = LocalDate.now().plusYears(2);

        CardUpdateRequestDto requestDto = new CardUpdateRequestDto();
        requestDto.setOwnerId(newOwner);
        requestDto.setExpiryMonth(futureDate.getMonthValue());
        requestDto.setExpiryYear(futureDate.getYear());
        requestDto.setStatus(CardStatus.BLOCKED);

        when(cardEntityRepository.findById(cardId)).thenReturn(Optional.of(existing));
        when(cardEntityRepository.save(any(CardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = adminCardService.updateCard(requestDto, cardId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(CardResponseDto.class, response.getBody());

        ArgumentCaptor<CardEntity> captor = ArgumentCaptor.forClass(CardEntity.class);
        verify(cardEntityRepository).save(captor.capture());
        CardEntity updated = captor.getValue();

        assertThat(updated.getOwnerId()).isEqualTo(newOwner);
        assertThat(updated.getStatus()).isEqualTo(CardStatus.BLOCKED);
        assertThat(updated.getExpiryMonth()).isEqualTo(futureDate.getMonthValue());
        assertThat(updated.getExpiryYear()).isEqualTo(futureDate.getYear());
        // Immutable PAN fields remain unchanged
        assertThat(updated.getPanEncrypted()).isEqualTo(existing.getPanEncrypted());
        assertThat(updated.getPanHash()).isEqualTo(existing.getPanHash());
        assertThat(updated.getLast4()).isEqualTo(existing.getLast4());
    }

    @Test
    @DisplayName("updateCard returns 404 when entity does not exist")
    void updateCardNotFound() {
        Long cardId = 999L;
        when(cardEntityRepository.findById(cardId)).thenReturn(Optional.empty());

        CardUpdateRequestDto requestDto = new CardUpdateRequestDto();
        requestDto.setExpiryMonth(LocalDate.now().plusMonths(1).getMonthValue());
        requestDto.setExpiryYear(LocalDate.now().plusMonths(1).getYear());

        ResponseEntity<?> response = adminCardService.updateCard(requestDto, cardId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(cardEntityRepository, never()).save(any(CardEntity.class));
    }

    @Test
    @DisplayName("updateCard returns 400 when expiry date already passed")
    void updateCardInvalidDate() {
        Long cardId = 5L;
        CardEntity existing = buildCardEntity(cardId, UUID.randomUUID(), CardStatus.ACTIVE);
        when(cardEntityRepository.findById(cardId)).thenReturn(Optional.of(existing));

        LocalDate past = LocalDate.now().minusMonths(2);
        CardUpdateRequestDto requestDto = new CardUpdateRequestDto();
        requestDto.setExpiryMonth(past.getMonthValue());
        requestDto.setExpiryYear(past.getYear());

        ResponseEntity<?> response = adminCardService.updateCard(requestDto, cardId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(cardEntityRepository, never()).save(any(CardEntity.class));
    }

    @Test
    @DisplayName("getAllCards returns mapped DTO list for requested page")
    void getAllCardsReturnsList() {
        CardEntity card = buildCardEntity(10L, UUID.randomUUID(), CardStatus.ACTIVE);
        when(cardEntityRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(card)));

        ResponseEntity<?> response = adminCardService.getAllCards(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(java.util.List.class, response.getBody());

        @SuppressWarnings("unchecked")
        java.util.List<CardResponseDto> content = (java.util.List<CardResponseDto>) response.getBody();
        assertThat(content).hasSize(1);
        assertThat(content.get(0).getId()).isEqualTo(card.getId());
    }
}
