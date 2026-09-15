package com.reservationsystem.seat;

import com.reservationsystem.venue.VenueEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "seat",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_seat_location",
                        columnNames = {
                                "venue_id",
                                "section",
                                "row_label",
                                "seat_number"
                        }
                )
        }
)
public class SeatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private VenueEntity venue;

    @Column(name = "section", length = 100)
    private String section;

    @Column(name = "row_label", nullable = false, length = 50)
    private String rowLabel;

    @Column(name = "seat_number", nullable = false, length = 50)
    private String seatNumber;

    @Column(name = "x")
    private Double x;

    @Column(name = "y")
    private Double y;

    @Column(name = "rotation", nullable = false)
    private Double rotation = 0.0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SeatEntity() {
    }

    public SeatEntity(
            VenueEntity venue,
            String section,
            String rowLabel,
            String seatNumber,
            Double x,
            Double y,
            Double rotation
    ) {
        this.venue = venue;
        this.section = section;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.x = x;
        this.y = y;
        this.rotation = rotation != null ? rotation : 0.0;
    }

    public UUID getId() {
        return id;
    }

    public VenueEntity getVenue() {
        return venue;
    }

    public String getSection() {
        return section;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Double getRotation() {
        return rotation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setPosition(Double x, Double y, Double rotation) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }
}