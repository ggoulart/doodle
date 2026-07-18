package com.ggoulart.doodle.slot.application;

import com.ggoulart.doodle.slot.domain.Slot;
import java.util.List;

public interface QuerySlotsUseCase {

    List<Slot> querySlots(QuerySlotsCommand command);
}
