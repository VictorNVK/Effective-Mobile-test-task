package com.example.bankcards.dto.response;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
public class CardResponseDto {

    private Long id;

    private UUID ownerId;

    private CardStatus status;

    private String maskedPan;

    private Integer expiryMonth;

    private Integer expiryYear;


    public static CardResponseDto from(CardEntity card) {
        return CardResponseDto.builder()
                .id(card.getId())
                .ownerId(card.getOwnerId())
                .status(card.getStatus())
                .maskedPan(card.getMaskedPan())
                .expiryMonth(card.getExpiryMonth())
                .expiryYear(card.getExpiryYear())
                .build();
    }
}
