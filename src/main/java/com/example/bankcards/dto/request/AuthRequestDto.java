package com.example.bankcards.dto.request;

import com.example.bankcards.entity.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuthRequestDto {

    @NotBlank
    private String login;

    @NotBlank
    private String password;

    @NotNull
    private RoleType role;
}
