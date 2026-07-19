
---
Gustavo Goulart - Doodle
---

A mini Doodle: a meeting scheduling API. Users have a personal calendar; calendars hold time slots; a slot can be booked as a meeting.

## Running the application

```
docker compose up --build
```

This starts the API at `http://localhost:8080` and a Postgres database. Add `-d` to run detached, and `docker compose down` (or `docker compose down -v` to also drop the database volume) to stop.

## Running the tests

```
./gradlew test
```

Unit tests use Mockito with no Spring context. The application-context test (`ApiApplicationTests`) spins up a real Postgres instance via Testcontainers, so it requires Docker to be running locally too.

## Architecture

The codebase is split into four bounded contexts — `user`, `calendar`, `slot`, `meeting` — each internally organized as hexagonal/ports-and-adapters:

- **`domain`** — the entity/value objects and their own invariants. Domain objects are "always valid": invalid construction throws immediately (e.g. `User` rejects a blank name or malformed email in its own constructor; `Slot` rejects `endTime <= startTime`; `Meeting` defaults a blank title). This guarantees the invariant holds no matter which adapter or caller constructs the object.
- **`application`** — ports (`XUseCase` interfaces for what the outside world can ask the core to do, `XRepository` interfaces for what the core needs from persistence) and the services that implement the use cases. Anything that requires a collaborator to check (e.g. "is this email already taken," "does this user exist") lives here rather than in `domain`, since a single entity can't answer that on its own.
- **`web`** — the driving adapter: REST controllers and request DTOs, plus an `@RestControllerAdvice` translating each package's own exceptions into HTTP status codes.
- **`persistence`** — the driven adapter: JPA entities and Spring Data repositories, implementing the `application` layer's repository ports. Maps `domain` objects to/from JPA entities; the `application`/`domain` layers have no JPA/Postgres awareness.

`calendar` intentionally has no `web` package — the spec calls for "calendar" to exist only in the domain, not as an exposed API concept. A user's calendar is created automatically as part of `POST /users`, and other packages resolve a user's calendar internally through `calendar`'s ports (never through an HTTP call).

Cross-context calls go through the target context's own `application` ports directly (e.g. `slot` depends on `user.application.GetUserUseCase` and `calendar.application.GetCalendarUseCase`; `meeting` depends on `slot.application.GetSlotUseCase`/`UpdateSlotUseCase`) — no shared database access or reaching into another package's internals.

## API

Once the app is running, see the Swagger UI at `http://localhost:8080/swagger-ui/index.html` for the full list of endpoints, request/response schemas, and status codes (raw spec at `/v3/api-docs`). It's kept in sync with the code via annotations rather than duplicated here. Timestamps throughout the API are ISO-8601 UTC instants (e.g. `"2026-07-20T10:00:00Z"`).

## Known limitations

Deliberate tradeoffs for this stage of the project, not oversights:

- **No authentication/authorization.** Any caller can act on any user/slot/meeting by id (e.g. delete someone else's slot) if they know or guess its UUID. No endpoint in this API has an auth check.
- **`MeetingService.bookSlot` has a check-then-act race window.** `users.email`, `calendars.user_id`, `meetings.slot_id`, and `(meeting_id, participant)` on `meeting_participants` now all have a DB-level `UNIQUE` constraint, so a genuine race — or a request with duplicate participants in the same list — can no longer produce duplicate/inconsistent rows. But hitting one of these constraints currently surfaces as a raw `DataIntegrityViolationException` (500), not the same clean 400/409 the application-level checks return on the normal path (confirmed: the transaction does roll back cleanly either way, so no partial/inconsistent state is left behind — just an ugly error response). Catching that exception in the persistence adapters and translating it to the existing domain exceptions is still open.
- **Deleting a slot with a booked meeting doesn't clean up the meeting.** `DELETE /slots/{id}` doesn't check for an existing meeting, so it can leave a `meetings` row pointing at a slot that no longer exists.
- **Schema is managed by Hibernate (`ddl-auto: update`)**, not a migration tool like Flyway — fine for this stage, but has no version history and won't safely handle destructive schema changes.
- **No metrics/observability** beyond the plain `/health` check (which doesn't verify DB connectivity).
