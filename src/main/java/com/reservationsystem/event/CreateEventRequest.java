package com.reservationsystem.event;

import java.time.Instant;
import java.util.UUID;

public record CreateEventRequest(
        UUID venueId,
        String name,
        String description,
        Instant startsAt,
        Instant endsAt
) {
}
