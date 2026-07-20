[![CI](https://github.com/ggoulart/doodle/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/ggoulart/doodle/actions/workflows/build.yml)

---
Gustavo Goulart - Doodle
---

A mini Doodle: a meeting scheduling API. Users have a personal calendar; calendars hold time slots; a slot can be booked as a meeting with a title, description, and participants. Slots can be queried by time range and free/busy status, and the data model is built to keep that querying fast as a calendar grows into the thousands of slots.

## Running the application

```
docker compose up --build
```

This starts the API at `http://localhost:8080`, a Postgres database, and a Spring Boot Admin server at `http://localhost:8081`. Add `-d` to run detached, and `docker compose down` (or `docker compose down -v` to also drop the database volume) to stop.

## Running the tests

```
./gradlew test
```

Unit tests use Mockito with no Spring context. The application-context test (`ApiApplicationTests`) spins up a real Postgres instance via Testcontainers, so it requires Docker to be running locally too. This doesn't include the Gatling load test, which exercises performance and concurrency separately — see [Performance & scale](#performance--scale) below.

## Architecture

The codebase is split into four bounded contexts — `user`, `calendar`, `slot`, `meeting` — each internally organized as hexagonal/ports-and-adapters:

- **`domain`** — the entity/value objects and their own invariants. Domain objects are "always valid": invalid construction throws immediately (e.g. `User` rejects a blank name or malformed email in its own constructor; `Slot` rejects `endTime <= startTime`; `Meeting` defaults a blank title). This guarantees the invariant holds no matter which adapter or caller constructs the object.
- **`application`** — ports (`XUseCase` interfaces for what the outside world can ask the core to do, `XRepository` interfaces for what the core needs from persistence) and the services that implement the use cases. Anything that requires a collaborator to check (e.g. "is this email already taken," "does this user exist") lives here rather than in `domain`, since a single entity can't answer that on its own.
- **`web`** — the driving adapter: REST controllers and request DTOs, plus an `@RestControllerAdvice` translating each package's own exceptions into HTTP status codes.
- **`persistence`** — the driven adapter: JPA entities and Spring Data repositories, implementing the `application` layer's repository ports. Maps `domain` objects to/from JPA entities; the `application`/`domain` layers have no JPA/Postgres awareness.

`calendar` intentionally has no `web` package — the spec calls for "calendar" to exist only in the domain, not as an exposed API concept. A user's calendar is created automatically as part of `POST /users`, and other packages resolve a user's calendar internally through `calendar`'s ports (never through an HTTP call).

Slot creation deliberately doesn't reject overlapping slots within the same calendar — a design choice, not a gap: multiple overlapping `FREE` slots can coexist as competing options for the same time (e.g. offering both a 30-minute and 60-minute slot over the same window), and whichever gets booked first wins, same as any other double-booking race.

Cross-context calls go through the target context's own `application` ports directly (e.g. `slot` depends on `user.application.GetUserUseCase` and `calendar.application.GetCalendarUseCase`; `meeting` depends on `slot.application.GetSlotUseCase`/`UpdateSlotUseCase`) — no shared database access or reaching into another package's internals. This does mean `slot` and `meeting` depend on each other (`slot` needs to know whether a slot is already booked before letting it be mutated; `meeting` needs to read/update the slot it's booking). To keep that coupling as narrow as possible, `slot` depends on a single-method `meeting.application.SlotHasMeetingUseCase` port rather than the full `MeetingRepository` — implemented by its own small `MeetingLookupService`, kept separate from `MeetingService` so the two contexts' beans don't form a circular constructor-injection dependency.

## API

Once the app is running, see the Swagger UI at `http://localhost:8080/swagger-ui/index.html` for the full list of endpoints, request/response schemas, and status codes (raw spec at `/v3/api-docs`). It's kept in sync with the code via annotations rather than duplicated here. Timestamps throughout the API are ISO-8601 UTC instants (e.g. `"2026-07-20T10:00:00Z"`).

## Performance & scale

The slot model is built around one query that has to stay fast as calendars grow: "what does this calendar look like between `from` and `to`, optionally filtered by status?" (`GET /slots`). That's a range-overlap query (`start_time < :to AND end_time > :from`) filtered by `calendar_id` — the composite index `idx_slots_calendar_id_start_time_end_time` on `slots(calendar_id, start_time, end_time)` exists specifically so Postgres can satisfy it with an index scan instead of a per-calendar sequential scan once a calendar holds hundreds or thousands of slots.

The other half of scale is concurrency: many users booking against the same or overlapping data at once. Two users can legitimately race for the same slot, and exactly one of them has to win, cleanly — a `409`, not a `500` or a double-booked slot. `users.email`, `calendars.user_id`, `meetings.slot_id`, and `(meeting_id, participant)` on `meeting_participants` are all enforced by DB-level `UNIQUE` constraints rather than a check-then-act pre-check in application code, so two concurrent requests can no longer both succeed no matter how the timing lines up. Each persistence adapter (`JpaUserRepository`, `JpaCalendarRepository`, `JpaMeetingRepository`) catches the resulting `DataIntegrityViolationException`, confirms — by checking the actual violated constraint's name — that it's the one it expects before translating it into the matching clean 400/409, and rethrows anything else untouched. (One related gap that isn't closed this way — plain slot edits outside of booking — is in [Known limitations](#known-limitations).)

Rather than just asserting the above, `api/src/gatling` has a Gatling simulation (`SchedulingApiSimulation`) that exercises it under real load, in three chained phases:

1. **Seed** — creates a fixed pool of users/calendars, each loaded with dozens of slots, so the later phases have real volume to query and book against instead of near-empty calendars.
2. **Query** — ramps concurrent `GET /slots` traffic against those now-large calendars, exercising the composite index under load.
3. **Booking contention** — deliberately draws bookings from a narrow slice of the seeded slot pool so multiple virtual users race for the exact same slot, asserting every response is either `201` or `409` — i.e. that the unique-constraint race behaves correctly under real concurrent load, not just in a unit test.

Run it locally with:

```
./gradlew :api:gatlingRun
```

(the app needs to already be running, e.g. via `docker compose up`). The HTML report lands under `api/build/reports/gatling/`. It also runs as part of CI on every push (the `test` job in `.github/workflows/build.yml`), with the report uploaded as a build artifact — so a regression in query performance or in booking correctness under contention gets caught automatically rather than relying on manual testing.

## Monitoring

The `api` module has Spring Boot Actuator, and registers itself with a Spring Boot Admin server (its own module, `admin/`, built from `Dockerfile.admin`) at `http://localhost:8081`. Open that URL to see the app's health, JVM/HTTP metrics, config properties, loggers, and more, all live.

`/actuator/health` is a real check, not decoration: since the app has a Postgres `DataSource`, Boot auto-adds a DB health indicator, so the endpoint reports `DOWN` (with the actual JDBC error) if Postgres becomes unreachable, and recovers to `UP` once it's back — confirmed by stopping Postgres and watching it happen live. It's the only health endpoint the app exposes.

Neither the Actuator endpoints nor the Admin server have any authentication — same already-acknowledged tradeoff as Swagger above, not a new one. No pre-built Docker image for Spring Boot Admin covers the 4.x line this project's Spring Boot 4.1 needs (the community image on Docker Hub tops out at 3.4.1), so the `admin` module is built from source the same way `api` is.

## Known limitations

Deliberate tradeoffs for this stage of the project, not oversights:

- **No authentication/authorization anywhere.** Any caller can act on any user/slot/meeting by id (e.g. delete someone else's slot) if they know or guess its UUID, and every Actuator endpoint (`env`, `heapdump`, `threaddump`, etc., via `management.endpoints.web.exposure.include: "*"`) is open too. Fine for local/demo use; would need locking down before running anywhere reachable outside localhost.
- **`CalendarAlreadyExistsException` has no HTTP mapping.** The uniqueness races described in [Performance & scale](#performance--scale) are closed at the DB level and translated into clean 400/409 responses everywhere else, but `calendar` deliberately has no `web` layer of its own, so this one exception has nowhere to be mapped. In practice it's only reachable via a UUID collision on a just-generated id — effectively unreachable — but if it's ever thrown it would surface as an unhandled 500 during `POST /users`.
- **No optimistic locking on slot edits outside of booking.** The DB constraint in [Performance & scale](#performance--scale) only closes the double-booking race; a plain `PATCH /slots/{id}` that isn't going through booking has no conflict protection at all — two concurrent edits to the same slot's time range or status silently last-write-wins, with no conflict surfaced to the loser. A `@Version` column plus rejecting stale writes with a `409` is the natural fix; not yet in place.
- **No pagination on `GET /slots`.** Results are bounded only by the `from`/`to` range (defaulting to 7 days when omitted), with no page size cap — a dense calendar queried over a wide explicit range returns everything in one response.
- **Schema is managed by Hibernate (`ddl-auto: update`)**, not a migration tool like Flyway — fine for this stage, but has no version history and won't safely handle destructive schema changes.
