package com.reservationsystem.event;

import java.time.Instant;
import java.util.UUID;

public record Event(
        UUID id,
        UUID venueId,
        String name,
        String description,
        Instant startsAt,
        Instant endsAt
) {
    public Event(EventEntity eventEntity) {
        this(
                eventEntity.getId(),
                eventEntity.getVenueId(),
                eventEntity.getName(),
                eventEntity.getDescription(),
                eventEntity.getStartsAt(),
                eventEntity.getEndsAt()
        );
    }
}