package com.reservationsystem.eventseat;

import com.reservationsystem.seat.Seat;

import java.math.BigDecimal;
import java.util.UUID;

public record EventSeat(
        UUID id,
        UUID eventId,
        Seat seat,
        BigDecimal price,
        String currency,
        String status
) {
    public EventSeat(EventSeatEntity eventSeatEntity) {
        this(
                eventSeatEntity.getId(),
                eventSeatEntity.getEvent().getId(),
                new Seat(eventSeatEntity.getSeat()),
                eventSeatEntity.getPrice(),
                eventSeatEntity.getCurrency(),
                eventSeatEntity.getStatus()
        );
    }
}
