package com.sevatrack.web;

import com.sevatrack.service.EscalationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin")
public class AdminServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Report is a record; Tomcat 9's EL cannot read record accessors, so expose plain lists.
        var report = app().reports().lastDays(30);
        req.setAttribute("departments", report.departments());
        req.setAttribute("overdue", app().escalation().overdueNow());
        req.setAttribute("recent", app().complaints().recent(20));
        req.setAttribute("officers", app().officers().findAllActive());
        render(req, resp, "admin");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("run-sla".equals(req.getParameter("action"))) {
            EscalationService.CycleResult r = app().escalation().runCycle();
            flash(req, "SLA check finished: " + r.summary() + ".");
        }
        resp.sendRedirect(req.getContextPath() + "/admin");
    }
}
