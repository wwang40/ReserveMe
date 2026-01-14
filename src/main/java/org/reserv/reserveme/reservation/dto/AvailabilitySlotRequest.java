package org.reserv.reserveme.reservation.dto;

import java.time.Instant;
import java.util.UUID;

public class AvailabilitySlotRequest {
    private UUID ownerId;
    private Instant startTime;
    private Instant endTime;

    public AvailabilitySlotRequest() {}

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
}

