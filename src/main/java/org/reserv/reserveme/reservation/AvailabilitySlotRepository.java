package org.reserv.reserveme.reservation;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, UUID> {
    // Add custom query methods if needed
    List<AvailabilitySlot> findByOwnerId(UUID ownerId);
}
