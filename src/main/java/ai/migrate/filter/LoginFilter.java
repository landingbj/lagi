package ai.migrate.filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class LoginFilter implements Filter {
    public void init(FilterConfig fConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("user")) {
                    if (session == null) {
                        session = req.getSession();
                        session.setAttribute("loggedIn", true);
                    }
                    chain.doFilter(request, response);
                    return;
                }
            }
        }
        if (session == null || session.getAttribute("loggedIn") == null) {
            res.sendRedirect("/");
        } else {
            chain.doFilter(request, response);
        }
    }

    public void destroy() {
    }
}
