package org.reserv.reserveme.reservation.dto;

import java.time.Instant;
import java.util.UUID;

public class CreateReservationRequest {
    private UUID requesterId;
    private UUID slotId;

    public CreateReservationRequest() {}

    public CreateReservationRequest(UUID requesterId, UUID slotId) {
        this.requesterId = requesterId;
        this.slotId = slotId;
    }

    public UUID getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(UUID requesterId) {
        this.requesterId = requesterId;
    }

    public UUID getSlotId() {
        return slotId;
    }

    public void setSlotId(UUID slotId) {
        this.slotId = slotId;
    }
}
