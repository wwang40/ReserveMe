package org.reserv.reserveme.reservation;

import org.reserv.reserveme.reservation.dto.AvailabilitySlotRequest;
import org.reserv.reserveme.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AvailabilitySlotService {
    private final AvailabilitySlotRepository slotRepository;
    private final UserRepository userRepository;

    public AvailabilitySlotService(AvailabilitySlotRepository slotRepository, UserRepository userRepository) {
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    public AvailabilitySlot createSlot(AvailabilitySlotRequest request) {
        var owner = userRepository.findById(request.getOwnerId()).orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        if (request.getStartTime() == null || request.getEndTime() == null || !request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Invalid start/end time");
        }
        AvailabilitySlot slot = new AvailabilitySlot(owner, request.getStartTime(), request.getEndTime());
        return slotRepository.save(slot);
    }

    public List<AvailabilitySlot> listAll() {
        return slotRepository.findAll();
    }

    public List<AvailabilitySlot> listByOwner(UUID ownerId) {
        return slotRepository.findByOwnerId(ownerId);
    }
}

