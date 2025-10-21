package com.example.bankcards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardCreateResponseDto {

    private Long id;

    private UUID ownerId;

    private String maskedPan;

    private Integer expiryMonth;

    private Integer expiryYear;

    private String plainPan;
}
