package com.ggoulart.doodle.slot.persistence;

import com.ggoulart.doodle.slot.domain.SlotStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SlotJpaRepository extends JpaRepository<SlotEntity, UUID> {

    @Query("select s from SlotEntity s "
            + "where s.calendarId = :calendarId "
            + "and s.startTime < :to and s.endTime > :from "
            + "and (:status is null or s.status = :status)")
    List<SlotEntity> findByCalendarIdAndOverlapping(
            @Param("calendarId") UUID calendarId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("status") SlotStatus status);
}
