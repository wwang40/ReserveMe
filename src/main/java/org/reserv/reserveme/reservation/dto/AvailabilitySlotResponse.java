package org.reserv.reserveme.reservation.dto;

import org.reserv.reserveme.reservation.AvailabilitySlot;
import org.reserv.reserveme.user.dto.UserResponse;

import java.time.Instant;
import java.util.UUID;

public record AvailabilitySlotResponse(
        UUID id,
        UserResponse owner,
        Instant startTime,
        Instant endTime,
        Instant createdAt
) {
    public static AvailabilitySlotResponse from(AvailabilitySlot s) {
        return new AvailabilitySlotResponse(
                s.getId(),
                UserResponse.from(s.getOwner()),
                s.getStartTime(),
                s.getEndTime(),
                s.getCreatedAt()
        );
    }
}
