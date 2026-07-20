package com.ggoulart.doodle.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Exercises the spec's explicit scale requirement ("hundreds of users, thousands of slots") in
 * three chained phases:
 *
 * <ol>
 *   <li>Seed: a fixed pool of calendars, each loaded with {@link #SLOTS_PER_USER} slots, so the
 *       overlap query actually has to filter through real volume instead of 0-1 rows.</li>
 *   <li>Query: hammers {@code GET /slots} against those large calendars concurrently.</li>
 *   <li>Booking contention: books slots drawn from a narrow slice of the seeded pool, so multiple
 *       virtual users deliberately race for the same slot and exercise the 201/409 split.</li>
 * </ol>
 */
public class SchedulingApiSimulation extends Simulation {

    private static final int SEED_USERS = 60;
    private static final int SLOTS_PER_USER = 50;

    private static final ConcurrentLinkedQueue<String> SEEDED_USER_IDS = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> SEEDED_SLOT_IDS = new ConcurrentLinkedQueue<>();

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // ---- Phase 1: seed a handful of calendars, each with tens of slots ----

    Supplier<Map<String, Object>> newUserFeed = () -> {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return Map.of(
                "username", "Seed_" + uniqueId,
                "email", "seed_" + uniqueId + "@teste.com"
        );
    };

    ScenarioBuilder seedScenario = scenario("Seed a calendar with many slots")
            .feed(Stream.generate(newUserFeed).iterator())
            .exec(http("Criar Usuário (seed)")
                    .post("/users")
                    .body(StringBody("{\"name\": \"#{username}\", \"email\": \"#{email}\"}"))
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("createdUserId"))
            )
            .exec(session -> {
                SEEDED_USER_IDS.add(session.getString("createdUserId"));
                return session;
            })
            .repeat(SLOTS_PER_USER, "slotIndex").on(
                    exec(session -> {
                        int i = session.getInt("slotIndex");
                        Instant start = Instant.parse("2026-09-01T00:00:00Z").plus(Duration.ofMinutes(60L * i));
                        Instant end = start.plus(Duration.ofMinutes(30));
                        return session.set("slotStart", start.toString()).set("slotEnd", end.toString());
                    })
                    .exec(http("Criar Slot (seed)")
                            .post("/slots")
                            .body(StringBody(
                                    "{\"userId\": \"#{createdUserId}\", \"startTime\": \"#{slotStart}\", \"endTime\": \"#{slotEnd}\", \"status\": \"FREE\"}"))
                            .check(status().is(201))
                            .check(jsonPath("$.id").saveAs("createdSlotId"))
                    )
                    .exec(session -> {
                        SEEDED_SLOT_IDS.add(session.getString("createdSlotId"));
                        return session;
                    })
            );

    // ---- Phase 2: hammer GET /slots against those large calendars ----

    Supplier<Map<String, Object>> queryFeed = () -> {
        List<String> pool = new ArrayList<>(SEEDED_USER_IDS);
        String userId = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        return Map.of("queryUserId", userId);
    };

    ScenarioBuilder queryScenario = scenario("Query a large calendar")
            .feed(Stream.generate(queryFeed).iterator())
            .exec(http("Consultar Slots (calendário grande)")
                    .get("/slots")
                    .queryParam("userId", "#{queryUserId}")
                    .queryParam("from", "2026-09-01T00:00:00Z")
                    .queryParam("to", "2026-09-05T00:00:00Z")
                    .check(status().is(200))
            )
            .pause(1);

    // ---- Phase 3: concurrent booking attempts, some deliberately colliding on the same slot ----

    Supplier<Map<String, Object>> bookingFeed = () -> {
        List<String> pool = new ArrayList<>(SEEDED_SLOT_IDS);
        // Draw from a narrow front slice of the pool so multiple virtual users have a real
        // chance of racing for the exact same slot, exercising the 201-then-409 split.
        int contendedRange = Math.max(1, pool.size() / 20);
        String slotId = pool.get(ThreadLocalRandom.current().nextInt(contendedRange));
        String participantEmail = "racer_" + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
        return Map.of("bookingSlotId", slotId, "participantEmail", participantEmail);
    };

    ScenarioBuilder bookingContentionScenario = scenario("Concurrent booking attempts")
            .feed(Stream.generate(bookingFeed).iterator())
            .exec(http("Reservar Reunião (contenção)")
                    .post("/slots/#{bookingSlotId}/meetings")
                    .body(StringBody(
                            "{\"title\": \"Contenção\", \"description\": \"Load test\", \"participants\": [\"#{participantEmail}\"]}"))
                    .check(status().in(201, 409))
            );

    // ---- Load profile ----
    {
        setUp(
                seedScenario.injectOpen(atOnceUsers(SEED_USERS))
                        .andThen(
                                queryScenario.injectOpen(
                                        rampUsersPerSec(5).to(50).during(20),
                                        constantUsersPerSec(50).during(40)
                                ),
                                bookingContentionScenario.injectOpen(
                                        rampUsersPerSec(2).to(10).during(20),
                                        constantUsersPerSec(10).during(40)
                                )
                        )
        ).protocols(httpProtocol);
    }
}
