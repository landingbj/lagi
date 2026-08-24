package ai.servlet;

import ai.account.CuihuaAccountService;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Alpha SSO landing endpoint used by Cuihua's system-administrator jump. */
public class SsoLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 7;
    private static final long SESSION_LIFETIME_MILLIS = COOKIE_MAX_AGE * 1000L;

    private final CuihuaAccountService accountService = new CuihuaAccountService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = req.getParameter("userId");
        try {
            CuihuaAccountService.LoginResult login = accountService.loginSystemAdminBySso(
                    userId, SESSION_LIFETIME_MILLIS);
            if (login == null) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "LinkMind SSO requires a logged-in system_admin account");
                return;
            }
            addCookie(resp, UserServlet.SESSION_COOKIE_NAME, login.getSessionToken(), true, req.isSecure());
            addCookie(resp, "userId", login.getUser().getUserId(), false, req.isSecure());
            String contextPath = req.getContextPath();
            String target = (contextPath == null || contextPath.isEmpty()) ? "/" : contextPath + "/";
            resp.setStatus(HttpServletResponse.SC_FOUND);
            resp.setHeader("Location", resp.encodeRedirectURL(target));
        } catch (SQLException e) {
            throw new ServletException("TokenLink SSO is unavailable", e);
        }
    }

    private void addCookie(HttpServletResponse resp, String name, String value, boolean httpOnly, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        resp.addCookie(cookie);
    }
}
