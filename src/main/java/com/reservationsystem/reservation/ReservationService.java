package com.reservationsystem.reservation;

import com.reservationsystem.eventseat.EventSeatEntity;
import com.reservationsystem.eventseat.EventSeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    static final Duration HOLD_DURATION = Duration.ofMinutes(15);

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final EventSeatRepository eventSeatRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationSeatRepository reservationSeatRepository,
            EventSeatRepository eventSeatRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.eventSeatRepository = eventSeatRepository;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(Reservation::new)
                .toList();
    }

    public Reservation getReservation(UUID reservationId) {
        return new Reservation(findReservationOrThrow(reservationId));
    }

    @Transactional
    public Reservation reserveSeat(UUID userId, UUID eventSeatId) {
        EventSeatEntity eventSeat = eventSeatRepository.findById(eventSeatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event seat not found: " + eventSeatId));

        if (!EventSeatEntity.AVAILABLE.equals(eventSeat.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat is not available: " + eventSeatId);
        }

        eventSeat.setStatus(EventSeatEntity.HELD);
        eventSeatRepository.save(eventSeat);

        ReservationEntity reservation = new ReservationEntity(
                userId,
                eventSeat.getEvent().getId(),
                Instant.now().plus(HOLD_DURATION)
        );
        ReservationEntity savedReservation = reservationRepository.save(reservation);

        reservationSeatRepository.save(new ReservationSeatEntity(savedReservation, eventSeat));

        return new Reservation(savedReservation);
    }

    @Transactional
    public Reservation confirmReservation(UUID reservationId) {
        ReservationEntity reservation = findReservationOrThrow(reservationId);

        if (!ReservationEntity.HELD.equals(reservation.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation is not HELD: " + reservationId);
        }

        reservation.setStatus(ReservationEntity.CONFIRMED);
        reservation.setConfirmedAt(Instant.now());

        for (ReservationSeatEntity reservationSeat : reservationSeatRepository.findAllByReservationId(reservationId)) {
            EventSeatEntity eventSeat = reservationSeat.getEventSeat();
            eventSeat.setStatus(EventSeatEntity.RESERVED);
            eventSeatRepository.save(eventSeat);
        }

        return new Reservation(reservationRepository.save(reservation));
    }

    @Transactional
    public Reservation cancelReservation(UUID reservationId) {
        ReservationEntity reservation = findReservationOrThrow(reservationId);

        if (ReservationEntity.CONFIRMED.equals(reservation.getStatus()) || ReservationEntity.CANCELLED.equals(reservation.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation cannot be cancelled: " + reservationId);
        }

        reservation.setStatus(ReservationEntity.CANCELLED);
        reservation.setCancelledAt(Instant.now());

        for (ReservationSeatEntity reservationSeat : reservationSeatRepository.findAllByReservationId(reservationId)) {
            EventSeatEntity eventSeat = reservationSeat.getEventSeat();
            eventSeat.setStatus(EventSeatEntity.AVAILABLE);
            eventSeatRepository.save(eventSeat);
        }

        return new Reservation(reservationRepository.save(reservation));
    }

    private ReservationEntity findReservationOrThrow(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found: " + reservationId));
    }
}
