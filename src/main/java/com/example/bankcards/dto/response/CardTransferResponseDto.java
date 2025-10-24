package com.example.bankcards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardTransferResponseDto {

    private Long fromCardId;

    private Long fromBalance;

    private Long toCardId;

    private Long toBalance;
}
