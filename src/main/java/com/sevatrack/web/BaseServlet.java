package com.sevatrack.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

abstract class BaseServlet extends HttpServlet {

    protected AppContext app() {
        return AppContext.of(getServletContext());
    }

    protected void render(HttpServletRequest req, HttpServletResponse resp, String view) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute("flash") != null) {
            req.setAttribute("flash", s.getAttribute("flash"));
            s.removeAttribute("flash");
        }
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
    }

    protected void flash(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flash", message);
    }

    protected static int intParam(HttpServletRequest req, String name, int def) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    protected static long longParam(HttpServletRequest req, String name, long def) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) return def;
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
