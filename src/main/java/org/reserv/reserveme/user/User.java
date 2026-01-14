package org.reserv.reserveme.user;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
@Entity
@Table(name = "users")
public class User {
    // Explicit getters to ensure they are available at compile time
    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "display_name")
    private String displayName;

    protected User() {}

    // Original constructor kept for compatibility
    public User(String email, String passwordHash, String role) {
        this(email, passwordHash, role, null);
    }

    // New constructor that accepts a displayName
    public User(String email, String passwordHash, String role, String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.displayName = displayName;
        this.createdAt = Instant.now();
    }

    public String getDisplayName() {
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }
        if (email == null || email.isBlank()) {
            return "";
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    // Explicit getters to avoid relying on Lombok during compilation
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
}
