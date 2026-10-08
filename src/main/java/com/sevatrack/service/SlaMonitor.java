package com.sevatrack.service;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Runnable for a ScheduledExecutorService. Never throws, otherwise the schedule would be cancelled. */
public class SlaMonitor implements Runnable {
    private static final Logger LOG = Logger.getLogger(SlaMonitor.class.getName());
    private final EscalationService service;

    public SlaMonitor(EscalationService service) {
        this.service = service;
    }

    @Override
    public void run() {
        try {
            EscalationService.CycleResult r = service.runCycle();
            if (r.checked() > 0) LOG.info("SLA cycle: " + r.summary());
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, "SLA monitor cycle crashed", t);
        }
    }
}
