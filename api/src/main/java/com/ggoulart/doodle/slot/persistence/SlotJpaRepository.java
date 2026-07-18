package com.ggoulart.doodle.slot.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SlotJpaRepository extends JpaRepository<SlotEntity, UUID> {
}
