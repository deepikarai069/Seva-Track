package com.sevatrack.web;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class EncodingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        if (resp instanceof HttpServletResponse h) {
            h.setHeader("X-Content-Type-Options", "nosniff");
            h.setHeader("X-Frame-Options", "SAMEORIGIN");
        }
        chain.doFilter(req, resp);
    }
}
