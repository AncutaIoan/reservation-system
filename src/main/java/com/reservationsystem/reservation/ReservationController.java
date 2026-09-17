package com.reservationsystem.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reservation reserveSeat(@RequestBody CreateReservationRequest request) {
        return reservationService.reserveSeat(request.userId(), request.eventSeatId());
    }

    @GetMapping("/{reservationId}")
    public Reservation getReservation(@PathVariable UUID reservationId) {
        return reservationService.getReservation(reservationId);
    }

    @PostMapping("/{reservationId}/confirm")
    public Reservation confirmReservation(@PathVariable UUID reservationId) {
        return reservationService.confirmReservation(reservationId);
    }

    @DeleteMapping("/{reservationId}")
    public Reservation cancelReservation(@PathVariable UUID reservationId) {
        return reservationService.cancelReservation(reservationId);
    }
}
