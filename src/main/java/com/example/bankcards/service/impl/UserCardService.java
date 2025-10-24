package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.CardTransferRequestDto;
import com.example.bankcards.dto.response.CardBalanceResponseDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.dto.response.CardTransferResponseDto;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.ClientEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardEntityRepository;
import com.example.bankcards.repository.ClientEntityRepository;
import com.example.bankcards.service.IUserCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCardService implements IUserCardService {

    private static final int PAGE_SIZE = 10;

    private final CardEntityRepository cardEntityRepository;
    private final ClientEntityRepository clientEntityRepository;

    @Override
    public ResponseEntity<?> getCards(Integer page, UserDetails userDetails) {
        try {
            Optional<ClientEntity> clientOptional = resolveClient(userDetails);
            if (clientOptional.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            int pageNumber = (page == null || page < 0) ? 0 : page;
            PageRequest pageRequest = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by("id").ascending());
            UUID ownerId = clientOptional.get().getId();

            Page<CardResponseDto> cardPage = cardEntityRepository.findByOwnerId(ownerId, pageRequest)
                    .map(CardResponseDto::from);

            return ResponseEntity.ok(cardPage.getContent());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> transfer(CardTransferRequestDto cardTransferRequestDto, UserDetails userDetails) {
        try {
            if (cardTransferRequestDto == null
                    || cardTransferRequestDto.getFromCardId() == null
                    || cardTransferRequestDto.getToCardId() == null
                    || cardTransferRequestDto.getAmount() == null
                    || cardTransferRequestDto.getAmount() <= 0) {
                return new ResponseEntity<>("Invalid transfer parameters", HttpStatus.BAD_REQUEST);
            }
            Optional<ClientEntity> clientOptional = resolveClient(userDetails);
            if (clientOptional.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            UUID ownerId = clientOptional.get().getId();

            Optional<CardEntity> fromCardOptional = cardEntityRepository.findByIdAndOwnerId(cardTransferRequestDto.getFromCardId(), ownerId);
            if (fromCardOptional.isEmpty()) {
                return new ResponseEntity<>("Source card not found", HttpStatus.NOT_FOUND);
            }

            Optional<CardEntity> toCardOptional = cardEntityRepository.findByIdAndOwnerId(cardTransferRequestDto.getToCardId(), ownerId);
            if (toCardOptional.isEmpty()) {
                return new ResponseEntity<>("Destination card not found", HttpStatus.NOT_FOUND);
            }

            CardEntity fromCard = fromCardOptional.get();
            CardEntity toCard = toCardOptional.get();

            if (fromCard.getStatus() == CardStatus.BLOCKED) {
                return new ResponseEntity<>("Source card is blocked", HttpStatus.BAD_REQUEST);
            }

            if (toCard.getStatus() == CardStatus.BLOCKED) {
                return new ResponseEntity<>("Destination card is blocked", HttpStatus.BAD_REQUEST);
            }

            long fromBalance = fromCard.getBalance() == null ? 0L : fromCard.getBalance();
            long amount = cardTransferRequestDto.getAmount();
            if (fromBalance < amount) {
                return new ResponseEntity<>("Insufficient funds", HttpStatus.BAD_REQUEST);
            }

            long toBalance = toCard.getBalance() == null ? 0L : toCard.getBalance();

            fromCard.setBalance(fromBalance - amount);
            toCard.setBalance(toBalance + amount);

            cardEntityRepository.save(fromCard);
            cardEntityRepository.save(toCard);

            return ResponseEntity.ok(CardTransferResponseDto.builder()
                    .fromCardId(fromCard.getId())
                    .fromBalance(fromCard.getBalance())
                    .toCardId(toCard.getId())
                    .toBalance(toCard.getBalance())
                    .build());

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getBalance(Long cardId, UserDetails userDetails) {
        try {
            if (cardId == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Optional<ClientEntity> clientOptional = resolveClient(userDetails);
            if (clientOptional.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            UUID ownerId = clientOptional.get().getId();
            Optional<CardEntity> cardOptional = cardEntityRepository.findByIdAndOwnerId(cardId, ownerId);
            if (cardOptional.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            CardEntity card = cardOptional.get();
            long balance = card.getBalance() == null ? 0L : card.getBalance();

            return ResponseEntity.ok().body(CardBalanceResponseDto.from(balance, card.getId()));

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Optional<ClientEntity> resolveClient(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            return Optional.empty();
        }
        return clientEntityRepository.findByLogin(userDetails.getUsername());
    }
}
