package com.reservationsystem.seat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<SeatEntity, UUID> {
    List<SeatEntity> findAllByVenueId(UUID venueId);
}