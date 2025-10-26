package com.example.bankcards.repository;

import com.example.bankcards.entity.ApplicationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApplicationEntityRepository extends JpaRepository<ApplicationEntity, UUID> {

    Page<ApplicationEntity> findAllByApproved(Boolean approved, Pageable pageable);

    boolean existsByCardIdAndApprovedIsFalse(Long cardId);
}
