package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CardCreateRequestDto;
import com.example.bankcards.dto.request.CardUpdateRequestDto;
import com.example.bankcards.dto.request.CreateUserRequestDto;
import com.example.bankcards.service.IAdminCardService;
import com.example.bankcards.service.IAdminUserService;
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
    private final IAdminUserService adminUserService;

    @PostMapping("/card/create")
    public ResponseEntity<?> createCard(@RequestBody @Valid CardCreateRequestDto cardCreateRequestDto) {
        return adminCardService.createCard(cardCreateRequestDto);
    }

    @GetMapping("/cards")
    public ResponseEntity<?> getAllCards(@Parameter(description = "Zero based page index", example = "0")
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

    @PostMapping("/user")
    public ResponseEntity<?> addUser(@Parameter(description = "User payload", required = true)
                                     @RequestBody @Valid CreateUserRequestDto createUserRequestDto){
        return adminUserService.addUser(createUserRequestDto);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@Parameter(description = "User identifier", required = true)
                                        @PathVariable UUID id){
        return adminUserService.deleteUser(id);
    }

    @PatchMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@Parameter(description = "User identifier", required = true)
                                        @PathVariable UUID id,
                                        @Parameter(description = "User payload", required = true)
                                        @RequestBody @Valid CreateUserRequestDto createUserRequestDto){
        return adminUserService.updateUser(id, createUserRequestDto);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUser(@Parameter(description = "User identifier", required = true)
                                     @PathVariable UUID id){
        return adminUserService.getUser(id);
    }

    @GetMapping("/users/{page}")
    public ResponseEntity<?> getUsers(@Parameter(description = "Zero-based page index", example = "0", required = true)
                                      @PathVariable("page") Integer page){
        return adminUserService.getUsers(page);
    }

}
