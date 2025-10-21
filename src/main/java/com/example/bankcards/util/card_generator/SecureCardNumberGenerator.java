package com.example.bankcards.util.card_generator;

import com.example.bankcards.util.AesGcmAttributeConverter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.util.Objects;

@Component
public class SecureCardNumberGenerator implements ICardNumberGenerator {

    private final AesGcmAttributeConverter converter;

    public SecureCardNumberGenerator(Environment environment) {
        String base64Key = resolveKey(environment);
        this.converter = new AesGcmAttributeConverter(base64Key);
    }

    @Override
    public CardGenerationResult generate() {
        return generate("4", 16);
    }

    @Override
    public CardGenerationResult generate(String binPrefix, int length) {
        String plainPan = CardNumberGenerator.generateCardNumber(binPrefix, length);
        String encrypted = converter.convertToDatabaseColumn(plainPan);
        String hash = hashPan(plainPan);
        String last4 = plainPan.substring(plainPan.length() - 4);
        YearMonth expiry = YearMonth.now().plusYears(3);

        return new CardGenerationResult(
                plainPan,
                encrypted,
                hash,
                last4,
                expiry.getMonthValue(),
                expiry.getYear()
        );
    }

    private static String resolveKey(Environment environment) {
        String key = null;
        if (environment != null) {
            key = environment.getProperty("PAN_ENC_KEY_BASE64");
            if (key == null) {
                key = environment.getProperty("pan.enc.key-base64");
            }
        }
        if (key == null || key.isBlank()) {
            key = System.getenv("PAN_ENC_KEY_BASE64");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("PAN_ENC_KEY_BASE64 must be provided for card encryption");
        }
        return key;
    }

    private static String hashPan(String pan) {
        Objects.requireNonNull(pan, "pan cannot be null");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(pan.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
