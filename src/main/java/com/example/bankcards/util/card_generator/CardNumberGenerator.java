package com.example.bankcards.util.card_generator;

import java.security.SecureRandom;
import java.util.Objects;

public class CardNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private CardNumberGenerator() {}

    public static String generateCardNumber(String binPrefix, int length) {
        Objects.requireNonNull(binPrefix, "binPrefix cannot be null");
        if (!binPrefix.matches("^\\d*$")) {
            throw new IllegalArgumentException("binPrefix must contain only digits");
        }
        if (length < 13 || length > 19) {
            throw new IllegalArgumentException("card length must be between 13 and 19");
        }
        if (binPrefix.length() >= length) {
            throw new IllegalArgumentException("binPrefix length must be less than total length");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(binPrefix);

        int randomDigits = length - 1 - binPrefix.length();
        for (int i = 0; i < randomDigits; i++) {
            sb.append(RANDOM.nextInt(10));
        }

        int checkDigit = computeLuhnCheckDigit(sb.toString());
        sb.append(checkDigit);

        return sb.toString();
    }
    public static String generateRandomVisa16() {
        return generateCardNumber("4", 16);
    }

    public static boolean isValidLuhn(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("^\\d+$")) return false;
        int sum = 0;
        boolean doubleDigit = false;
        // iterate from right to left
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int d = cardNumber.charAt(i) - '0';
            if (doubleDigit) {
                d = d * 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
    public static int computeLuhnCheckDigit(String withoutCheck) {
        if (withoutCheck == null || !withoutCheck.matches("^\\d+$")) {
            throw new IllegalArgumentException("Input must be digits only");
        }
        String s = withoutCheck + "0";
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = s.length() - 1; i >= 0; i--) {
            int d = s.charAt(i) - '0';
            if (doubleDigit) {
                d = d * 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        int mod = sum % 10;
        return (10 - mod) % 10;
    }


}
