package com.example.bankcards.repository;

import com.example.bankcards.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientEntityRepository extends JpaRepository<ClientEntity, Long> {

    boolean existsClientEntityById(UUID id);

    void deleteClientEntityById(UUID id);

    Optional<ClientEntity> findByLogin(String login);

    Optional<ClientEntity> getClientEntityById(UUID id);
}
