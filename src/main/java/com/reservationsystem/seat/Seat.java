package com.reservationsystem.seat;

public record Seat(
        String section,
        String rowLabel,
        String seatNumber,
        Double x,
        Double y
) {
    public Seat(SeatEntity seatEntity) {
        this(seatEntity.getSection(), seatEntity.getRowLabel(), seatEntity.getSeatNumber(), seatEntity.getX(), seatEntity.getY());
    }
}
