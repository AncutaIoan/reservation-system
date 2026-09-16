package com.reservationsystem.venue;

import com.reservationsystem.seat.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueRepository extends JpaRepository<VenueEntity, UUID> {

}
