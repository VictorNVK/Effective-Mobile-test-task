package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardCreateRequestDto;
import org.springframework.http.ResponseEntity;

public interface IAdminCardService {

    ResponseEntity<?> createCard(CardCreateRequestDto cardCreateRequestDto);

    ResponseEntity<?> deleteCard();

    ResponseEntity<?> updateCard();

    ResponseEntity<?> blockCard(Long cardId);

    ResponseEntity<?> activateCard(Long cardId);

    ResponseEntity<?> getCard();

    ResponseEntity<?> getAllCards();
}
