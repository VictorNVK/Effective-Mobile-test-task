package com.example.bankcards.service;

import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface IAdminUserService {

    ResponseEntity<?> deleteUser(UUID id);

}
