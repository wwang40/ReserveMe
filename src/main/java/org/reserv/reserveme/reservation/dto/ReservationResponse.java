package org.reserv.reserveme.reservation.dto;

import org.reserv.reserveme.reservation.Reservation;
import org.reserv.reserveme.reservation.AvailabilitySlot;
import org.reserv.reserveme.reservation.dto.AvailabilitySlotResponse;
import org.reserv.reserveme.user.dto.UserResponse;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        AvailabilitySlotResponse slot,
        UserResponse user,
        String status,
        Instant createdAt
) {
    public static ReservationResponse from(Reservation r, AvailabilitySlot slot) {
        return new ReservationResponse(
                r.getId(),
                slot != null ? AvailabilitySlotResponse.from(slot) : null,
                UserResponse.from(r.getRequester()),
                r.getStatus(),
                r.getCreatedAt()
        );
    }
}
