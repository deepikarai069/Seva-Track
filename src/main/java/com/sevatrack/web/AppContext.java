package com.sevatrack.web;

import com.sevatrack.dao.*;
import com.sevatrack.routing.RoutingEngine;
import com.sevatrack.service.ComplaintService;
import com.sevatrack.service.EscalationService;
import com.sevatrack.service.ReportService;
import com.sevatrack.service.SlaPolicy;
import com.sevatrack.util.ConnectionProvider;

import javax.servlet.ServletContext;
import java.time.Clock;

/** Hand-wired object graph (no DI framework): one instance per webapp, kept in the ServletContext. */
public final class AppContext {
    public static final String KEY = "sevatrack.app";

    private final ComplaintService complaints;
    private final EscalationService escalation;
    private final ReportService reports;
    private final ReferenceDao reference;
    private final OfficerDao officers;
    private final Clock clock;

    private AppContext(ComplaintService c, EscalationService e, ReportService r, ReferenceDao ref, OfficerDao o, Clock clock) {
        this.complaints = c;
        this.escalation = e;
        this.reports = r;
        this.reference = ref;
        this.officers = o;
        this.clock = clock;
    }

    public static AppContext create(ConnectionProvider cp, Clock clock) {
        ComplaintDao complaintDao = new JdbcComplaintDao(cp);
        OfficerDao officerDao = new JdbcOfficerDao(cp);
        ReferenceDao referenceDao = new JdbcReferenceDao(cp);
        HistoryDao historyDao = new JdbcHistoryDao(cp);
        SlaPolicy sla = new SlaPolicy(new JdbcSlaPolicyDao(cp));
        ComplaintService cs = new ComplaintService(complaintDao, referenceDao, officerDao, historyDao, sla,
                RoutingEngine.forAssignment(), clock);
        EscalationService es = new EscalationService(complaintDao, officerDao, sla, RoutingEngine.forEscalation(), clock);
        ReportService rs = new ReportService(new JdbcReportDao(cp), clock);
        return new AppContext(cs, es, rs, referenceDao, officerDao, clock);
    }

    public static AppContext of(ServletContext sc) {
        return (AppContext) sc.getAttribute(KEY);
    }

    public ComplaintService complaints() { return complaints; }
    public EscalationService escalation() { return escalation; }
    public ReportService reports() { return reports; }
    public ReferenceDao reference() { return reference; }
    public OfficerDao officers() { return officers; }
    public Clock clock() { return clock; }
}
