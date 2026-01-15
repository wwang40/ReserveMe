package org.reserv.reserveme.reservation;

import java.nio.file.AccessDeniedException;
import org.reserv.reserveme.reservation.dto.CreateReservationRequest;
import org.reserv.reserveme.reservation.dto.ReservationResponse;
import org.reserv.reserveme.reservation.dto.AvailabilitySlotResponse;
import org.reserv.reserveme.reservation.AvailabilitySlot;
import org.reserv.reserveme.reservation.AvailabilitySlotRepository;
import org.reserv.reserveme.reservation.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;
    private final AvailabilitySlotRepository slotRepository;

    public ReservationController(ReservationService reservationService, AvailabilitySlotRepository slotRepository) {
        this.reservationService = reservationService;
        this.slotRepository = slotRepository;
    }

    private UUID getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        try {
            return UUID.fromString(auth.getPrincipal().toString());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token subject");
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@RequestBody CreateReservationRequest request) {
        UUID requesterId = getAuthenticatedUserId();
        request.setRequesterId(requesterId);
        try {
            log.info("Creating reservation for requester={} slotId={}", requesterId, request.getSlotId());
            Reservation r = reservationService.createReservation(request);
            AvailabilitySlot slot = slotRepository.findById(r.getSlotId()).orElse(null);
            return ReservationResponse.from(r, slot);
        } catch (IllegalArgumentException ex) {
            log.warn("Failed to create reservation: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @DeleteMapping("/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID reservationId) throws AccessDeniedException {
        UUID callerId = getAuthenticatedUserId();
        try {
            log.info("Delete reservation request={} caller={}", reservationId, callerId);
            reservationService.deleteReservation(reservationId, callerId);
        } catch (IllegalArgumentException ex) {
            log.warn("Delete failed: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (AccessDeniedException ex) {
            log.warn("Delete denied: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }
    }

    @PutMapping("/{reservationId}/confirm")
    public ReservationResponse confirm(@PathVariable UUID reservationId) throws AccessDeniedException {
        UUID ownerId = getAuthenticatedUserId();
        try {
            log.info("Confirm reservation request={} owner={}", reservationId, ownerId);
            Reservation r = reservationService.confirmReservation(reservationId, ownerId);
            AvailabilitySlot slot = slotRepository.findById(r.getSlotId()).orElse(null);
            return ReservationResponse.from(r, slot);
        } catch (IllegalArgumentException ex) {
            log.warn("Confirm failed: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (AccessDeniedException ex) {
            log.warn("Confirm denied: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }
    }

    @GetMapping
    public List<ReservationResponse> list(@RequestParam(required = false) UUID userId) {
        List<Reservation> reservations;
        if (userId != null) {
            reservations = reservationService.listReservationsForUser(userId);
        } else {
            reservations = reservationService.listReservations();
        }
        return reservations.stream().map(r -> {
            AvailabilitySlot slot = slotRepository.findById(r.getSlotId()).orElse(null);
            return ReservationResponse.from(r, slot);
        }).collect(Collectors.toList());
    }

    @GetMapping("/byRequester")
    public List<ReservationResponse> listByRequester(@RequestParam UUID requesterId) {
        return reservationService.listReservationsForUser(requesterId).stream().map(r -> {
            AvailabilitySlot slot = slotRepository.findById(r.getSlotId()).orElse(null);
            return ReservationResponse.from(r, slot);
        }).collect(Collectors.toList());
    }

    @GetMapping("/bySlot")
    public List<ReservationResponse> listBySlot(@RequestParam UUID slotId) {
        return reservationService.findBySlotId(slotId).stream().map(r -> {
            AvailabilitySlot slot = slotRepository.findById(r.getSlotId()).orElse(null);
            return ReservationResponse.from(r, slot);
        }).collect(Collectors.toList());
    }
}
