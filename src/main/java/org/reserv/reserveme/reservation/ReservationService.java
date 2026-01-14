package org.reserv.reserveme.reservation;

import org.reserv.reserveme.reservation.dto.CreateReservationRequest;
import org.reserv.reserveme.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final AvailabilitySlotRepository slotRepository;

    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository, AvailabilitySlotRepository slotRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
    }

    public Reservation createReservation(CreateReservationRequest request) {
        UUID requesterId = request.getRequesterId();
        UUID slotId = request.getSlotId();

        if (requesterId == null || slotId == null) {
            throw new IllegalArgumentException("requesterId and slotId are required");
        }

        var user = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        var slot = slotRepository.findById(slotId).orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        // If there is already an ACTIVE reservation for this slot, reject
        List<Reservation> existing = reservationRepository.findBySlotId(slotId);
        boolean activeExists = existing.stream().anyMatch(r -> "ACTIVE".equalsIgnoreCase(r.getStatus()));
        if (activeExists) {
            throw new IllegalStateException("Slot is already reserved");
        }

        Reservation reservation = new Reservation(slotId, user, "ACTIVE");
        return reservationRepository.save(reservation);
    }

    public List<Reservation> listReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> listReservationsForUser(UUID requesterId) {
        return reservationRepository.findByRequesterId(requesterId);
    }

    public List<Reservation> findBySlotId(UUID slotId) {
        return reservationRepository.findBySlotId(slotId);
    }
}
