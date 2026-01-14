package org.reserv.reserveme.reservation;

import org.reserv.reserveme.reservation.dto.CreateReservationRequest;
import org.reserv.reserveme.reservation.dto.ReservationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@RequestBody CreateReservationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            try {
                UUID requesterId = UUID.fromString(auth.getPrincipal().toString());
                request.setRequesterId(requesterId);
            } catch (Exception ignored) {
            }
        }
        Reservation r = reservationService.createReservation(request);
        return ReservationResponse.from(r);
    }

    @GetMapping
    public List<ReservationResponse> list() {
        return reservationService.listReservations().stream().map(ReservationResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/byRequester")
    public List<ReservationResponse> listByRequester(@RequestParam UUID requesterId) {
        return reservationService.listReservationsForUser(requesterId).stream().map(ReservationResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/bySlot")
    public List<ReservationResponse> listBySlot(@RequestParam UUID slotId) {
        return reservationService.findBySlotId(slotId).stream().map(ReservationResponse::from).collect(Collectors.toList());
    }
}
