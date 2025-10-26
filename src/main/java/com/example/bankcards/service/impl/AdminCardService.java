package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.CardCreateRequestDto;
import com.example.bankcards.dto.request.CardUpdateRequestDto;
import com.example.bankcards.dto.response.ApplicationResponseDto;
import com.example.bankcards.dto.response.CardCreateResponseDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.entity.ApplicationEntity;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.ApplicationEntityRepository;
import com.example.bankcards.repository.CardEntityRepository;
import com.example.bankcards.repository.ClientEntityRepository;
import com.example.bankcards.service.IAdminCardService;
import com.example.bankcards.util.card_generator.CardGenerationResult;
import com.example.bankcards.util.card_generator.ICardNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminCardService implements IAdminCardService {

    private static final int PAGE_SIZE = 10;

    private final ApplicationEntityRepository applicationEntityRepository;
    private final CardEntityRepository cardEntityRepository;
    private final ICardNumberGenerator cardNumberGenerator;
    private final ClientEntityRepository clientEntityRepository;

    @Override
    public ResponseEntity<?> createCard(CardCreateRequestDto cardCreateRequestDto) {
        try {
            UUID ownerId = cardCreateRequestDto.getOwnerId();
            if (ownerId != null && !clientEntityRepository.existsClientEntityById(ownerId)) {
                return ResponseEntity.noContent().build();
            }

            CardGenerationResult generated = cardNumberGenerator.generate();

            CardEntity cardEntity = CardEntity.builder()
                    .ownerId(ownerId)
                    .panEncrypted(generated.encryptedPan())
                    .last4(generated.last4())
                    .panHash(generated.panHash())
                    .status(CardStatus.ACTIVE)
                    .balance(0L)
                    .expiryMonth(generated.expiryMonth())
                    .expiryYear(generated.expiryYear())
                    .build();

            CardEntity saved = cardEntityRepository.save(cardEntity);

            return new ResponseEntity<>(CardCreateResponseDto.from(saved, generated.plainPan()), HttpStatus.CREATED);

        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("Card with generated number already exists", HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteCard(Long id) {
        try {
            if(cardEntityRepository.existsById(id)) {
                cardEntityRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.OK);
            }else {
                return new ResponseEntity<>("Card with id " + id + " does not exist", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
           return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateCard(CardUpdateRequestDto updateRequestDto, Long id) {
        try {
            Optional<CardEntity> cardEntityOptional = cardEntityRepository.findById(id);
            if (cardEntityOptional.isEmpty()) {
                return new ResponseEntity<>("Card with id " + id + " does not exist", HttpStatus.NOT_FOUND);
            }

            if (!updateRequestDto.isExpiryDateValid()) {
                return new ResponseEntity<>("Incorrect date!", HttpStatus.BAD_REQUEST);
            }

            CardEntity cardEntity = cardEntityOptional.get();

            if (updateRequestDto.getOwnerId() != null) {
                cardEntity.setOwnerId(updateRequestDto.getOwnerId());
            }
            cardEntity.setExpiryMonth(updateRequestDto.getExpiryMonth());
            cardEntity.setExpiryYear(updateRequestDto.getExpiryYear());
            if (updateRequestDto.getStatus() != null) {
                cardEntity.setStatus(updateRequestDto.getStatus());
            }

            CardEntity saved = cardEntityRepository.save(cardEntity);
            return ResponseEntity.ok().body(CardResponseDto.from(saved));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> blockCard(Long cardId) {
        return updateCardStatus(cardId, CardStatus.BLOCKED);
    }

    @Override
    public ResponseEntity<?> activateCard(Long cardId) {
        return updateCardStatus(cardId, CardStatus.ACTIVE);
    }

    @Override
    public ResponseEntity<?> getCard(Long id) {
        try {
            Optional<CardEntity> cardEntity = cardEntityRepository.findById(id);
            if(cardEntity.isPresent()) {
                return ResponseEntity.ok(CardResponseDto.from(cardEntity.get()));
            }else {
                return new ResponseEntity<>("Card with id " + id + " does not exist", HttpStatus.NOT_FOUND);
            }
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllCards(Integer page) {
        try {
            int pageNumber = (page == null || page < 0) ? 0 : page;
            PageRequest pageRequest = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by("id").ascending());
            Page<CardResponseDto> cardsPage = cardEntityRepository.findAll(pageRequest)
                    .map(CardResponseDto::from);

            return ResponseEntity.ok(cardsPage.getContent());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> approveApplication(UUID applicationId) {
        try {
            if (applicationId == null) {
                return new ResponseEntity<>("Application identifier must be provided", HttpStatus.BAD_REQUEST);
            }

            Optional<ApplicationEntity> applicationOptional = applicationEntityRepository.findById(applicationId);
            if (applicationOptional.isEmpty()) {
                return new ResponseEntity<>("Application not found", HttpStatus.NOT_FOUND);
            }

            ApplicationEntity application = applicationOptional.get();
            if (Boolean.TRUE.equals(application.getApproved())) {
                return ResponseEntity.ok(ApplicationResponseDto.from(application));
            }

            application.setApproved(Boolean.TRUE);
            ApplicationEntity savedApplication = applicationEntityRepository.save(application);

            cardEntityRepository.findById(savedApplication.getCardId())
                    .ifPresent(card -> {
                        if (card.getStatus() != CardStatus.BLOCKED) {
                            card.setStatus(CardStatus.BLOCKED);
                            cardEntityRepository.save(card);
                        }
                    });

            return ResponseEntity.ok(ApplicationResponseDto.from(savedApplication));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getApplications(Integer page, Boolean approved) {
        try {
            int pageNumber = (page == null || page < 0) ? 0 : page;
            PageRequest pageRequest = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by("id").ascending());
            Page<ApplicationResponseDto> applicationPage;
            if (approved == null) {
                applicationPage = applicationEntityRepository.findAll(pageRequest)
                        .map(ApplicationResponseDto::from);
            } else {
                applicationPage = applicationEntityRepository.findAllByApproved(approved, pageRequest)
                    .map(ApplicationResponseDto::from);
            }
            return ResponseEntity.ok(applicationPage.getContent());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<?> updateCardStatus(Long cardId, CardStatus targetStatus) {
        try {
            Optional<CardEntity> cardOptional = cardEntityRepository.findById(cardId);
            if (cardOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            CardEntity cardEntity = cardOptional.get();

            if (cardEntity.getStatus() == targetStatus) {
                return ResponseEntity.ok(CardResponseDto.from(cardEntity));
            }

            cardEntity.setStatus(targetStatus);
            CardEntity saved = cardEntityRepository.save(cardEntity);

            return ResponseEntity.ok(CardResponseDto.from(saved));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
