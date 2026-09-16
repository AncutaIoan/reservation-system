package com.reservationsystem.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationRepository
        extends JpaRepository<ReservationEntity, UUID> {
}