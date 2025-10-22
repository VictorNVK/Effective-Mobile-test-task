package com.example.bankcards.service.impl;

import com.example.bankcards.repository.ClientEntityRepository;
import com.example.bankcards.service.IAdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService implements IAdminUserService {

    private final ClientEntityRepository clientEntityRepository;

    @Override
    public ResponseEntity<?> deleteUser(UUID id) {
        try {
            if(clientEntityRepository.existsClientEntityById(id)){
                clientEntityRepository.deleteClientEntityById(id);
                return new ResponseEntity<>(HttpStatus.OK);
            }else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
