package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardTransferRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

public interface IUserCardService {

    ResponseEntity<?> getCards(Integer page, UserDetails userDetails);

    ResponseEntity<?> requestCardBlock(Long cardId, UserDetails userDetails);

    ResponseEntity<?> transfer(CardTransferRequestDto cardTransferRequestDto,  UserDetails userDetails);

    ResponseEntity<?> getBalance(Long cardId, UserDetails userDetails);

}
