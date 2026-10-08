package com.sevatrack.web;

import com.sevatrack.service.SlaMonitor;
import com.sevatrack.util.ConnectionProvider;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(AppContextListener.class.getName());
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppContext app = AppContext.create(ConnectionProvider.fromEnv(), Clock.systemDefaultZone());
        sce.getServletContext().setAttribute(AppContext.KEY, app);

        long every = parse(System.getenv("SLA_CHECK_INTERVAL_SECONDS"), 60);
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sevatrack-sla-monitor");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(new SlaMonitor(app.escalation()), 15, every, TimeUnit.SECONDS);
        LOG.info("SevaTrack started; SLA monitor runs every " + every + "s");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) scheduler.shutdownNow();
    }

    private static long parse(String v, long def) {
        try {
            return v == null ? def : Math.max(5, Long.parseLong(v.trim()));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
