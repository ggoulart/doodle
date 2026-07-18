package com.ggoulart.doodle.calendar.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CalendarJpaRepository extends JpaRepository<CalendarEntity, UUID> {
}
