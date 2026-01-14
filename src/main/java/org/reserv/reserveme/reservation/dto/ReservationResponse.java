package org.reserv.reserveme.reservation.dto;

import org.reserv.reserveme.reservation.Reservation;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID slotId,
        UUID requesterId,
        String status,
        Instant createdAt
) {
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getSlotId(),
                r.getRequester().getId(),
                r.getStatus(),
                r.getCreatedAt()
        );
    }
}
