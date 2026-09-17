package com.reservationsystem.seat;

import com.reservationsystem.venue.VenueEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatService seatService;

    @Test
    void getSeatsFor_venueHasSeats_returnsSeatsForVenue() {
        UUID venueId = UUID.randomUUID();
        VenueEntity venue = new VenueEntity("National Arena", "Bucharest");
        SeatEntity seat = new SeatEntity(venue, "A", "1", "12", 1.0, 2.0, 0.0);

        when(seatRepository.findAllByVenueId(venueId)).thenReturn(List.of(seat));

        List<Seat> result = seatService.getSeatsFor(venueId);

        assertThat(result)
                .hasSize(1)
                .extracting(Seat::seatNumber)
                .containsExactly("12");
    }

    @Test
    void getSeatsFor_venueHasNoSeats_returnsEmptyList() {
        UUID venueId = UUID.randomUUID();

        when(seatRepository.findAllByVenueId(venueId)).thenReturn(List.of());

        List<Seat> result = seatService.getSeatsFor(venueId);

        assertThat(result).isEmpty();
    }
}
