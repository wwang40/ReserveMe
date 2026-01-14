package org.reserv.reserveme.reservation;

import jakarta.persistence.*;
import lombok.Getter;
import org.reserv.reserveme.user.User;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "availability_slots")
public class AvailabilitySlot {
    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AvailabilitySlot() {}

    public AvailabilitySlot(User owner, Instant startTime, Instant endTime) {
        this.owner = owner;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = Instant.now();
    }

    // Explicit getters
    public UUID getId() { return id; }
    public User getOwner() { return owner; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public Instant getCreatedAt() { return createdAt; }
}

