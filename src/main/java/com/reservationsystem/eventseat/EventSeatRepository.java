package com.reservationsystem.eventseat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventSeatRepository extends JpaRepository<EventSeatEntity, UUID> {
    List<EventSeatEntity> findAllByEventId(UUID eventId);

    List<EventSeatEntity> findAllByEventIdAndStatus(UUID eventId, String status);
}
