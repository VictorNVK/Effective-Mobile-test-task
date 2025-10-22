package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardCreateRequestDto;
import com.example.bankcards.dto.request.CardUpdateRequestDto;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface IAdminCardService {

    ResponseEntity<?> createCard(CardCreateRequestDto cardCreateRequestDto);

    ResponseEntity<?> deleteCard(Long id);

    ResponseEntity<?> updateCard(CardUpdateRequestDto cardUpdateRequestDto, Long id);

    ResponseEntity<?> blockCard(Long cardId);

    ResponseEntity<?> activateCard(Long cardId);

    ResponseEntity<?> getCard(Long id);

    ResponseEntity<?> getAllCards(Integer page);
}
