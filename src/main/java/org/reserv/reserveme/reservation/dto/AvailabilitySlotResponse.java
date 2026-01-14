package org.reserv.reserveme.reservation.dto;

import org.reserv.reserveme.reservation.AvailabilitySlot;

import java.time.Instant;
import java.util.UUID;

public record AvailabilitySlotResponse(
        UUID id,
        UUID ownerId,
        Instant startTime,
        Instant endTime,
        Instant createdAt
) {
    public static AvailabilitySlotResponse from(AvailabilitySlot s) {
        return new AvailabilitySlotResponse(
                s.getId(),
                s.getOwner().getId(),
                s.getStartTime(),
                s.getEndTime(),
                s.getCreatedAt()
        );
    }
}

