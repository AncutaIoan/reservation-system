package com.reservationsystem.seat;

import com.reservationsystem.TestcontainersConfiguration;
import com.reservationsystem.venue.VenueEntity;
import com.reservationsystem.venue.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class SeatRepositoryTest {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Test
    void findAllByVenueId_venueHasSeats_returnsOnlySeatsForVenue() {
        VenueEntity venueA = venueRepository.saveAndFlush(new VenueEntity("National Arena", "Bucharest"));
        VenueEntity venueB = venueRepository.saveAndFlush(new VenueEntity("Cluj Arena", "Cluj-Napoca"));

        SeatEntity seatA1 = new SeatEntity(venueA, "A", "1", "12", 1.0, 2.0, 0.0);
        SeatEntity seatA2 = new SeatEntity(venueA, "A", "1", "13", 2.0, 2.0, 0.0);
        SeatEntity seatB1 = new SeatEntity(venueB, "A", "1", "12", 1.0, 2.0, 0.0);

        seatRepository.saveAll(List.of(seatA1, seatA2, seatB1));

        List<SeatEntity> result = seatRepository.findAllByVenueId(venueA.getId());

        assertThat(result)
                .hasSize(2)
                .allMatch(seat -> seat.getVenue().getId().equals(venueA.getId()));
    }

    @Test
    void findAllByVenueId_venueHasNoSeats_returnsEmptyList() {
        List<SeatEntity> result = seatRepository.findAllByVenueId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}
