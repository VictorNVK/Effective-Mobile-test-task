package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardEntityRepository extends JpaRepository<CardEntity, Long> {

}
