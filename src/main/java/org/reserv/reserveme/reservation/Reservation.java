package org.reserv.reserveme.reservation;

import jakarta.persistence.*;
import lombok.Getter;
import org.reserv.reserveme.user.User;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "slot_id", nullable = false)
    private UUID slotId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requester_user_id", nullable = false)
    private User requester;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Reservation() {}

    public Reservation(UUID slotId, User requester, String status) {
        this.slotId = slotId;
        this.requester = requester;
        this.status = status;
        this.createdAt = Instant.now();
    }

    // Explicit getters
    public UUID getId() { return id; }
    public UUID getSlotId() { return slotId; }
    public User getRequester() { return requester; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
