package com.reservationsystem.venue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueService venueService;

    @Test
    void getAllVenues_venuesExist_returnsAllVenues() {
        VenueEntity venue = new VenueEntity("National Arena", "Bucharest");

        when(venueRepository.findAll()).thenReturn(List.of(venue));

        List<Venue> result = venueService.getAllVenues();

        assertThat(result)
                .hasSize(1)
                .extracting(Venue::Name)
                .containsExactly("National Arena");
    }

    @Test
    void getAllVenues_noVenues_returnsEmptyList() {
        when(venueRepository.findAll()).thenReturn(List.of());

        List<Venue> result = venueService.getAllVenues();

        assertThat(result).isEmpty();
    }
}
