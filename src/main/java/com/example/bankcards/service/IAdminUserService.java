package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateUserRequestDto;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface IAdminUserService {

    ResponseEntity<?> deleteUser(UUID id);

    ResponseEntity<?> addUser(CreateUserRequestDto createUserRequestDto);

    ResponseEntity<?> updateUser(UUID id, CreateUserRequestDto createUserRequestDto);

    ResponseEntity<?> getUser(UUID id);

    ResponseEntity<?> getUsers(Integer page);

}
