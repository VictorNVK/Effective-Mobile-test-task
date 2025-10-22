package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CardCreateRequestDto;
import com.example.bankcards.dto.request.CardUpdateRequestDto;
import com.example.bankcards.service.IAdminCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminCardService adminCardService;

    @PostMapping("/card/create")
    public ResponseEntity<?> createCard(@RequestBody @Valid CardCreateRequestDto cardCreateRequestDto) {
        return adminCardService.createCard(cardCreateRequestDto);
    }

    @GetMapping("/cards")
    public ResponseEntity<?> getAllCards(@Parameter(description = "Zero-based page index", example = "0")
                                         @RequestParam(value = "page", defaultValue = "0") Integer page) {
        return adminCardService.getAllCards(page);
    }

    @PatchMapping("/card/block/{cardId}")
    public ResponseEntity<?> blockCard(@Parameter(description = "Card identifier", required = true)
                                       @PathVariable("cardId") Long cardId) {
        return adminCardService.blockCard(cardId);
    }

    @PatchMapping("/card/activate/{cardId}")
    public ResponseEntity<?> activateCard(@Parameter(description = "Card identifier", required = true)
                                          @PathVariable("cardId") Long cardId) {
        return adminCardService.activateCard(cardId);
    }

    @DeleteMapping("/card/delete/{cardId}")
    public ResponseEntity<?> deleteCard(@PathVariable("cardId") Long cardId) {
        return adminCardService.deleteCard(cardId);
    }

    @GetMapping("/card/{cardId}")
    public ResponseEntity<?> getCardById(@PathVariable("cardId") Long cardId) {
        return adminCardService.getCard(cardId);
    }

    @PatchMapping("/card/update/{cardId}")
    public ResponseEntity<?> updateCard(
            @Parameter(description = "Card identifier", required = true)
            @PathVariable("cardId") Long cardId,
            @Valid @RequestBody CardUpdateRequestDto cardUpdateRequestDto
                                       ) {
        return adminCardService.updateCard(cardUpdateRequestDto, cardId);
    }

}
