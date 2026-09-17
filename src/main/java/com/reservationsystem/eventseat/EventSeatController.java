package com.reservationsystem.eventseat;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events/{eventId}")
public class EventSeatController {

    private final EventSeatService eventSeatService;

    public EventSeatController(EventSeatService eventSeatService) {
        this.eventSeatService = eventSeatService;
    }

    @PostMapping("/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public EventSeat addSeat(@PathVariable UUID eventId, @RequestBody AddSeatRequest request) {
        return eventSeatService.addSeat(eventId, request);
    }

    @GetMapping("/seats")
    public List<EventSeat> getSeats(@PathVariable UUID eventId) {
        return eventSeatService.getSeatsFor(eventId);
    }

    @GetMapping("/available-seats")
    public List<EventSeat> getAvailableSeats(@PathVariable UUID eventId) {
        return eventSeatService.getAvailableSeatsFor(eventId);
    }
}
