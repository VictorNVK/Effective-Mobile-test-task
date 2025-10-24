package com.example.bankcards.dto.request;

import lombok.Data;

@Data
public class CardTransferRequestDto {

    private Long fromCardId;

    private Long toCardId;

    private Long amount;
}
