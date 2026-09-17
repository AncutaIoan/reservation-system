package com.reservationsystem.reservation;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ReservationSeatId implements Serializable {

    private UUID reservationId;
    private UUID eventSeatId;

    protected ReservationSeatId() {
    }

    public ReservationSeatId(UUID reservationId, UUID eventSeatId) {
        this.reservationId = reservationId;
        this.eventSeatId = eventSeatId;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public UUID getEventSeatId() {
        return eventSeatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationSeatId that)) return false;
        return Objects.equals(reservationId, that.reservationId) && Objects.equals(eventSeatId, that.eventSeatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId, eventSeatId);
    }
}
