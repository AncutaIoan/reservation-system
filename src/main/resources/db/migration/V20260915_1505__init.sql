-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE IF NOT EXISTS app_user (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );


-- ============================================================
-- VENUES
-- ============================================================

CREATE TABLE IF NOT EXISTS venue (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(255) NOT NULL,
    address TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );


-- ============================================================
-- EVENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS event (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    venue_id UUID NOT NULL,

    name VARCHAR(255) NOT NULL,
    description TEXT,

    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_event_venue
    FOREIGN KEY (venue_id)
    REFERENCES venue(id),

    CONSTRAINT chk_event_time
    CHECK (
              ends_at IS NULL
              OR ends_at > starts_at
          )
    );


CREATE INDEX IF NOT EXISTS idx_event_venue_id
    ON event(venue_id);

CREATE INDEX IF NOT EXISTS idx_event_starts_at
    ON event(starts_at);


-- ============================================================
-- PHYSICAL SEATS
--
-- Represents the actual seat inside a venue.
--
-- Availability does NOT belong here because the same physical
-- seat can be available for one event and reserved for another.
-- ============================================================

CREATE TABLE IF NOT EXISTS seat (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    venue_id UUID NOT NULL,

    section VARCHAR(100),
    row_label VARCHAR(50) NOT NULL,
    seat_number VARCHAR(50) NOT NULL,

    -- Coordinates for eventual SVG seat map
    x DOUBLE PRECISION,
    y DOUBLE PRECISION,
    rotation DOUBLE PRECISION NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_seat_venue
    FOREIGN KEY (venue_id)
    REFERENCES venue(id),

    CONSTRAINT uq_seat_location
    UNIQUE (
               venue_id,
               section,
               row_label,
               seat_number
           )
    );


CREATE INDEX IF NOT EXISTS idx_seat_venue_id
    ON seat(venue_id);


-- ============================================================
-- EVENT SEATS
--
-- Represents a seat for a specific event.
--
-- This is where price and availability live.
--
-- This table will later be important for concurrency handling.
-- ============================================================

CREATE TABLE IF NOT EXISTS event_seat (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    event_id UUID NOT NULL,
    seat_id UUID NOT NULL,

    price NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'RON',

    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',

    -- Used later for optimistic locking.
    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_event_seat_event
    FOREIGN KEY (event_id)
    REFERENCES event(id),

    CONSTRAINT fk_event_seat_seat
    FOREIGN KEY (seat_id)
    REFERENCES seat(id),

    CONSTRAINT uq_event_seat
    UNIQUE (event_id, seat_id),

    CONSTRAINT chk_event_seat_price
    CHECK (price >= 0),

    CONSTRAINT chk_event_seat_status
    CHECK (
              status IN (
              'AVAILABLE',
              'HELD',
              'RESERVED',
              'BLOCKED'
                        )
    )
    );


CREATE INDEX IF NOT EXISTS idx_event_seat_event_id
    ON event_seat(event_id);

CREATE INDEX IF NOT EXISTS idx_event_seat_seat_id
    ON event_seat(seat_id);

CREATE INDEX IF NOT EXISTS idx_event_seat_event_status
    ON event_seat(event_id, status);


-- ============================================================
-- RESERVATIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS reservation (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,
    event_id UUID NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'HELD',

    expires_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,

    CONSTRAINT fk_reservation_user
    FOREIGN KEY (user_id)
    REFERENCES app_user(id),

    CONSTRAINT fk_reservation_event
    FOREIGN KEY (event_id)
    REFERENCES event(id),

    CONSTRAINT chk_reservation_status
    CHECK (
              status IN (
              'HELD',
              'CONFIRMED',
              'EXPIRED',
              'CANCELLED'
                        )
    )
    );


CREATE INDEX IF NOT EXISTS idx_reservation_user_id
    ON reservation(user_id);

CREATE INDEX IF NOT EXISTS idx_reservation_event_id
    ON reservation(event_id);

CREATE INDEX IF NOT EXISTS idx_reservation_status
    ON reservation(status);

CREATE INDEX IF NOT EXISTS idx_reservation_expires_at
    ON reservation(expires_at);


-- ============================================================
-- RESERVATION SEATS
--
-- A reservation can contain one or multiple seats.
--
-- This is preferable to storing seat_id directly on reservation
-- because later somebody may reserve A10 + A11 + A12 together.
-- ============================================================

CREATE TABLE IF NOT EXISTS reservation_seat (
                                                reservation_id UUID NOT NULL,
                                                event_seat_id UUID NOT NULL,

                                                created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                PRIMARY KEY (
                                                reservation_id,
                                                event_seat_id
),

    CONSTRAINT fk_reservation_seat_reservation
    FOREIGN KEY (reservation_id)
    REFERENCES reservation(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_reservation_seat_event_seat
    FOREIGN KEY (event_seat_id)
    REFERENCES event_seat(id)
    );


CREATE INDEX IF NOT EXISTS idx_reservation_seat_event_seat
    ON reservation_seat(event_seat_id);


-- ============================================================
-- PAYMENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS payment (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    reservation_id UUID NOT NULL,

    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'RON',

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    -- Protects against duplicate payment requests.
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,

    external_payment_id VARCHAR(255),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,

    CONSTRAINT fk_payment_reservation
    FOREIGN KEY (reservation_id)
    REFERENCES reservation(id),

    CONSTRAINT chk_payment_amount
    CHECK (amount >= 0),

    CONSTRAINT chk_payment_status
    CHECK (
              status IN (
              'PENDING',
              'SUCCESS',
              'FAILED',
              'REFUNDED'
                        )
    )
    );


CREATE INDEX IF NOT EXISTS idx_payment_reservation_id
    ON payment(reservation_id);

CREATE INDEX IF NOT EXISTS idx_payment_status
    ON payment(status);


-- ============================================================
-- TRANSACTIONAL OUTBOX
-- ============================================================

CREATE TABLE IF NOT EXISTS outbox_event (
                                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    aggregate_id UUID NOT NULL,

    event_type VARCHAR(255) NOT NULL,

    payload JSONB NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ
    );


CREATE INDEX IF NOT EXISTS idx_outbox_event_unpublished
    ON outbox_event(created_at)
    WHERE published_at IS NULL;