package com.ggoulart.doodle.calendar.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CalendarJpaRepository extends JpaRepository<CalendarEntity, UUID> {

    Optional<CalendarEntity> findByUserId(UUID userId);
}
