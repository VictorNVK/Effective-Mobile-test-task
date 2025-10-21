package com.example.bankcards.util.card_generator;

public interface ICardNumberGenerator {

    CardGenerationResult generate();

    CardGenerationResult generate(String binPrefix, int length);
}
