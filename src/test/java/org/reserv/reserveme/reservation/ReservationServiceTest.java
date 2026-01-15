package org.reserv.reserveme.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reserv.reserveme.reservation.dto.CreateReservationRequest;
import org.reserv.reserveme.user.User;
import org.reserv.reserveme.user.UserRepository;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AvailabilitySlotRepository slotRepository;

    @InjectMocks
    private ReservationService reservationService;

    private UUID userId;
    private UUID slotId;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        slotId = UUID.randomUUID();
    }

    private static void setId(Object o, UUID id) throws Exception {
        Field f = o.getClass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(o, id);
    }

    @Test
    void createReservation_success() throws Exception {
        // Arrange
        CreateReservationRequest req = new CreateReservationRequest(userId, slotId);
        User user = new User("u@example.com", "passhash", "ROLE_USER", "User");
        setId(user, userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        AvailabilitySlot slot = new AvailabilitySlot(user, Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200));
        setId(slot, slotId);
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(reservationRepository.findBySlotId(slotId)).thenReturn(List.of());

        // capture saved reservation
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        when(reservationRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        // Act
        Reservation saved = reservationService.createReservation(req);

        // Assert
        verify(reservationRepository, times(1)).save(any());
        assertThat(saved.getSlotId()).isEqualTo(slotId);
        assertThat(saved.getRequester().getEmail()).isEqualTo("u@example.com");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void createReservation_slotAlreadyReserved_throws() throws Exception {
        CreateReservationRequest req = new CreateReservationRequest(userId, slotId);
        User user = new User("u@example.com", "passhash", "ROLE_USER", "User");
        setId(user, userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        AvailabilitySlot slot = new AvailabilitySlot(user, Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200));
        setId(slot, slotId);
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        Reservation existing = new Reservation(slotId, user, "ACTIVE");
        when(reservationRepository.findBySlotId(slotId)).thenReturn(List.of(existing));

        assertThrows(IllegalStateException.class, () -> reservationService.createReservation(req));
    }

    @Test
    void confirmReservation_success() throws Exception {
        // Arrange existing reservation
        User requester = new User("r@example.com", "h", "ROLE_USER", "Requester");
        UUID requesterId = UUID.randomUUID();
        setId(requester, requesterId);
        UUID resId = UUID.randomUUID();
        Reservation res = new Reservation(slotId, requester, "ACTIVE");
        setId(res, resId);
        when(reservationRepository.findById(resId)).thenReturn(Optional.of(res));

        User owner = new User("o@example.com", "h", "ROLE_USER", "Owner");
        UUID ownerId = UUID.randomUUID();
        setId(owner, ownerId);
        AvailabilitySlot slot = new AvailabilitySlot(owner, Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200));
        setId(slot, slotId);
        when(slotRepository.findById(res.getSlotId())).thenReturn(Optional.of(slot));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        Reservation confirmed = reservationService.confirmReservation(resId, ownerId);

        // Assert
        assertThat(confirmed.getStatus()).isEqualTo("CONFIRMED");
        verify(reservationRepository, times(1)).save(res);
    }

    @Test
    void deleteReservation_byOwner_allowed() throws Exception {
        User requester = new User("r@example.com", "h", "ROLE_USER", "Requester");
        UUID requesterId = UUID.randomUUID();
        setId(requester, requesterId);
        UUID resId = UUID.randomUUID();
        Reservation res = new Reservation(slotId, requester, "ACTIVE");
        setId(res, resId);
        when(reservationRepository.findById(resId)).thenReturn(Optional.of(res));

        User owner = new User("o@example.com", "h", "ROLE_USER", "Owner");
        UUID ownerId = UUID.randomUUID();
        setId(owner, ownerId);
        AvailabilitySlot slot = new AvailabilitySlot(owner, Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200));
        setId(slot, slotId);
        when(slotRepository.findById(res.getSlotId())).thenReturn(Optional.of(slot));

        // Act
        reservationService.deleteReservation(resId, ownerId);

        // Assert
        verify(reservationRepository, times(1)).delete(res);
    }

    @Test
    void listReservationsForUser_mergesIncomingOutgoing() throws Exception {
        // setup a single user who is both requester of r1 and owner of slot for r2
        User user = new User("u@example.com", "h", "ROLE_USER", "UserX");
        UUID userIdLocal = UUID.randomUUID();
        setId(user, userIdLocal);

        UUID r1Id = UUID.randomUUID();
        Reservation r1 = new Reservation(UUID.randomUUID(), user, "ACTIVE");
        setId(r1, r1Id);

        AvailabilitySlot slot = new AvailabilitySlot(user, Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200));
        UUID slotOwnedId = UUID.randomUUID();
        setId(slot, slotOwnedId);

        UUID r2Id = UUID.randomUUID();
        Reservation r2 = new Reservation(slotOwnedId, user, "ACTIVE");
        setId(r2, r2Id);

        // when querying by requesterId -> return r1
        when(reservationRepository.findByRequesterId(userIdLocal)).thenReturn(List.of(r1));
        // when finding slots by owner id -> return slot
        when(slotRepository.findByOwnerId(userIdLocal)).thenReturn(List.of(slot));
        // when querying reservations by slotId -> return r2
        when(reservationRepository.findBySlotId(slotOwnedId)).thenReturn(List.of(r2));

        // Act
        List<Reservation> merged = reservationService.listReservationsForUser(userIdLocal);

        // Assert results contains r1 and r2
        assertThat(merged).contains(r1);
        assertThat(merged).contains(r2);
    }
}
