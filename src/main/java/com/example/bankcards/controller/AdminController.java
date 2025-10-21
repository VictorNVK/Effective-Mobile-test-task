package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CardCreateRequestDto;
import com.example.bankcards.service.IAdminCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminCardService adminCardService;

    @PostMapping("/card/create")
    public ResponseEntity<?> createCard(@RequestBody @Valid CardCreateRequestDto cardCreateRequestDto) {
        return adminCardService.createCard(cardCreateRequestDto);
    }

}
