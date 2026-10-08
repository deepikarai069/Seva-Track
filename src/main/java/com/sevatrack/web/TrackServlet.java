package com.sevatrack.web;

import com.sevatrack.model.Complaint;
import com.sevatrack.service.NotFoundException;
import com.sevatrack.service.ValidationException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/track")
public class TrackServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ticket = req.getParameter("ticket");
        req.setAttribute("ticket", ticket == null ? "" : ticket.trim());
        if (ticket != null && !ticket.isBlank()) {
            try {
                Complaint c = app().complaints().track(ticket);
                req.setAttribute("complaint", c);
                req.setAttribute("history", app().complaints().history(c.getId()));
                req.setAttribute("escalations", app().complaints().escalations(c.getId()));
                req.setAttribute("justRegistered", "1".equals(req.getParameter("new")));
            } catch (NotFoundException e) {
                req.setAttribute("error", e.getMessage());
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } catch (ValidationException e) {
                req.setAttribute("error", e.getMessage());
            }
        }
        render(req, resp, "track");
    }
}
