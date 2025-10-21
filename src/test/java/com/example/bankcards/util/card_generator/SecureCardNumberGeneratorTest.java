package com.example.bankcards.util.card_generator;

import com.example.bankcards.util.AesGcmAttributeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class SecureCardNumberGeneratorTest {

    private static final String RAW_KEY = "0123456789ABCDEF0123456789ABCDEF";
    private static final String BASE64_KEY = Base64.getEncoder()
            .encodeToString(RAW_KEY.getBytes(StandardCharsets.UTF_8));

    private SecureCardNumberGenerator generator;

    @BeforeEach
    void setUp() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("PAN_ENC_KEY_BASE64", BASE64_KEY);
        generator = new SecureCardNumberGenerator(environment);
    }

    @Test
    @DisplayName("generate() returns masked card data with encrypted PAN and SHA-256 hash")
    void generateReturnsValidCardData() {
        CardGenerationResult result = generator.generate();

        assertNotNull(result);
        assertTrue(result.plainPan().matches("\\d{16}"), "Plain PAN must be 16 digits");
        assertEquals(result.plainPan().substring(result.plainPan().length() - 4), result.last4());
        assertNotNull(result.encryptedPan());
        assertFalse(result.encryptedPan().isBlank(), "Encrypted PAN must not be blank");

        var converter = new AesGcmAttributeConverter(BASE64_KEY);
        assertEquals(result.plainPan(), converter.convertToEntityAttribute(result.encryptedPan()));

        assertNotNull(result.panHash());
        assertEquals(64, result.panHash().length(), "SHA-256 hash must be 64 hex chars");
        assertTrue(result.panHash().matches("[0-9a-f]{64}"));

        YearMonth expectedExpiry = YearMonth.now().plusYears(3);
        assertEquals(expectedExpiry.getMonthValue(), result.expiryMonth());
        assertEquals(expectedExpiry.getYear(), result.expiryYear());
    }

    @Test
    @DisplayName("generate(bin, length) respects provided prefix and length")
    void generateWithCustomParameters() {
        CardGenerationResult result = generator.generate("51", 15);

        assertTrue(result.plainPan().startsWith("51"));
        assertEquals(15, result.plainPan().length());
        assertEquals("51", result.plainPan().substring(0, 2));
        assertEquals(result.plainPan().substring(result.plainPan().length() - 4), result.last4());
    }

    @Test
    @DisplayName("Constructor fails when encryption key is missing")
    void constructorFailsWithoutKey() {
        MockEnvironment environment = new MockEnvironment();
        assertThrows(IllegalStateException.class, () -> new SecureCardNumberGenerator(environment));
    }
}
