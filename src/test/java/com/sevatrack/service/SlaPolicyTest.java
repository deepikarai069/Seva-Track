package com.sevatrack.service;

import com.sevatrack.dao.SlaPolicyDao;
import com.sevatrack.model.Priority;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SlaPolicyTest {
    @Test
    void readsHoursFromTableAndCachesIt() {
        SlaPolicyDao dao = mock(SlaPolicyDao.class);
        when(dao.loadAll()).thenReturn(Map.of("LOW:2", 70));
        SlaPolicy p = new SlaPolicy(dao);
        assertEquals(Duration.ofHours(70), p.windowFor(Priority.LOW, 2));
        p.windowFor(Priority.LOW, 2);
        verify(dao, times(1)).loadAll();
    }

    @Test
    void fallsBackToSensibleDefaultsWhenRowMissing() {
        SlaPolicyDao dao = mock(SlaPolicyDao.class);
        when(dao.loadAll()).thenReturn(Map.of());
        SlaPolicy p = new SlaPolicy(dao);
        assertEquals(Duration.ofHours(4), p.windowFor(Priority.CRITICAL, 1));
        assertEquals(Duration.ofHours(120), p.windowFor(Priority.LOW, 3));
    }
}
