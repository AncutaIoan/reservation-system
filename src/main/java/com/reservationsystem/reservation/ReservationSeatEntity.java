package com.reservationsystem.reservation;

import com.reservationsystem.eventseat.EventSeatEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "reservation_seat")
public class ReservationSeatEntity {

    @EmbeddedId
    private ReservationSeatId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("reservationId")
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationEntity reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("eventSeatId")
    @JoinColumn(name = "event_seat_id", nullable = false)
    private EventSeatEntity eventSeat;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ReservationSeatEntity() {
    }

    public ReservationSeatEntity(ReservationEntity reservation, EventSeatEntity eventSeat) {
        this.reservation = reservation;
        this.eventSeat = eventSeat;
        this.id = new ReservationSeatId(reservation.getId(), eventSeat.getId());
    }

    public ReservationSeatId getId() {
        return id;
    }

    public ReservationEntity getReservation() {
        return reservation;
    }

    public EventSeatEntity getEventSeat() {
        return eventSeat;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
