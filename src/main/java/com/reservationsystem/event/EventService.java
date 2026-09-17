package com.reservationsystem.event;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(Event::new)
                .toList();
    }

    public List<Event> findBy(String name) {
        return eventRepository.findAllByName(name)
                .stream()
                .map(Event::new)
                .toList();
    }
}