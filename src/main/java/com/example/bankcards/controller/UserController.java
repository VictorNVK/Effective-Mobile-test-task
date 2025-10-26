package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CardTransferRequestDto;
import com.example.bankcards.service.IUserCardService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserCardService userCardService;

    @GetMapping("/cards")
    public ResponseEntity<?> getCards(@Parameter(description = "Zero-based page index", example = "0")
                                      @RequestParam(value = "page", defaultValue = "0") Integer page,
                                      @Parameter(hidden = true)
                                      @AuthenticationPrincipal UserDetails userDetails) {
        return userCardService.getCards(page, userDetails);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Parameter(description = "Transfer payload", required = true)
                                      @RequestBody @Valid CardTransferRequestDto cardTransferRequestDto,
                                      @Parameter(hidden = true)
                                      @AuthenticationPrincipal UserDetails userDetails) {
        return userCardService.transfer(cardTransferRequestDto, userDetails);
    }

    @GetMapping("/card/{cardId}/balance")
    public ResponseEntity<?> getBalance(@Parameter(description = "Card identifier", required = true)
                                        @PathVariable("cardId") Long cardId,
                                        @Parameter(hidden = true)
                                        @AuthenticationPrincipal UserDetails userDetails) {
        return userCardService.getBalance(cardId, userDetails);
    }
    
    @PostMapping("/card/{cardId}/block")
    public ResponseEntity<?> requestCardBlock(@Parameter(name = "cardId", description = "Card identifier", required = true)
                                              @PathVariable("cardId") Long cardId,
                                              @Parameter(hidden = true)
                                              @AuthenticationPrincipal UserDetails userDetails) {
        return userCardService.requestCardBlock(cardId, userDetails);
    }
}
