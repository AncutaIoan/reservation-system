package com.reservationsystem.user;

import java.time.Instant;
import java.util.UUID;

public record User(
        UUID id,
        String email,
        String displayName,
        Instant createdAt
) {
    public User(UserEntity userEntity) {
        this(
                userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getDisplayName(),
                userEntity.getCreatedAt()
        );
    }
}
