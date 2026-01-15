package org.reserv.reserveme.reservation;

import org.reserv.reserveme.reservation.dto.AvailabilitySlotRequest;
import org.reserv.reserveme.reservation.dto.AvailabilitySlotResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/slots")
public class AvailabilitySlotController {

    private final AvailabilitySlotService slotService;

    public AvailabilitySlotController(AvailabilitySlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilitySlotResponse create(@RequestBody AvailabilitySlotRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            try {
                UUID ownerId = UUID.fromString(auth.getPrincipal().toString());
                request.setOwnerId(ownerId);
            } catch (Exception ignored) {
            }
        }
        AvailabilitySlot s = slotService.createSlot(request);
        return AvailabilitySlotResponse.from(s);
    }
    @DeleteMapping("/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID slotId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID requesterId = UUID.fromString(auth.getPrincipal().toString());

        slotService.deleteSlot(slotId, requesterId);
    }
    @GetMapping
    public List<AvailabilitySlotResponse> list() {
        return slotService.listAll().stream().map(AvailabilitySlotResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/byOwner")
    public List<AvailabilitySlotResponse> listByOwner(@RequestParam UUID ownerId) {
        return slotService.listByOwner(ownerId).stream().map(AvailabilitySlotResponse::from).collect(Collectors.toList());
    }
}
