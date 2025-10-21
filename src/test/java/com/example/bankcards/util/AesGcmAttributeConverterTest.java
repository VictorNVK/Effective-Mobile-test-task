package com.example.bankcards.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AesGcmAttributeConverterTest {

    private static final String BASE64_KEY = Base64.getEncoder()
            .encodeToString("0123456789ABCDEF0123456789ABCDEF".getBytes(StandardCharsets.UTF_8));

    private final AesGcmAttributeConverter converter = new AesGcmAttributeConverter(BASE64_KEY);

    @Test
    @DisplayName("convertToDatabaseColumn should return null when attribute is null")
    void convertToDatabaseColumnReturnsNullForNullAttribute() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    @DisplayName("convertToEntityAttribute should return null when column value is null")
    void convertToEntityAttributeReturnsNullForNullColumnValue() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    @DisplayName("Encryption and decryption round trip preserves original text")
    void roundTripEncryptionDecryptionWorks() {
        String original = "4111111111111111";

        String encrypted = converter.convertToDatabaseColumn(original);
        assertNotNull(encrypted, "Encrypted value should not be null");
        assertNotEquals(original, encrypted, "Encrypted value should differ from original");
        assertTrue(encrypted.contains(":"), "Encrypted value should contain IV and ciphertext parts");

        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(original, decrypted, "Decrypted value should equal original");
    }

    @Test
    @DisplayName("Encryption uses random IV so repeated conversions yield different ciphertexts")
    void encryptionProducesDifferentCiphertextPerInvocation() {
        String original = "5555444433332222";

        String first = converter.convertToDatabaseColumn(original);
        String second = converter.convertToDatabaseColumn(original);

        assertNotEquals(first, second, "Ciphertext should be different due to random IV");
    }

    @Test
    @DisplayName("Decrypting improperly formatted column fails with IllegalArgumentException")
    void decryptingInvalidFormatThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute("not-valid-format")
        );
        assertTrue(exception.getMessage().contains("Invalid encrypted value format"));
    }
}
