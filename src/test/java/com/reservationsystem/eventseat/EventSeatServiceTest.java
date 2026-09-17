package com.reservationsystem.eventseat;

import com.reservationsystem.event.EventEntity;
import com.reservationsystem.event.EventRepository;
import com.reservationsystem.seat.SeatEntity;
import com.reservationsystem.seat.SeatRepository;
import com.reservationsystem.venue.VenueEntity;
import com.reservationsystem.venue.VenueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class EventSeatServiceTest {

    @Mock
    private EventSeatRepository eventSeatRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private EventSeatService eventSeatService;

    private final VenueEntity venue = new VenueEntity("National Arena", "Bucharest");

    private EventEntity newEvent(UUID venueId) {
        return new EventEntity(
                venueId,
                "Metallica",
                "Metallica concert",
                Instant.parse("2026-10-10T18:00:00Z"),
                Instant.parse("2026-10-10T21:00:00Z")
        );
    }

    @Test
    void addSeat_existingEvent_createsSeatAndReturnsEventSeat() {
        UUID venueId = UUID.randomUUID();
        EventEntity event = newEvent(venueId);
        UUID eventId = UUID.randomUUID();
        AddSeatRequest request = new AddSeatRequest("A", "1", "12", 1.0, 2.0, 0.0, BigDecimal.valueOf(100), "RON");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(venueRepository.findById(venueId)).thenReturn(Optional.of(venue));
        when(seatRepository.save(any(SeatEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventSeatRepository.save(any(EventSeatEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventSeat result = eventSeatService.addSeat(eventId, request);

        assertThat(result.seat().seatNumber()).isEqualTo("12");
        assertThat(result.price()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.status()).isEqualTo(EventSeatEntity.AVAILABLE);
    }

    @Test
    void addSeat_missingEvent_throwsResponseStatusException() {
        UUID eventId = UUID.randomUUID();
        AddSeatRequest request = new AddSeatRequest("A", "1", "12", 1.0, 2.0, 0.0, BigDecimal.valueOf(100), "RON");

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventSeatService.addSeat(eventId, request))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void addSeat_missingVenue_throwsResponseStatusException() {
        UUID venueId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        EventEntity event = newEvent(venueId);
        AddSeatRequest request = new AddSeatRequest("A", "1", "12", 1.0, 2.0, 0.0, BigDecimal.valueOf(100), "RON");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(venueRepository.findById(venueId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventSeatService.addSeat(eventId, request))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getSeatsFor_eventHasSeats_returnsAllSeats() {
        UUID eventId = UUID.randomUUID();
        EventEntity event = newEvent(UUID.randomUUID());
        SeatEntity seat = new SeatEntity(venue, "A", "1", "12", 1.0, 2.0, 0.0);
        EventSeatEntity eventSeat = new EventSeatEntity(event, seat, BigDecimal.valueOf(100), "RON");

        when(eventSeatRepository.findAllByEventId(eventId)).thenReturn(List.of(eventSeat));

        List<EventSeat> result = eventSeatService.getSeatsFor(eventId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAvailableSeatsFor_eventHasAvailableSeats_returnsOnlyAvailable() {
        UUID eventId = UUID.randomUUID();
        EventEntity event = newEvent(UUID.randomUUID());
        SeatEntity seat = new SeatEntity(venue, "A", "1", "12", 1.0, 2.0, 0.0);
        EventSeatEntity eventSeat = new EventSeatEntity(event, seat, BigDecimal.valueOf(100), "RON");

        when(eventSeatRepository.findAllByEventIdAndStatus(eventId, EventSeatEntity.AVAILABLE))
                .thenReturn(List.of(eventSeat));

        List<EventSeat> result = eventSeatService.getAvailableSeatsFor(eventId);

        assertThat(result)
                .hasSize(1)
                .allMatch(es -> es.status().equals(EventSeatEntity.AVAILABLE));
    }
}
