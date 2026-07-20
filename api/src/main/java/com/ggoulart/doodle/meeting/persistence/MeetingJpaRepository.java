package com.ggoulart.doodle.meeting.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface MeetingJpaRepository extends JpaRepository<MeetingEntity, UUID> {

    boolean existsBySlotId(UUID slotId);
}
