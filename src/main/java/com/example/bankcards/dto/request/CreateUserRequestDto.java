package com.example.bankcards.dto.request;

import lombok.Data;

@Data
public class CreateUserRequestDto {

    private String login;

    private String password;
}
