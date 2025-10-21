package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CardCreateRequestDto {

    @NotNull
    private UUID ownerId;
}
