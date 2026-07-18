package com.ggoulart.doodle.slot.persistence;

import com.ggoulart.doodle.slot.application.SlotRepository;
import com.ggoulart.doodle.slot.domain.Slot;
import org.springframework.stereotype.Repository;

@Repository
class JpaSlotRepository implements SlotRepository {

    private final SlotJpaRepository slotJpaRepository;

    JpaSlotRepository(SlotJpaRepository slotJpaRepository) {
        this.slotJpaRepository = slotJpaRepository;
    }

    @Override
    public Slot save(Slot slot) {
        SlotEntity entity = new SlotEntity(
                slot.id(), slot.calendarId(), slot.startTime(), slot.endTime(), slot.status());
        SlotEntity saved = slotJpaRepository.save(entity);
        return toDomain(saved);
    }

    private Slot toDomain(SlotEntity entity) {
        return new Slot(
                entity.getId(), entity.getCalendarId(), entity.getStartTime(), entity.getEndTime(), entity.getStatus());
    }
}
