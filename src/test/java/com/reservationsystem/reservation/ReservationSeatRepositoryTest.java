package com.reservationsystem.reservation;

import com.reservationsystem.TestcontainersConfiguration;
import com.reservationsystem.event.EventEntity;
import com.reservationsystem.event.EventRepository;
import com.reservationsystem.eventseat.EventSeatEntity;
import com.reservationsystem.eventseat.EventSeatRepository;
import com.reservationsystem.seat.SeatEntity;
import com.reservationsystem.seat.SeatRepository;
import com.reservationsystem.user.UserEntity;
import com.reservationsystem.user.UserRepository;
import com.reservationsystem.venue.VenueEntity;
import com.reservationsystem.venue.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class ReservationSeatRepositoryTest {

    @Autowired
    private ReservationSeatRepository reservationSeatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EventSeatRepository eventSeatRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findAllByReservationId_reservationHasSeats_returnsLinkedSeats() {
        VenueEntity venue = venueRepository.saveAndFlush(new VenueEntity("National Arena", "Bucharest"));

        EventEntity event = eventRepository.saveAndFlush(new EventEntity(
                venue.getId(),
                "Metallica",
                "Metallica concert",
                Instant.parse("2026-10-10T18:00:00Z"),
                Instant.parse("2026-10-10T21:00:00Z")
        ));

        SeatEntity seat = seatRepository.saveAndFlush(new SeatEntity(venue, "A", "1", "12", 1.0, 2.0, 0.0));

        EventSeatEntity eventSeat = eventSeatRepository.saveAndFlush(
                new EventSeatEntity(event, seat, BigDecimal.valueOf(100), "RON")
        );

        UserEntity user = userRepository.saveAndFlush(new UserEntity("jane@example.com", "Jane Doe"));

        ReservationEntity reservation = reservationRepository.saveAndFlush(
                new ReservationEntity(user.getId(), event.getId(), Instant.now())
        );

        reservationSeatRepository.saveAndFlush(new ReservationSeatEntity(reservation, eventSeat));

        List<ReservationSeatEntity> result = reservationSeatRepository.findAllByReservationId(reservation.getId());

        assertThat(result)
                .hasSize(1)
                .allMatch(reservationSeat -> reservationSeat.getEventSeat().getId().equals(eventSeat.getId()));
    }

    @Test
    void findAllByReservationId_noSeatsLinked_returnsEmptyList() {
        List<ReservationSeatEntity> result = reservationSeatRepository.findAllByReservationId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}
