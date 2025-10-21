package com.example.bankcards.util.card_generator;

public record CardGenerationResult(
        String plainPan,
        String encryptedPan,
        String panHash,
        String last4,
        int expiryMonth,
        int expiryYear
) {
}
