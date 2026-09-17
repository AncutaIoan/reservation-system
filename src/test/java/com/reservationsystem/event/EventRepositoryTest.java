package com.reservationsystem.event;

import com.reservationsystem.TestcontainersConfiguration;
import com.reservationsystem.venue.VenueEntity;
import com.reservationsystem.venue.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Test
    void shouldFindAllEventsByName() {
        VenueEntity venue = new VenueEntity(
                "National Arena",
                "Bucharest"
        );

        VenueEntity savedVenue = venueRepository.saveAndFlush(venue);

        EventEntity event1 = new EventEntity(
                savedVenue.getId(),
                "Metallica",
                "Metallica concert",
                Instant.parse("2026-10-10T18:00:00Z"),
                Instant.parse("2026-10-10T21:00:00Z")
        );

        EventEntity event2 = new EventEntity(
                savedVenue.getId(),
                "Metallica",
                "Second Metallica concert",
                Instant.parse("2026-10-11T18:00:00Z"),
                Instant.parse("2026-10-11T21:00:00Z")
        );

        EventEntity event3 = new EventEntity(
                savedVenue.getId(),
                "Coldplay",
                "Coldplay concert",
                Instant.parse("2026-11-01T18:00:00Z"),
                Instant.parse("2026-11-01T21:00:00Z")
        );

        eventRepository.saveAll(List.of(
                event1,
                event2,
                event3
        ));

        List<EventEntity> result =
                eventRepository.findAllByName("Metallica");

        assertThat(result)
                .hasSize(2)
                .allMatch(event -> event.getName().equals("Metallica"));
    }
}