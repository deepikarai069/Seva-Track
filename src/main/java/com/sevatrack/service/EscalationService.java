package com.sevatrack.service;

import com.sevatrack.dao.ComplaintDao;
import com.sevatrack.dao.OfficerDao;
import com.sevatrack.model.Complaint;
import com.sevatrack.model.Officer;
import com.sevatrack.routing.RoutingEngine;
import com.sevatrack.util.Fmt;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SLA enforcement. L1 (ward officer) -> L2 (supervisor) -> L3 (department head).
 * Each level has its own SLA window (table sla_policy). Missing the SLA at L3 flags the complaint as breached.
 */
public class EscalationService {
    public static final int MAX_LEVEL = 3;
    private static final Logger LOG = Logger.getLogger(EscalationService.class.getName());

    public record CycleResult(int checked, int escalated, int breached, int failed) {
        public String summary() {
            return checked + " overdue checked, " + escalated + " escalated, " + breached + " breached at L3, " + failed + " failed";
        }
    }

    private final ComplaintDao complaints;
    private final OfficerDao officers;
    private final SlaPolicy sla;
    private final RoutingEngine routing;
    private final Clock clock;

    public EscalationService(ComplaintDao complaints, OfficerDao officers, SlaPolicy sla, RoutingEngine routing, Clock clock) {
        this.complaints = complaints;
        this.officers = officers;
        this.sla = sla;
        this.routing = routing;
        this.clock = clock;
    }

    /** One monitoring pass. Safe to call repeatedly; failures on one complaint do not stop the others. */
    public synchronized CycleResult runCycle() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Complaint> overdue = complaints.findOverdue(now);
        int escalated = 0, breached = 0, failed = 0;
        for (Complaint c : overdue) {
            try {
                switch (process(c, now)) {
                    case ESCALATED -> escalated++;
                    case BREACHED -> breached++;
                    case NO_ACTION -> { }
                }
            } catch (RuntimeException e) {
                failed++;
                LOG.log(Level.WARNING, "Escalation failed for " + c.getTicketNo(), e);
            }
        }
        return new CycleResult(overdue.size(), escalated, breached, failed);
    }

    /** Overdue complaints that the next cycle would act on (for the admin dashboard). */
    public List<Complaint> overdueNow() {
        return complaints.findOverdue(LocalDateTime.now(clock));
    }

    private enum Outcome { ESCALATED, BREACHED, NO_ACTION }

    private Outcome process(Complaint c, LocalDateTime now) {
        int level = c.getEscalationLevel();
        if (level >= MAX_LEVEL) {
            complaints.markBreached(c.getId(), "SLA missed at final level " + Fmt.levelLabel(level) + " (due " + Fmt.dateTime(c.getSlaDueAt()) + ")");
            return Outcome.BREACHED;
        }
        int next = level + 1;
        List<Officer> candidates = officers.findActive(c.getDepartmentId(), next);
        Optional<Officer> target = routing.pick(c, candidates);
        if (target.isEmpty()) {
            LOG.warning("No active officer at level " + next + " for " + c.getTicketNo() + " (department " + c.getDepartmentId() + ")");
            return Outcome.NO_ACTION;
        }
        LocalDateTime due = now.plus(sla.windowFor(c.getPriority(), next));
        String reason = "SLA of " + Fmt.levelLabel(level) + " expired at " + Fmt.dateTime(c.getSlaDueAt())
                + "; escalated to " + target.get().getName();
        complaints.escalate(c.getId(), target.get().getId(), next, due, reason);
        return Outcome.ESCALATED;
    }
}
