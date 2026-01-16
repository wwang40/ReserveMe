# ReserveMe API Documentation (for frontend implementation)

This document describes the REST API exposed by ReserveMe and provides request/response schemas, example requests/responses, and UI mapping recommendations to help generate a React frontend.

Base URL
- http://localhost:8080

Authentication
- JWT-based authentication using `/api/auth/register` and `/api/auth/login`.
- After login/register the server returns a JWT in the response body as `{ "token": "..." }`.
- Frontend should store the token (in memory, secure storage, or httpOnly cookie) and include it in Authorization header: `Authorization: Bearer <token>`.

JSON conventions
- Dates/times are ISO-8601 instants (e.g., 2026-01-13T14:00:00Z).
- UUIDs are strings.

---

Entities and DTOs (shapes)

- User (server entity)
  - id: UUID
  - email: string
  - displayName: string
  - createdAt: Instant

- CreateUserRequest
  - email: string
  - displayName: string

- UserResponse
  - id: UUID
  - email: string
  - displayName: string
  - createdAt: Instant

- AvailabilitySlot (server entity)
  - id: UUID
  - ownerId: UUID (user id)
  - startTime: Instant
  - endTime: Instant
  - createdAt: Instant

- AvailabilitySlotRequest
  - ownerId: UUID
  - startTime: Instant
  - endTime: Instant

- AvailabilitySlotResponse
  - id: UUID
  - ownerId: UUID
  - startTime: Instant
  - endTime: Instant
  - createdAt: Instant

- Reservation (server entity)
  - id: UUID
  - slotId: UUID
  - requesterId: UUID
  - status: string (PENDING, CONFIRMED, REJECTED, CANCELLED)
  - createdAt: Instant

- CreateReservationRequest
  - requesterId: UUID
  - slotId: UUID

- ReservationResponse
  - id: UUID
  - slotId: UUID
  - requesterId: UUID
  - status: string
  - createdAt: Instant

---

Statuses & semantics
- PENDING: A requester has asked to reserve a slot. The owner must accept or reject.
- CONFIRMED: Owner accepted the reservation. This should appear under "confirmed reservations" for both the requester and the owner.
- REJECTED: Owner rejected the incoming request. Requester should see it as rejected.
- CANCELLED: Either side cancelled an existing PENDING or CONFIRMED reservation.

Important behavior expectations (frontend/backed contract):
- When a reservation is created it should be persisted with status PENDING.
- Owners should see their incoming PENDING requests via an "incoming" endpoint.
- Requesters should see their outgoing requests (PENDING) via a "byRequester" endpoint.
- When owner accepts: status => CONFIRMED and slot should be considered reserved (further reservation attempts should return 409).
- When owner rejects: status => REJECTED.
- When either party cancels: status => CANCELLED.

---

Endpoints

1) Users

- POST /api/users
  - Description: Create a user
  - Request body: CreateUserRequest
  - Response: UserResponse (201)
  - Errors: 400 for invalid input, 409 for duplicate email

- GET /api/users
  - Description: List all users
  - Response: [UserResponse]


2) Availability slots

- POST /api/slots
  - Description: Create an availability slot (owner's available window)
  - Request body: AvailabilitySlotRequest
  - Response: AvailabilitySlotResponse (201)
  - Errors: 400 for invalid input (start >= end), 404 if owner not found

- GET /api/slots
  - Description: List all availability slots
  - Response: [AvailabilitySlotResponse]

- GET /api/slots/byOwner?ownerId=<uuid>
  - Description: List availability slots for a specific owner
  - Response: [AvailabilitySlotResponse]


3) Reservations (request/confirm/reject/cancel flows)

- POST /api/reservations
  - Description: Create a reservation request for a slot by a requester
  - Request body: CreateReservationRequest
  - Behavior: Creates a reservation with status PENDING. If a CONFIRMED reservation already exists for the same slot, returns 409 Conflict
  - Response: ReservationResponse (201)
  - Errors: 400 for missing requesterId/slotId or invalid references; 409 for slot already confirmed/reserved

- GET /api/reservations
  - Description: List all reservations
  - Response: [ReservationResponse]

