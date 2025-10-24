package com.example.bankcards.dto.response;

import com.example.bankcards.entity.CardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardBalanceResponseDto {

    private Long cardId;

    private Long balance;

    public static CardBalanceResponseDto from(CardEntity cardEntity) {
        long balance = cardEntity.getBalance() == null ? 0L : cardEntity.getBalance();
        return CardBalanceResponseDto.builder()
                .cardId(cardEntity.getId())
                .balance(balance)
                .build();
    }

    public static CardBalanceResponseDto from(long balance, Long cardId) {
        return CardBalanceResponseDto.builder()
                .cardId(cardId)
                .balance(balance)
                .build();
    }
}
