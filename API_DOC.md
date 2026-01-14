# ReserveMe API Documentation (for frontend implementation)

This document describes the REST API exposed by ReserveMe and provides request/response schemas, example requests/responses, and UI mapping recommendations to help generate a React frontend (for example with Copilot or other generators).

Base URL
- http://localhost:8080

Authentication
- JWT-based authentication using `/api/auth/register` and `/api/auth/login`.
- After login/register the server returns a JWT in the response body as `{ "token": "..." }`.
- Frontend should store the token (in memory or secure storage) and include it in Authorization header: `Authorization: Bearer <token>`.

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
  - status: string (e.g., ACTIVE)
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


3) Reservations

- POST /api/reservations
  - Description: Create a reservation for a slot by a requester
  - Request body: CreateReservationRequest
  - Behavior: If a reservation already exists with status ACTIVE for the same slot, returns 409 Conflict
  - Response: ReservationResponse (201)
  - Errors: 400 for missing requesterId/slotId or invalid references; 409 for slot already reserved

- GET /api/reservations
  - Description: List all reservations
  - Response: [ReservationResponse]

- GET /api/reservations/byRequester?requesterId=<uuid>
  - Description: List reservations requested by a user
  - Response: [ReservationResponse]

- GET /api/reservations/bySlot?slotId=<uuid>
  - Description: List reservations for a slot
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

- Create reservation

```powershell
curl -X POST -H "Content-Type: application/json" -d '{"requesterId":"<user-uuid>","slotId":"<slot-uuid>"}' http://localhost:8080/api/reservations
```

- Register

```powershell
curl -X POST -H "Content-Type: application/json" -d '{"email":"alice@example.com","password":"securepassword"}' http://localhost:8080/api/auth/register
```

- Login

```powershell
curl -X POST -H "Content-Type: application/json" -d '{"email":"alice@example.com","password":"securepassword"}' http://localhost:8080/api/auth/login
```

---

Frontend mapping suggestions (React)

- Pages / Views
  1. UsersList (list users) — GET /api/users
  2. SlotList (list slots) — GET /api/slots
  3. CreateSlot (form) — POST /api/slots
  4. SlotDetail (slot + reservations) — GET /api/slots/byOwner and GET /api/reservations/bySlot
  5. ReservationList (for user) — GET /api/reservations/byRequester?requesterId=<id>
  6. CreateReservation (choose slot + requester) — POST /api/reservations
  7. Login (form) — POST /api/auth/login
  8. Register (form) — POST /api/auth/register

- Components
  - UserSelect: fetches /api/users, displays a dropdown
  - SlotCard: displays slot times and owner
  - ReservationCard: displays reservation info
  - ErrorBanner: shows API error messages (400/409) from responses
  - LoginForm: handles login
  - RegisterForm: handles registration

- Data flow and optimistic UI
  - When creating a reservation, POST to `/api/reservations`. If 201, update UI. If 409, show error banner (slot already reserved).
  - On login/register, store the JWT and update the user context/state.

- Forms & validation
  - Do client-side validation (start < end, email format). Always show server-side error messages on failure.
  - For login/register, validate email/password format and show appropriate messages.

- Time handling
  - Use native Date or a library (date-fns) to parse/format ISO-8601 instants.
  - Display times in user's local timezone.

---

Suggested React folder structure (simple)

src/
  api/
    index.js           // wrapper around fetch/axios
    users.js           // functions: listUsers, createUser
    slots.js           // functions: listSlots, createSlot, listSlotsByOwner
    reservations.js    // functions: listReservations, createReservation, listByRequester, listBySlot
    auth.js             // functions: register, login
  components/
    UserSelect.jsx
    SlotCard.jsx
    ReservationCard.jsx
    ErrorBanner.jsx
    LoginForm.jsx
    RegisterForm.jsx
  pages/
    UsersList.jsx
    SlotsList.jsx
    SlotDetail.jsx
    CreateReservation.jsx
    Login.jsx
    Register.jsx

---

Error handling conventions (for frontend)

- 400: show validation message near the form
- 404: show "Not Found" state
- 409: show clear conflict message (e.g., "Slot already reserved")

---

Appendix: Minimal API client examples (JS using fetch)

```js
// api/index.js
const api = (path, opts = {}) => fetch(`http://localhost:8080${path}`, {
  headers: { 'Content-Type': 'application/json' },
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
export const listBySlot = (id) => api(`/api/reservations/bySlot?slotId=${id}`);

// api/auth.js
import api from './index';
export const register = (payload) => api('/api/auth/register', { method: 'POST', body: JSON.stringify(payload) });
export const login = (payload) => api('/api/auth/login', { method: 'POST', body: JSON.stringify(payload) });
```

---

If you'd like, I can:
- Add the `AvailabilitySlot` endpoints and DTOs to the CHANGES.md (already done), and open `API_DOC.md` in the editor for review.
- Generate React components (basic forms/pages) against the API documentation.
- Add unit + MockMvc tests to exercise the new slots/reservations endpoints.

Tell me which you'd like next and I'll implement it.
