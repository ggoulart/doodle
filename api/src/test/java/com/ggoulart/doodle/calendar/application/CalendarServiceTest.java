package com.ggoulart.doodle.calendar.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ggoulart.doodle.calendar.domain.Calendar;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private CalendarRepository calendarRepository;

    @Test
    void createCalendarSavesCalendarForGivenUser() {
        CalendarService service = new CalendarService(calendarRepository);
        UUID userId = UUID.randomUUID();
        when(calendarRepository.save(any(Calendar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createCalendar(userId);

        ArgumentCaptor<Calendar> captor = ArgumentCaptor.forClass(Calendar.class);
        verify(calendarRepository).save(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(userId);
        assertThat(captor.getValue().id()).isNotNull();
    }

    @Test
    void getCalendarByUserIdReturnsCalendarWhenFound() {
        CalendarService service = new CalendarService(calendarRepository);
        Calendar calendar = new Calendar(UUID.randomUUID(), UUID.randomUUID());
        when(calendarRepository.findByUserId(calendar.userId())).thenReturn(Optional.of(calendar));

        Optional<Calendar> result = service.getCalendarByUserId(calendar.userId());

        assertThat(result).contains(calendar);
    }

    @Test
    void getCalendarByUserIdReturnsEmptyWhenMissing() {
        CalendarService service = new CalendarService(calendarRepository);
        UUID userId = UUID.randomUUID();
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Optional<Calendar> result = service.getCalendarByUserId(userId);

        assertThat(result).isEmpty();
    }
}
