package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.CreateUserRequestDto;
import com.example.bankcards.dto.response.UserGetResponseDto;
import com.example.bankcards.entity.ClientEntity;
import com.example.bankcards.repository.ClientEntityRepository;
import com.example.bankcards.service.IAdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService implements IAdminUserService {

    private final ClientEntityRepository clientEntityRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int PAGE_SIZE = 10;

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

    @Override
    public ResponseEntity<?> addUser(CreateUserRequestDto createUserRequestDto) {
        try {
            if (createUserRequestDto == null
                    || createUserRequestDto.getLogin() == null
                    || createUserRequestDto.getPassword() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            if (clientEntityRepository.findByLogin(createUserRequestDto.getLogin()).isPresent()) {
                return new ResponseEntity<>("User with provided login already exists", HttpStatus.CONFLICT);
            }

            ClientEntity user = ClientEntity.builder()
                    .login(createUserRequestDto.getLogin())
                    .password(passwordEncoder.encode(createUserRequestDto.getPassword()))
                    .build();

            ClientEntity saved = clientEntityRepository.save(user);

            return new ResponseEntity<>(UserGetResponseDto.from(saved), HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateUser(UUID id, CreateUserRequestDto createUserRequestDto) {
        try {
            if (id == null || createUserRequestDto == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Optional<ClientEntity> userOptional = clientEntityRepository.getClientEntityById(id);

            if (userOptional.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            ClientEntity user = userOptional.get();

            String newLogin = createUserRequestDto.getLogin();
            if (newLogin != null && !newLogin.equals(user.getLogin())) {
                Optional<ClientEntity> existing = clientEntityRepository.findByLogin(newLogin);
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    return new ResponseEntity<>("User with provided login already exists", HttpStatus.CONFLICT);
                }
                user.setLogin(newLogin);
            }

            if (createUserRequestDto.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(createUserRequestDto.getPassword()));
            }

            ClientEntity saved = clientEntityRepository.save(user);
            return ResponseEntity.ok(UserGetResponseDto.from(saved));
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getUser(UUID id) {
        try {
            if(id != null) {
                Optional<ClientEntity> clientEntityOptional = clientEntityRepository.getClientEntityById(id);
                if(clientEntityOptional.isPresent()) {
                    return ResponseEntity.ok(UserGetResponseDto.from(clientEntityOptional.get()));
                }else {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getUsers(Integer page) {
        try {
            int pageNumber = (page == null || page < 0) ? 0 : page;
            PageRequest pageRequest = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by("login").ascending());
            Page<UserGetResponseDto> usersPage = clientEntityRepository.findAll(pageRequest)
                    .map(UserGetResponseDto::from);
            return ResponseEntity.ok(usersPage.getContent());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
