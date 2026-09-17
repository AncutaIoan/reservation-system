package com.reservationsystem.reservation;

import java.util.UUID;

public record CreateReservationRequest(
        UUID userId,
        UUID eventSeatId
) {
}
