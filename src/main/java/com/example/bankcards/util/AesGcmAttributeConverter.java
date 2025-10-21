package com.example.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
public class AesGcmAttributeConverter implements AttributeConverter<String, String> {

    private static final String KEY_ENV = "PAN_ENC_KEY_BASE64";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BIT = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKey secretKey;

    public AesGcmAttributeConverter(String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalStateException("Encryption key must be provided");
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalStateException("Encryption key must be 32 bytes (256 bit)");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            String ivBase64 = Base64.getEncoder().encodeToString(iv);
            String ctBase64 = Base64.getEncoder().encodeToString(cipherText);

            return ivBase64 + ":" + ctBase64;
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting attribute", e);
        }
    }
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String[] parts = dbData.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid encrypted value format");
        }
        try {
            byte[] iv = decodeBase64(parts[0]);
            byte[] cipherText = decodeBase64(parts[1]);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting attribute", e);
        }
    }

    private static byte[] decodeBase64(String value) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid encrypted value format", e);
        }
    }
}
