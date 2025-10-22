package com.example.bankcards.dto.request;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.time.Year;
import java.util.UUID;

@Data
public class CardUpdateRequestDto {

    private UUID ownerId;

    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    private Integer expiryMonth;

    @NotNull(message = "Expiry year is required")
    @Positive(message = "Expiry year must be a positive number")
    private Integer expiryYear;

    private CardStatus status;

    @AssertTrue(message = "Expiry date must not be in the past")
    public boolean isExpiryDateValid() {
        if (expiryMonth == null || expiryYear == null) {
            return false;
        }
        int currentYear = Year.now().getValue();
        if (expiryYear < currentYear) {
            return false;
        }
        if (expiryYear == currentYear) {
            int currentMonth = LocalDate.now().getMonthValue();
            return expiryMonth >= currentMonth;
        }
        return true;
    }
}
