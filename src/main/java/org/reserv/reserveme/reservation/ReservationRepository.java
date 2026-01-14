package org.reserv.reserveme.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByRequesterId(UUID requesterId);
    List<Reservation> findBySlotId(UUID slotId);
    List<Reservation> findByStatus(String status);
}
