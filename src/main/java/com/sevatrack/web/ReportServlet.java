package com.sevatrack.web;

import com.sevatrack.service.ReportService.Report;
import com.sevatrack.service.ValidationException;
import com.sevatrack.util.ReportFormatter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** GET /reports?format=json|xml&from=2026-09-01&to=2026-09-30 (defaults: json, last 30 days). */
@WebServlet("/reports")
public class ReportServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String format = req.getParameter("format") == null ? "json" : req.getParameter("format").toLowerCase();
        if (!format.equals("json") && !format.equals("xml")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "format must be json or xml");
            return;
        }
        try {
            LocalDate to = parse(req.getParameter("to"), LocalDate.now(app().clock()));
            LocalDate from = parse(req.getParameter("from"), to.minusDays(29));
            Report report = app().reports().build(from, to);
            boolean json = format.equals("json");
            resp.setContentType(json ? "application/json;charset=UTF-8" : "application/xml;charset=UTF-8");
            resp.getWriter().write(json ? ReportFormatter.toJson(report) : ReportFormatter.toXml(report));
        } catch (ValidationException | DateTimeParseException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date range (use yyyy-MM-dd, from <= to)");
        }
    }

    private static LocalDate parse(String v, LocalDate def) {
        return v == null || v.isBlank() ? def : LocalDate.parse(v.trim());
    }
}
