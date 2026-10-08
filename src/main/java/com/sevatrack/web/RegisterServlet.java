package com.sevatrack.web;

import com.sevatrack.model.Complaint;
import com.sevatrack.model.Priority;
import com.sevatrack.service.ComplaintForm;
import com.sevatrack.service.ValidationException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/register")
public class RegisterServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        show(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ComplaintForm f = new ComplaintForm();
        f.setCitizenName(req.getParameter("citizenName"));
        f.setCitizenPhone(req.getParameter("citizenPhone"));
        f.setCitizenEmail(req.getParameter("citizenEmail"));
        f.setTitle(req.getParameter("title"));
        f.setDescription(req.getParameter("description"));
        f.setCategoryId(intParam(req, "categoryId", 0));
        f.setWardId(intParam(req, "wardId", 0));
        f.setPriority(req.getParameter("priority"));
        try {
            Complaint c = app().complaints().register(f);
            resp.sendRedirect(req.getContextPath() + "/track?ticket=" + URLEncoder.encode(c.getTicketNo(), StandardCharsets.UTF_8) + "&new=1");
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("form", f);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            show(req, resp);
        }
    }

    private void show(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("categories", app().reference().categories());
        req.setAttribute("wards", app().reference().wards());
        req.setAttribute("departments", app().reference().departments());
        req.setAttribute("priorities", Priority.values());
        render(req, resp, "register");
    }
}
