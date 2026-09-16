package com.reservationsystem.seat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SeatService {
    private final SeatRepository seatRepository;

    @Autowired
    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }


    public List<Seat> getSeatsFor(UUID venueId) {
        return seatRepository.findAllByVenueId(venueId)
                                .stream()
                                .map(Seat::new)
                                .toList();
    }
}
