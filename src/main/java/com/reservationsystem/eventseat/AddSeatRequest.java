package com.reservationsystem.eventseat;

import java.math.BigDecimal;

public record AddSeatRequest(
        String section,
        String rowLabel,
        String seatNumber,
        Double x,
        Double y,
        Double rotation,
        BigDecimal price,
        String currency
) {
}
