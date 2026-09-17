package com.reservationsystem.eventseat;

import com.reservationsystem.event.EventEntity;
import com.reservationsystem.event.EventRepository;
import com.reservationsystem.seat.SeatEntity;
import com.reservationsystem.seat.SeatRepository;
import com.reservationsystem.venue.VenueEntity;
import com.reservationsystem.venue.VenueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class EventSeatService {

    private final EventSeatRepository eventSeatRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final VenueRepository venueRepository;

    public EventSeatService(
            EventSeatRepository eventSeatRepository,
            EventRepository eventRepository,
            SeatRepository seatRepository,
            VenueRepository venueRepository
    ) {
        this.eventSeatRepository = eventSeatRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.venueRepository = venueRepository;
    }

    public EventSeat addSeat(UUID eventId, AddSeatRequest request) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + eventId));

        VenueEntity venue = venueRepository.findById(event.getVenueId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found: " + event.getVenueId()));

        SeatEntity seat = new SeatEntity(
                venue,
                request.section(),
                request.rowLabel(),
                request.seatNumber(),
                request.x(),
                request.y(),
                request.rotation()
        );
        SeatEntity savedSeat = seatRepository.save(seat);

        EventSeatEntity eventSeat = new EventSeatEntity(event, savedSeat, request.price(), request.currency());
        EventSeatEntity savedEventSeat = eventSeatRepository.save(eventSeat);

        return new EventSeat(savedEventSeat);
    }

    public List<EventSeat> getSeatsFor(UUID eventId) {
        return eventSeatRepository.findAllByEventId(eventId)
                .stream()
                .map(EventSeat::new)
                .toList();
    }

    public List<EventSeat> getAvailableSeatsFor(UUID eventId) {
        return eventSeatRepository.findAllByEventIdAndStatus(eventId, EventSeatEntity.AVAILABLE)
                .stream()
                .map(EventSeat::new)
                .toList();
    }
}
