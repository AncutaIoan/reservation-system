package com.reservationsystem.reservation;

import com.reservationsystem.event.EventEntity;
import com.reservationsystem.eventseat.EventSeatEntity;
import com.reservationsystem.eventseat.EventSeatRepository;
import com.reservationsystem.seat.SeatEntity;
import com.reservationsystem.venue.VenueEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationSeatRepository reservationSeatRepository;

    @Mock
    private EventSeatRepository eventSeatRepository;

    @InjectMocks
    private ReservationService reservationService;

    private EventSeatEntity newEventSeat() {
        VenueEntity venue = new VenueEntity("National Arena", "Bucharest");
        EventEntity event = new EventEntity(
                UUID.randomUUID(),
                "Metallica",
                "Metallica concert",
                Instant.parse("2026-10-10T18:00:00Z"),
                Instant.parse("2026-10-10T21:00:00Z")
        );
        ReflectionTestUtils.setField(event, "id", UUID.randomUUID());

        SeatEntity seat = new SeatEntity(venue, "A", "1", "12", 1.0, 2.0, 0.0);
        EventSeatEntity eventSeat = new EventSeatEntity(event, seat, BigDecimal.valueOf(100), "RON");
        ReflectionTestUtils.setField(eventSeat, "id", UUID.randomUUID());
        return eventSeat;
    }

    private ReservationEntity newReservation(String status) {
        ReservationEntity reservation = new ReservationEntity(UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        reservation.setStatus(status);
        ReflectionTestUtils.setField(reservation, "id", UUID.randomUUID());
        return reservation;
    }

    @Test
    void reserveSeat_availableSeat_holdsSeatAndCreatesReservation() {
        EventSeatEntity eventSeat = newEventSeat();
        UUID userId = UUID.randomUUID();

        when(eventSeatRepository.findById(eventSeat.getId())).thenReturn(Optional.of(eventSeat));
        when(eventSeatRepository.save(any(EventSeatEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> {
            ReservationEntity reservation = invocation.getArgument(0);
            ReflectionTestUtils.setField(reservation, "id", UUID.randomUUID());
            return reservation;
        });

        Reservation result = reservationService.reserveSeat(userId, eventSeat.getId());

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.status()).isEqualTo(ReservationEntity.HELD);
        assertThat(eventSeat.getStatus()).isEqualTo(EventSeatEntity.HELD);
    }

    @Test
    void reserveSeat_seatAlreadyHeld_throwsResponseStatusException() {
        EventSeatEntity eventSeat = newEventSeat();
        eventSeat.setStatus(EventSeatEntity.HELD);

        when(eventSeatRepository.findById(eventSeat.getId())).thenReturn(Optional.of(eventSeat));

        assertThatThrownBy(() -> reservationService.reserveSeat(UUID.randomUUID(), eventSeat.getId()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void reserveSeat_missingEventSeat_throwsResponseStatusException() {
        UUID eventSeatId = UUID.randomUUID();

        when(eventSeatRepository.findById(eventSeatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.reserveSeat(UUID.randomUUID(), eventSeatId))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getReservation_existingId_returnsReservation() {
        ReservationEntity reservation = newReservation(ReservationEntity.HELD);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        Reservation result = reservationService.getReservation(reservation.getId());

        assertThat(result.id()).isEqualTo(reservation.getId());
    }

    @Test
    void getReservation_missingId_throwsResponseStatusException() {
        UUID reservationId = UUID.randomUUID();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReservation(reservationId))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void confirmReservation_heldReservation_confirmsAndMarksSeatsReserved() {
        ReservationEntity reservation = newReservation(ReservationEntity.HELD);
        EventSeatEntity eventSeat = newEventSeat();
        ReservationSeatEntity reservationSeat = new ReservationSeatEntity(reservation, eventSeat);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationSeatRepository.findAllByReservationId(reservation.getId())).thenReturn(List.of(reservationSeat));
        when(eventSeatRepository.save(any(EventSeatEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.confirmReservation(reservation.getId());

        assertThat(result.status()).isEqualTo(ReservationEntity.CONFIRMED);
        assertThat(result.confirmedAt()).isNotNull();
        assertThat(eventSeat.getStatus()).isEqualTo(EventSeatEntity.RESERVED);
    }

    @Test
    void confirmReservation_cancelledReservation_throwsResponseStatusException() {
        ReservationEntity reservation = newReservation(ReservationEntity.CANCELLED);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.confirmReservation(reservation.getId()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void cancelReservation_heldReservation_cancelsAndReleasesSeats() {
        ReservationEntity reservation = newReservation(ReservationEntity.HELD);
        EventSeatEntity eventSeat = newEventSeat();
        eventSeat.setStatus(EventSeatEntity.HELD);
        ReservationSeatEntity reservationSeat = new ReservationSeatEntity(reservation, eventSeat);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationSeatRepository.findAllByReservationId(reservation.getId())).thenReturn(List.of(reservationSeat));
        when(eventSeatRepository.save(any(EventSeatEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.cancelReservation(reservation.getId());

        assertThat(result.status()).isEqualTo(ReservationEntity.CANCELLED);
        assertThat(result.cancelledAt()).isNotNull();
        assertThat(eventSeat.getStatus()).isEqualTo(EventSeatEntity.AVAILABLE);
    }

    @Test
    void cancelReservation_confirmedReservation_throwsResponseStatusException() {
        ReservationEntity reservation = newReservation(ReservationEntity.CONFIRMED);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(reservation.getId()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getAllReservations_reservationsExist_returnsAllReservations() {
        ReservationEntity reservation = newReservation(ReservationEntity.HELD);

        when(reservationRepository.findAll()).thenReturn(List.of(reservation));

        List<Reservation> result = reservationService.getAllReservations();

        assertThat(result)
                .hasSize(1)
                .extracting(Reservation::id)
                .containsExactly(reservation.getId());
    }
}
