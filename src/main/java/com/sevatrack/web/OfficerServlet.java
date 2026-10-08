package com.sevatrack.web;

import com.sevatrack.model.Officer;
import com.sevatrack.model.Status;
import com.sevatrack.service.BusinessException;
import com.sevatrack.service.NotFoundException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Officer work-queue. There is no login in this demo: you pick which officer you are. */
@WebServlet("/officer")
public class OfficerServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int officerId = intParam(req, "officerId", 0);
        if (officerId == 0) {
            req.setAttribute("officers", app().officers().findAllActive());
        } else {
            Officer o = app().officers().findById(officerId).orElse(null);
            if (o == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Officer not found");
                return;
            }
            req.setAttribute("officer", o);
            req.setAttribute("queue", app().complaints().forOfficer(officerId));
            req.setAttribute("statuses", Status.values());
        }
        render(req, resp, "officer");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int officerId = intParam(req, "officerId", 0);
        long complaintId = longParam(req, "complaintId", 0);
        try {
            Status target = Status.valueOf(String.valueOf(req.getParameter("status")));
            app().complaints().updateStatus(complaintId, officerId, target, req.getParameter("note"));
            flash(req, "Complaint updated to " + target.name().replace('_', ' ').toLowerCase() + ".");
        } catch (IllegalArgumentException e) {
            flash(req, "Unknown status.");
        } catch (BusinessException | NotFoundException e) {
            flash(req, e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/officer?officerId=" + officerId);
    }
}
