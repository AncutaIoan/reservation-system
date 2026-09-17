package com.reservationsystem.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeatEntity, ReservationSeatId> {
    List<ReservationSeatEntity> findAllByReservationId(UUID reservationId);
}
