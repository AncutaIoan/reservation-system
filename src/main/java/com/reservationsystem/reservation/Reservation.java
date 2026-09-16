package com.reservationsystem.reservation;

import java.time.Instant;
import java.util.UUID;

public record Reservation(
        UUID id,
        UUID userId,
        UUID eventId,
        String status,
        Instant expiresAt,
        Instant confirmedAt,
        Instant cancelledAt
) {
    public Reservation(ReservationEntity reservationEntity) {
        this(
                reservationEntity.getId(),
                reservationEntity.getUserId(),
                reservationEntity.getEventId(),
                reservationEntity.getStatus(),
                reservationEntity.getExpiresAt(),
                reservationEntity.getConfirmedAt(),
                reservationEntity.getCancelledAt()
        );
    }
}