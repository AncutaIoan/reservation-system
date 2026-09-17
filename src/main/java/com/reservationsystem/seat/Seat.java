package com.reservationsystem.seat;

import java.util.UUID;

public record Seat(
        UUID id,
        String section,
        String rowLabel,
        String seatNumber,
        Double x,
        Double y
) {
    public Seat(SeatEntity seatEntity) {
        this(seatEntity.getId(), seatEntity.getSection(), seatEntity.getRowLabel(), seatEntity.getSeatNumber(), seatEntity.getX(), seatEntity.getY());
    }
}
