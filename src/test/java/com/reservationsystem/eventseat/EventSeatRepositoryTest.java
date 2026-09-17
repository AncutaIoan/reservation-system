package com.reservationsystem.eventseat;

import com.reservationsystem.TestcontainersConfiguration;
import com.reservationsystem.event.EventEntity;
import com.reservationsystem.event.EventRepository;
import com.reservationsystem.seat.SeatEntity;
import com.reservationsystem.seat.SeatRepository;
import com.reservationsystem.venue.VenueEntity;
import com.reservationsystem.venue.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class EventSeatRepositoryTest {

    @Autowired
    private EventSeatRepository eventSeatRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private VenueRepository venueRepository;

    private EventEntity savedEvent;
    private SeatEntity savedSeatA;
    private SeatEntity savedSeatB;

    private void setUp() {
        VenueEntity venue = venueRepository.saveAndFlush(new VenueEntity("National Arena", "Bucharest"));

        savedEvent = eventRepository.saveAndFlush(new EventEntity(
                venue.getId(),
                "Metallica",
                "Metallica concert",
                Instant.parse("2026-10-10T18:00:00Z"),
                Instant.parse("2026-10-10T21:00:00Z")
        ));

        savedSeatA = seatRepository.saveAndFlush(new SeatEntity(venue, "A", "1", "12", 1.0, 2.0, 0.0));
        savedSeatB = seatRepository.saveAndFlush(new SeatEntity(venue, "A", "1", "13", 2.0, 2.0, 0.0));
    }

    @Test
    void findAllByEventId_eventHasSeats_returnsAllSeatsForEvent() {
        setUp();

        EventSeatEntity eventSeatA = new EventSeatEntity(savedEvent, savedSeatA, BigDecimal.valueOf(100), "RON");
        EventSeatEntity eventSeatB = new EventSeatEntity(savedEvent, savedSeatB, BigDecimal.valueOf(120), "RON");
        eventSeatRepository.saveAll(List.of(eventSeatA, eventSeatB));

        List<EventSeatEntity> result = eventSeatRepository.findAllByEventId(savedEvent.getId());

        assertThat(result)
                .hasSize(2)
                .allMatch(eventSeat -> eventSeat.getEvent().getId().equals(savedEvent.getId()));
    }

    @Test
    void findAllByEventIdAndStatus_someSeatsHeld_returnsOnlyMatchingStatus() {
        setUp();

        EventSeatEntity eventSeatA = new EventSeatEntity(savedEvent, savedSeatA, BigDecimal.valueOf(100), "RON");
        EventSeatEntity eventSeatB = new EventSeatEntity(savedEvent, savedSeatB, BigDecimal.valueOf(120), "RON");
        eventSeatB.setStatus(EventSeatEntity.HELD);
        eventSeatRepository.saveAll(List.of(eventSeatA, eventSeatB));

        List<EventSeatEntity> result = eventSeatRepository.findAllByEventIdAndStatus(savedEvent.getId(), EventSeatEntity.AVAILABLE);

        assertThat(result)
                .hasSize(1)
                .allMatch(eventSeat -> eventSeat.getStatus().equals(EventSeatEntity.AVAILABLE));
    }
}
