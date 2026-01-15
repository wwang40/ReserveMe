package org.reserv.reserveme.reservation;

import java.nio.file.AccessDeniedException;
import org.reserv.reserveme.reservation.dto.CreateReservationRequest;
import org.reserv.reserveme.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

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
    public void deleteReservation(UUID reservationId, UUID requesterId)
            throws AccessDeniedException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        // Allow deletion by the requester (cancel) OR by the slot owner (reject)
        UUID callerId = requesterId;
        boolean isRequestingUser = reservation.getRequester().getId().equals(callerId);
        boolean isSlotOwner = false;
        var slotOpt = slotRepository.findById(reservation.getSlotId());
        if (slotOpt.isPresent()) {
            isSlotOwner = slotOpt.get().getOwner().getId().equals(callerId);
        }

        if (!isRequestingUser && !isSlotOwner) {
            throw new AccessDeniedException("Not authorized to delete this reservation");
        }

        reservationRepository.delete(reservation);
    }

    public Reservation confirmReservation(UUID reservationId, UUID ownerId) throws AccessDeniedException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        // ensure caller is the owner of the slot
        var slot = slotRepository.findById(reservation.getSlotId()).orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        if (!slot.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Not authorized to confirm this reservation");
        }

        reservation.setStatus("CONFIRMED");
        return reservationRepository.save(reservation);
    }

    public List<Reservation> listReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> listReservationsForUser(UUID requesterId) {
        // Return reservations either requested by the user (outgoing)
        // or reservations for slots owned by the user (incoming).
        List<Reservation> byRequester = reservationRepository.findByRequesterId(requesterId);

        // find slot ids owned by user
        List<org.reserv.reserveme.reservation.AvailabilitySlot> slots = slotRepository.findByOwnerId(requesterId);
        List<Reservation> bySlots = new ArrayList<>();
        for (var s : slots) {
            bySlots.addAll(reservationRepository.findBySlotId(s.getId()));
        }

        // merge and dedupe preserving order (requester first)
        Map<UUID, Reservation> map = new LinkedHashMap<>();
        for (var r : byRequester) map.put(r.getId(), r);
        for (var r : bySlots) map.putIfAbsent(r.getId(), r);
        return new ArrayList<>(map.values());
    }

    public List<Reservation> findBySlotId(UUID slotId) {
        return reservationRepository.findBySlotId(slotId);
    }
}
