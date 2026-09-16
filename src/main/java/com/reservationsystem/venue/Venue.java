package com.reservationsystem.venue;

public record Venue(
    String address,
    String Name
) {
    public Venue(VenueEntity venueEntity) {
        this(venueEntity.getAddress(), venueEntity.getName());
    }
}
