package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CardCreateRequestDto;
import com.example.bankcards.service.IAdminCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminCardService adminCardService;

    @PostMapping("/card/create")
    public ResponseEntity<?> createCard(@RequestBody @Valid CardCreateRequestDto cardCreateRequestDto) {
        return adminCardService.createCard(cardCreateRequestDto);
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
}
