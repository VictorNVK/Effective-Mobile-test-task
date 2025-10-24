package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "cards",
        indexes = @Index(name = "idx_cards_pan_hash", columnList = "pan_hash"))
public class CardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pan_encrypted", length = 1024, nullable = false)
    private String panEncrypted;

    @Column(name = "last4", length = 4, nullable = false)
    private String last4;

    @Column(name = "pan_hash", length = 128, nullable = false, unique = true)
    private String panHash;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @Column(name = "balance")
    private Long balance = 0L;

    @Column(name = "expiry_month")
    private Integer expiryMonth;

    @Column(name = "expiry_year")
    private Integer expiryYear;

    @Transient
    public String getMaskedPan() {
        return "**** **** **** " + last4;
    }
}
