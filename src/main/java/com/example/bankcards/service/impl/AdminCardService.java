package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.CardCreateRequestDto;
import com.example.bankcards.dto.response.CardCreateResponseDto;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardEntityRepository;
import com.example.bankcards.repository.ClientEntityRepository;
import com.example.bankcards.service.IAdminCardService;
import com.example.bankcards.util.card_generator.CardGenerationResult;
import com.example.bankcards.util.card_generator.ICardNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCardService implements IAdminCardService {

    private final CardEntityRepository cardEntityRepository;
    private final ICardNumberGenerator cardNumberGenerator;
    private final ClientEntityRepository clientEntityRepository;

    @Override
    public ResponseEntity<?> createCard(CardCreateRequestDto cardCreateRequestDto) {
        try {
            if(!clientEntityRepository.existsClientEntityById(cardCreateRequestDto.getOwnerId())){
                return ResponseEntity.noContent().build();
            }
            CardGenerationResult generated = cardNumberGenerator.generate();

            CardEntity cardEntity = CardEntity.builder()
                    .ownerId(cardCreateRequestDto.getOwnerId())
                    .panEncrypted(generated.encryptedPan())
                    .last4(generated.last4())
                    .panHash(generated.panHash())
                    .status(CardStatus.ACTIVE)
                    .balance(0L)
                    .expiryMonth(generated.expiryMonth())
                    .expiryYear(generated.expiryYear())
                    .build();

            CardEntity saved = cardEntityRepository.save(cardEntity);

            CardCreateResponseDto cardCreateResponseDto = CardCreateResponseDto.builder()
                    .id(saved.getId())
                    .ownerId(saved.getOwnerId())
                    .maskedPan(saved.getMaskedPan())
                    .expiryMonth(saved.getExpiryMonth())
                    .expiryYear(saved.getExpiryYear())
                    .plainPan(generated.plainPan())
                    .build();

            return new ResponseEntity<>(cardCreateResponseDto, HttpStatus.CREATED);

        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("Card with generated number already exists", HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteCard() {
        return null;
    }

    @Override
    public ResponseEntity<?> updateCard() {
        return null;
    }

    @Override
    public ResponseEntity<?> blockCard() {
        return null;
    }

    @Override
    public ResponseEntity<?> getCard() {
        return null;
    }

    @Override
    public ResponseEntity<?> getAllCards() {
        return null;
    }
}
