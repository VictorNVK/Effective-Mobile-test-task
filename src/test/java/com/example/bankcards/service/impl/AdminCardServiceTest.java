package com.example.bankcards.service.impl;

import com.example.bankcards.entity.ApplicationEntity;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.ApplicationEntityRepository;
import com.example.bankcards.repository.CardEntityRepository;
import com.example.bankcards.repository.ClientEntityRepository;
import com.example.bankcards.util.card_generator.ICardNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCardServiceTest {

    @Mock
    private ApplicationEntityRepository applicationEntityRepository;

    @Mock
    private CardEntityRepository cardEntityRepository;

    @Mock
    private ICardNumberGenerator cardNumberGenerator;

    @Mock
    private ClientEntityRepository clientEntityRepository;

    @InjectMocks
    private AdminCardService adminCardService;

    private UUID applicationId;
    private ApplicationEntity pendingApplication;
    private CardEntity activeCard;

    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        pendingApplication = ApplicationEntity.builder()
                .id(applicationId)
                .accountId(UUID.randomUUID())
                .cardId(99L)
                .approved(Boolean.FALSE)
                .build();

        activeCard = CardEntity.builder()
                .id(99L)
                .status(CardStatus.ACTIVE)
                .build();
    }

    @Test
    void approveApplication_marksApplicationAndCardBlocked() {
        when(applicationEntityRepository.findById(applicationId)).thenReturn(Optional.of(pendingApplication));
        when(applicationEntityRepository.save(any(ApplicationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardEntityRepository.findById(99L)).thenReturn(Optional.of(activeCard));
        when(cardEntityRepository.save(any(CardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = adminCardService.approveApplication(applicationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<ApplicationEntity> applicationCaptor = ArgumentCaptor.forClass(ApplicationEntity.class);
        verify(applicationEntityRepository).save(applicationCaptor.capture());
        assertThat(applicationCaptor.getValue().getApproved()).isTrue();

        ArgumentCaptor<CardEntity> cardCaptor = ArgumentCaptor.forClass(CardEntity.class);
        verify(cardEntityRepository).save(cardCaptor.capture());
        assertThat(cardCaptor.getValue().getStatus()).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    void approveApplication_returnsNotFoundWhenMissing() {
        when(applicationEntityRepository.findById(applicationId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = adminCardService.approveApplication(applicationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(cardEntityRepository, never()).save(any(CardEntity.class));
        verify(applicationEntityRepository, never()).save(any(ApplicationEntity.class));
    }

    @Test
    void approveApplication_returnsExistingWhenAlreadyApproved() {
        ApplicationEntity approvedApplication = ApplicationEntity.builder()
                .id(applicationId)
                .accountId(UUID.randomUUID())
                .cardId(100L)
                .approved(Boolean.TRUE)
                .build();

        when(applicationEntityRepository.findById(applicationId)).thenReturn(Optional.of(approvedApplication));

        ResponseEntity<?> response = adminCardService.approveApplication(applicationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(applicationEntityRepository, never()).save(any(ApplicationEntity.class));
        verify(cardEntityRepository, never()).save(any(CardEntity.class));
    }

    @Test
    void getApplications_returnsAllWhenFilterNotProvided() {
        ApplicationEntity application = ApplicationEntity.builder()
                .id(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .cardId(1L)
                .approved(Boolean.FALSE)
                .build();

        when(applicationEntityRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(application)));

        ResponseEntity<?> response = adminCardService.getApplications(0, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        List<?> body = (List<?>) response.getBody();
        assertThat(body).hasSize(1);

        verify(applicationEntityRepository).findAll(any(PageRequest.class));
        verify(applicationEntityRepository, never()).findAllByApproved(anyBoolean(), any(PageRequest.class));
    }

    @Test
    void getApplications_filtersByApprovalFlag() {
        ApplicationEntity application = ApplicationEntity.builder()
                .id(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .cardId(2L)
                .approved(Boolean.TRUE)
                .build();

        when(applicationEntityRepository.findAllByApproved(eq(Boolean.TRUE), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(application)));

        ResponseEntity<?> response = adminCardService.getApplications(1, Boolean.TRUE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        List<?> body = (List<?>) response.getBody();
        assertThat(body).hasSize(1);

        verify(applicationEntityRepository).findAllByApproved(eq(Boolean.TRUE), any(PageRequest.class));
        verify(applicationEntityRepository, never()).findAll(any(PageRequest.class));
    }
}
