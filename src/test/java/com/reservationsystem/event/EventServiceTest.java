package com.reservationsystem.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void getAllEvents_eventsExist_returnsAllEvents() {
        EventEntity event = new EventEntity(
                UUID.randomUUID(),
                "Metallica",
                "Metallica concert",
                Instant.parse("2026-10-10T18:00:00Z"),
                Instant.parse("2026-10-10T21:00:00Z")
        );

        when(eventRepository.findAll()).thenReturn(List.of(event));

        List<Event> result = eventService.getAllEvents();

        assertThat(result)
                .hasSize(1)
                .extracting(Event::name)
                .containsExactly("Metallica");
    }

    @Test
    void findBy_matchingName_returnsMatchingEvents() {
        EventEntity event = new EventEntity(
                UUID.randomUUID(),
                "Coldplay",
                "Coldplay concert",
                Instant.parse("2026-11-01T18:00:00Z"),
                Instant.parse("2026-11-01T21:00:00Z")
        );

        when(eventRepository.findAllByName("Coldplay")).thenReturn(List.of(event));

        List<Event> result = eventService.findBy("Coldplay");

        assertThat(result)
                .hasSize(1)
                .extracting(Event::name)
                .containsExactly("Coldplay");
    }

    @Test
    void findBy_noMatchingName_returnsEmptyList() {
        when(eventRepository.findAllByName("Unknown")).thenReturn(List.of());

        List<Event> result = eventService.findBy("Unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void createEvent_validRequest_savesAndReturnsEvent() {
        UUID venueId = UUID.randomUUID();
        CreateEventRequest request = new CreateEventRequest(
                venueId,
                "Metallica",
                "Metallica concert",
                Instant.parse("2026-10-10T18:00:00Z"),
                Instant.parse("2026-10-10T21:00:00Z")
        );

        when(eventRepository.save(any(EventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.createEvent(request);

        verify(eventRepository).save(any(EventEntity.class));
        assertThat(result.venueId()).isEqualTo(venueId);
        assertThat(result.name()).isEqualTo("Metallica");
        assertThat(result.description()).isEqualTo("Metallica concert");
        assertThat(result.startsAt()).isEqualTo(Instant.parse("2026-10-10T18:00:00Z"));
        assertThat(result.endsAt()).isEqualTo(Instant.parse("2026-10-10T21:00:00Z"));
    }
}
