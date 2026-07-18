package com.ggoulart.doodle.slot.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SlotJpaRepository extends JpaRepository<SlotEntity, UUID> {

    List<SlotEntity> findByCalendarIdAndStartTimeLessThanAndEndTimeGreaterThan(UUID calendarId, Instant to, Instant from);
}
