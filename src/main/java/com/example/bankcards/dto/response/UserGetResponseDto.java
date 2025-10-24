package com.example.bankcards.dto.response;

import com.example.bankcards.entity.ClientEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserGetResponseDto {

    private UUID id;

    private String login;

    public static UserGetResponseDto from(ClientEntity user) {
        return UserGetResponseDto.builder()
                .id(user.getId())
                .login(user.getLogin())
                .build();
    }
}
