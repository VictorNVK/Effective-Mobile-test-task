package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardTransferRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

public interface IUserCardService {

    ResponseEntity<?> getCards(Integer page, UserDetails userDetails);

    //Метод для запроса блокировки карты ToDo

    ResponseEntity<?> transfer(CardTransferRequestDto cardTransferRequestDto,  UserDetails userDetails);

    ResponseEntity<?> getBalance(Long cardId, UserDetails userDetails);




}
