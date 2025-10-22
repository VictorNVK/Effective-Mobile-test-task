package com.example.bankcards.dto;

import com.example.bankcards.dto.request.CardUpdateRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CardUpdateRequestDtoTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validExpiryDatePassesValidation() {
        LocalDate future = LocalDate.now().plusMonths(2);
        CardUpdateRequestDto dto = new CardUpdateRequestDto();
        dto.setExpiryMonth(future.getMonthValue());
        dto.setExpiryYear(future.getYear());

        Set<ConstraintViolation<CardUpdateRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void monthGreaterThanTwelveFailsValidation() {
        LocalDate future = LocalDate.now().plusYears(1);
        CardUpdateRequestDto dto = new CardUpdateRequestDto();
        dto.setExpiryMonth(13);
        dto.setExpiryYear(future.getYear());

        Set<ConstraintViolation<CardUpdateRequestDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("expiryMonth");
    }

    @Test
    void pastExpiryDateFailsValidation() {
        LocalDate past = LocalDate.now().minusMonths(1);
        CardUpdateRequestDto dto = new CardUpdateRequestDto();
        dto.setExpiryMonth(past.getMonthValue());
        dto.setExpiryYear(past.getYear());

        Set<ConstraintViolation<CardUpdateRequestDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("expiryDateValid");
    }
}
