# ReserveMe

ReserveMe is a small scheduling/reservation microservice and reference frontend that lets users publish availability slots and other users request reservations for those slots. Owners can accept or reject requests, and either side can cancel reservations.

This repository contains:
- A Java Spring Boot backend (REST API).
- A React + Vite frontend in the `frontend/` folder.
- SQL migrations for schema under `src/main/resources/db/migration`.
- Tests (unit + integration) under `src/test`.

Key features
- User accounts and JWT authentication (register/login).
- Create availability slots (owner, start/end times).
- Request a reservation for a slot (creates a PENDING reservation).
- Owners can view incoming requests and accept or reject them.
- Confirmed reservations appear for both requester and owner.
- Cancel a reservation (by owner or requester).
- API-first design with clear endpoints for frontend integration.

Quick start (development)

Prerequisites
- Java 17+ (or the version specified in `pom.xml`).
- Maven 3.6+ (mvnw wrapper is provided).
- Node.js 18+ (for the frontend) and npm.
- Docker Desktop (optional, used by Testcontainers and docker-compose).

Run backend
1. Build and run with Maven (from project root):

```powershell
./mvnw spring-boot:run
```

or build the jar and run:

```powershell
./mvnw package; java -jar target/ReserveMe-0.0.1-SNAPSHOT.jar
```

By default the backend listens on http://localhost:8080.

Run frontend (development)

```powershell
cd frontend; npm install; npm run dev
```

Open the Vite dev server (printed URL) in your browser. The frontend expects the backend at http://localhost:8080 by default.

Docker (optional)

- Start the app with docker-compose (trusting the included Dockerfile):

```powershell
docker-compose up --build
```

This runs the backend (and frontend if configured in the compose file) inside containers.

Testing

- Run unit + integration tests with Maven:

```powershell
./mvnw test
```

- Integration tests use Testcontainers (Docker Desktop must be running).

API overview

See `API_DOC.md` for a full API reference, examples, and frontend mapping suggestions. Major reservation endpoints:
- POST /api/reservations -> create request (PENDING)
- GET /api/reservations/incoming?ownerId=<uuid> -> owner incoming requests
- POST /api/reservations/{id}/accept -> owner accepts (CONFIRMED)
- POST /api/reservations/{id}/reject -> owner rejects (REJECTED)
- POST /api/reservations/{id}/cancel -> cancel reservation (CANCELLED)

Database and migrations

The project uses Flyway migrations under `src/main/resources/db/migration`.

Deployment

A simple production deploy strategy:
1. Build the fat jar: `./mvnw package`.
2. Build Docker image (optional): `docker build -t reserveme:latest .`.
3. Push image to a registry and deploy to your platform of choice (Heroku, ECS, GKE, Azure App Service, etc.).

CI/CD notes

- Add a pipeline step to run `./mvnw -B verify`.
- Ensure tests using Testcontainers either run on an agent with Docker or are annotated to skip in CI where Docker is not available.

Contributing

- Create issues for bugs or feature ideas.
- Open PRs with tests for new behavior.

Contact

If you need help running the app locally or want feature help, reply in the repo issues.

