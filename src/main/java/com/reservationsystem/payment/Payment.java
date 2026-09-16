package com.reservationsystem.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Payment(
        UUID id,
        UUID reservationId,
        BigDecimal amount,
        String currency,
        String status,
        String idempotencyKey,
        String externalPaymentId,
        Instant completedAt
) {
    public Payment(PaymentEntity paymentEntity) {
        this(
                paymentEntity.getId(),
                paymentEntity.getReservationId(),
                paymentEntity.getAmount(),
                paymentEntity.getCurrency(),
                paymentEntity.getStatus(),
                paymentEntity.getIdempotencyKey(),
                paymentEntity.getExternalPaymentId(),
                paymentEntity.getCompletedAt()
        );
    }
}