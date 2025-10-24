package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardEntityRepository extends JpaRepository<CardEntity, Long> {

    Page<CardEntity> findByOwnerId(UUID ownerId, Pageable pageable);

    Optional<CardEntity> findByIdAndOwnerId(Long id, UUID ownerId);
}
