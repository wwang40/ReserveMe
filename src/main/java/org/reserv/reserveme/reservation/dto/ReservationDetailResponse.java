package org.reserv.reserveme.reservation.dto;

import org.reserv.reserveme.reservation.AvailabilitySlot;
import org.reserv.reserveme.reservation.AvailabilitySlot;
import org.reserv.reserveme.reservation.dto.AvailabilitySlotResponse;
import org.reserv.reserveme.user.dto.UserResponse;

import java.time.Instant;
import java.util.UUID;

public record ReservationDetailResponse(
        UUID id,
        AvailabilitySlotResponse slot,
        UserResponse user,
        String status,
        Instant createdAt
) {
}

