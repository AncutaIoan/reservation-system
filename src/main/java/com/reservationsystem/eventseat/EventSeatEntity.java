package com.reservationsystem.eventseat;

import com.reservationsystem.event.EventEntity;
import com.reservationsystem.seat.SeatEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "event_seat",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_event_seat",
                        columnNames = {"event_id", "seat_id"}
                )
        }
)
public class EventSeatEntity {

    public static final String AVAILABLE = "AVAILABLE";
    public static final String HELD = "HELD";
    public static final String RESERVED = "RESERVED";
    public static final String BLOCKED = "BLOCKED";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private SeatEntity seat;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "RON";

    @Column(name = "status", nullable = false, length = 30)
    private String status = AVAILABLE;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EventSeatEntity() {
    }

    public EventSeatEntity(EventEntity event, SeatEntity seat, BigDecimal price, String currency) {
        this.event = event;
        this.seat = seat;
        this.price = price;
        this.currency = currency != null ? currency : "RON";
        this.status = AVAILABLE;
    }

    public UUID getId() {
        return id;
    }

    public EventEntity getEvent() {
        return event;
    }

    public SeatEntity getSeat() {
        return seat;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
