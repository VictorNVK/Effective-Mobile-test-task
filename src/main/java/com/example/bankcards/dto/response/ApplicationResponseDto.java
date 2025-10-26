package com.example.bankcards.dto.response;

import com.example.bankcards.entity.ApplicationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
public class ApplicationResponseDto {

    private UUID id;

    private UUID accountId;

    private Long cardId;

    private Boolean approved;

    public static ApplicationResponseDto from(ApplicationEntity application) {
        return ApplicationResponseDto.builder()
                .id(application.getId())
                .accountId(application.getAccountId())
                .cardId(application.getCardId())
                .approved(application.getApproved())
                .build();
    }
}