- GET /api/reservations/byRequester?requesterId=<uuid>
  - Description: List reservations created by a user (outgoing requests)
  - Response: [ReservationResponse]

- GET /api/reservations/incoming?ownerId=<uuid>
  - Description: List incoming reservation requests for slots owned by `ownerId` (status filter optional)
  - Response: [ReservationResponse]
  - Notes: This is the endpoint owners use to view PENDING requests to accept/reject.

- POST /api/reservations/{id}/accept
  - Description: Owner accepts a PENDING reservation
  - Path param: reservation id
  - Behavior: Only the slot owner can accept. Transitions status PENDING -> CONFIRMED. Should mark the slot as reserved; further reservation attempts for that slot return 409.
  - Response: ReservationResponse (200)
  - Errors: 403 if caller is not slot owner; 404 if reservation not found; 409 if slot already confirmed by another reservation; 400 if reservation not in PENDING state

- POST /api/reservations/{id}/reject
  - Description: Owner rejects a PENDING reservation
  - Behavior: Only the slot owner can reject. Transitions PENDING -> REJECTED.
  - Response: ReservationResponse (200)
  - Errors: 403 if caller is not slot owner; 404 if reservation not found; 400 if reservation not in PENDING state

- POST /api/reservations/{id}/cancel
  - Description: Cancels a reservation. Can be invoked by the requester or the owner depending on the business rules.
  - Behavior: Transitions PENDING or CONFIRMED -> CANCELLED. If CONFIRMED, both users should see this reservation in their history as cancelled. If the owner cancels a PENDING reservation, the requester should be notified (status CANCELLED).
  - Response: ReservationResponse (200)
  - Errors: 403 if caller not involved in reservation; 404 if reservation not found

- GET /api/reservations/confirmed/byUser?userId=<uuid>
  - Description: List confirmed reservations (both where user is the requester or owner)
  - Response: [ReservationResponse]


4) Authentication

- POST /api/auth/register
  - Description: Register a new user. Returns a JWT.
  - Request body: `{ "email": "user@example.com", "password": "plaintext" }`
  - Response: `{ "token": "<jwt>" }` (201)
  - Errors: 400 for invalid input, 409 if email already exists.

- POST /api/auth/login
  - Description: Login existing user. Returns a JWT.
  - Request body: `{ "email": "user@example.com", "password": "plaintext" }`
  - Response: `{ "token": "<jwt>" }` (200)
  - Errors: 400 for invalid credentials.

---

Example requests (curl - PowerShell style)

- Create user

```powershell
curl -X POST -H "Content-Type: application/json" -d '{"email":"alice@example.com","displayName":"Alice"}' http://localhost:8080/api/users
```

- Create a slot

```powershell
curl -X POST -H "Content-Type: application/json" -d '{"ownerId":"<user-uuid>","startTime":"2026-01-20T09:00:00Z","endTime":"2026-01-20T10:00:00Z"}' http://localhost:8080/api/slots
```

- Create reservation (request)

```powershell
curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer <token>" -d '{"requesterId":"<user-uuid>","slotId":"<slot-uuid>"}' http://localhost:8080/api/reservations
```

- Owner lists incoming requests

```powershell
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/reservations/incoming?ownerId=<owner-uuid>
```

- Owner accepts a request

```powershell
curl -X POST -H "Authorization: Bearer <token>" http://localhost:8080/api/reservations/<reservation-id>/accept
```

- Owner rejects a request

```powershell
curl -X POST -H "Authorization: Bearer <token>" http://localhost:8080/api/reservations/<reservation-id>/reject
```

- Cancel a reservation (requester or owner)

```powershell
curl -X POST -H "Authorization: Bearer <token>" http://localhost:8080/api/reservations/<reservation-id>/cancel
```

- Get confirmed reservations for a user

```powershell
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/reservations/confirmed/byUser?userId=<user-uuid>
```

---

Frontend mapping suggestions (React)

