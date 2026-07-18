package com.ggoulart.doodle.slot.persistence;

import com.ggoulart.doodle.slot.application.SlotRepository;
import com.ggoulart.doodle.slot.domain.Slot;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
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

    @Override
    public Optional<Slot> findById(UUID id) {
        return slotJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Slot> findByCalendarIdAndOverlapping(UUID calendarId, Instant from, Instant to) {
        return slotJpaRepository.findByCalendarIdAndStartTimeLessThanAndEndTimeGreaterThan(calendarId, to, from)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        try {
            slotJpaRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ignored) {
            // already gone - deleting a nonexistent slot is a no-op, not an error
        }
    }

    private Slot toDomain(SlotEntity entity) {
        return new Slot(
                entity.getId(), entity.getCalendarId(), entity.getStartTime(), entity.getEndTime(), entity.getStatus());
    }
}
