package com.example.bankcards.service;

import org.springframework.http.ResponseEntity;

public interface IAdminUserService {

    ResponseEntity<?> createUser();

    ResponseEntity<?> deleteUser();

}