- Pages / Views
  1. UsersList (list users) — GET /api/users
  2. SlotList (list slots) — GET /api/slots
  3. CreateSlot (form) — POST /api/slots
  4. SlotDetail (slot + reservations) — GET /api/slots/byOwner and GET /api/reservations/bySlot
  5. ReservationList (for user) — GET /api/reservations/byRequester?requesterId=<id>
  6. IncomingRequests (for owner) — GET /api/reservations/incoming?ownerId=<id>
  7. ConfirmedReservations (for user) — GET /api/reservations/confirmed/byUser?userId=<id>
  8. CreateReservation (choose slot + requester) — POST /api/reservations
  9. Login (form) — POST /api/auth/login
  10. Register (form) — POST /api/auth/register

- Components
  - UserSelect: fetches /api/users, displays a dropdown
  - SlotCard: displays slot times and owner
  - ReservationCard: displays reservation info and action buttons (accept/reject/cancel) depending on role & status
  - ErrorBanner: shows API error messages (400/409) from responses
  - LoginForm: handles login
  - RegisterForm: handles registration

- Data flow and optimistic UI
  - When creating a reservation, POST to `/api/reservations`. If 201, update UI. If 409, show error banner (slot already reserved).
  - Owners should poll or subscribe to incoming requests (or use web sockets) to get near-real-time updates when someone requests a slot.
  - On accept: send POST to `/api/reservations/{id}/accept` and update local lists: remove from incoming, add to confirmed for both users.
  - On reject: send POST to `/api/reservations/{id}/reject` and remove from incoming.
  - On cancel: send POST to `/api/reservations/{id}/cancel` and update both users' lists accordingly.

- Forms & validation
  - Do client-side validation (start < end, email format). Always show server-side error messages on failure.
  - For login/register, validate email/password format and show appropriate messages.

- Time handling
  - Use native Date or a library (date-fns) to parse/format ISO-8601 instants.
  - Display times in user's local timezone.

---

Error handling conventions (for frontend)

- 400: show validation message near the form
- 403: show "Not authorized" message
- 404: show "Not Found" state
- 409: show clear conflict message (e.g., "Slot already reserved")

---

Appendix: Minimal API client examples (JS using fetch)

```js
// api/index.js
const api = (path, opts = {}) => fetch(`http://localhost:8080${path}`, {
  headers: { 'Content-Type': 'application/json', ...(opts.headers || {}) },
  ...opts
}).then(async res => {
  const text = await res.text();
  let data = text ? JSON.parse(text) : null;
  if (!res.ok) {
    const err = new Error(data?.message || res.statusText);
    err.status = res.status;
    err.body = data;
    throw err;
  }
  return data;
});

export default api;

// api/users.js
import api from './index';
export const listUsers = () => api('/api/users');
export const createUser = (payload) => api('/api/users', { method: 'POST', body: JSON.stringify(payload) });

// api/slots.js
import api from './index';
export const listSlots = () => api('/api/slots');
export const createSlot = (payload) => api('/api/slots', { method: 'POST', body: JSON.stringify(payload) });

// api/reservations.js
import api from './index';
export const createReservation = (payload) => api('/api/reservations', { method: 'POST', body: JSON.stringify(payload) });
export const listReservations = () => api('/api/reservations');
export const listByRequester = (id) => api(`/api/reservations/byRequester?requesterId=${id}`);
export const listIncoming = (ownerId) => api(`/api/reservations/incoming?ownerId=${ownerId}`);
export const acceptReservation = (id) => api(`/api/reservations/${id}/accept`, { method: 'POST' });
export const rejectReservation = (id) => api(`/api/reservations/${id}/reject`, { method: 'POST' });
export const cancelReservation = (id) => api(`/api/reservations/${id}/cancel`, { method: 'POST' });
export const confirmedByUser = (id) => api(`/api/reservations/confirmed/byUser?userId=${id}`);

// api/auth.js
import api from './index';
export const register = (payload) => api('/api/auth/register', { method: 'POST', body: JSON.stringify(payload) });
export const login = (payload) => api('/api/auth/login', { method: 'POST', body: JSON.stringify(payload) });
```

---

If you'd like, I can also:
- Generate the frontend components that implement incoming requests, accept/reject flows and ensure the requester sees pending state.
- Add or adjust backend controller endpoints if your server is missing `incoming`, `accept`, `reject`, or `cancel` handlers.
- Add integration tests that exercise the full request -> accept -> confirmed lifecycle (using Testcontainers), and/or unit tests covering the ReservationService.

Tell me which next step you want and I'll implement it.
