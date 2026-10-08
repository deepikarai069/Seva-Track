package com.sevatrack;

import com.sevatrack.model.Complaint;
import com.sevatrack.model.Officer;
import com.sevatrack.model.Priority;
import com.sevatrack.model.Status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class TestData {
    public static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 7, 10, 0);

    private TestData() {}

    public static Clock fixedClock() {
        return Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    }

    public static Officer officer(int id, int dept, Integer ward, int level, int open, int max, boolean emergency) {
        Officer o = new Officer();
        o.setId(id);
        o.setName("Officer " + id);
        o.setDepartmentId(dept);
        o.setWardId(ward);
        o.setLevel(level);
        o.setOpenLoad(open);
        o.setMaxLoad(max);
        o.setEmergency(emergency);
        return o;
    }

    public static Complaint complaint(long id, int dept, int ward, Priority p, Status s, int level, LocalDateTime due) {
        Complaint c = new Complaint();
        c.setId(id);
        c.setTicketNo("ST-TEST-" + id);
        c.setDepartmentId(dept);
        c.setWardId(ward);
        c.setPriority(p);
        c.setStatus(s);
        c.setEscalationLevel(level);
        c.setSlaDueAt(due);
        return c;
    }

    /** A clock tests can move forward. */
    public static final class MutableClock extends Clock {
        private Instant now;

        public MutableClock(Instant start) {
            this.now = start;
        }

        public void advanceHours(long h) {
            now = now.plusSeconds(h * 3600);
        }

        @Override public ZoneId getZone() { return ZoneId.systemDefault(); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }
}
