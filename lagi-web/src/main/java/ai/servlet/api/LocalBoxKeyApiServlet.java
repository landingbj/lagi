package ai.servlet.api;

import ai.config.ContextLoader;
import ai.config.pojo.LocalKeyConfig;
import ai.llm.dao.TokenStatisticsDao;
import ai.llm.pojo.TokenStatisticsRange;
import ai.localkey.LocalBoxKey;
import ai.localkey.LocalBoxKeyService;
import ai.servlet.BaseServlet;
import lombok.Data;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local-only management surface for box-issued lmk_ keys. It deliberately has
 * no Cuihua/SSO dependency: an administrator supplies a subject id and an
 * optional display name when issuing a key.
 */
public class LocalBoxKeyApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final LocalBoxKeyService keyService = new LocalBoxKeyService();
    private final TokenStatisticsDao statisticsDao = new TokenStatisticsDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!authorize(req, resp)) {
            return;
        }
        String action = action(req);
        if ("usage".equals(action)) {
            writeUsage(req, resp);
        } else {
            writeList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!authorize(req, resp)) {
            return;
        }
        String action = action(req);
        try {
            if ("create".equals(action)) {
                CreateRequest body = reqBodyToObj(req, CreateRequest.class);
                LocalBoxKey created = keyService.create(body == null ? null : body.subjectId,
                        body == null ? null : body.displayName, body == null ? null : body.remark);
                writeJson(resp, created);
            } else if ("revoke".equals(action)) {
                IdRequest body = reqBodyToObj(req, IdRequest.class);
                if (body == null || body.id == null || !keyService.revoke(body.id)) {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "local key was not found or is already revoked");
                    return;
                }
                writeJson(resp, status("revoked"));
            } else if ("remark".equals(action)) {
                RemarkRequest body = reqBodyToObj(req, RemarkRequest.class);
                if (body == null || body.id == null || !keyService.updateRemark(body.id, body.remark)) {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "local key was not found");
                    return;
                }
                writeJson(resp, status("updated"));
            } else {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "unknown local key action");
            }
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log("local key administration failed", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "local key storage is unavailable");
        }
    }

    private void writeList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<LocalBoxKey> keys = keyService.list(req.getParameter("subjectId"));
            writeJson(resp, keys);
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "local key storage is unavailable");
        }
    }

    private void writeUsage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String subjectId = req.getParameter("subjectId");
        if (subjectId == null || subjectId.trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "subjectId is required");
            return;
        }
        TokenStatisticsRange range = TokenStatisticsRange.fromQueryParam(req.getParameter("range"));
        writeJson(resp, statisticsDao.summarize(range, subjectId.trim()));
    }

    private boolean authorize(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LocalKeyConfig config = localKeyConfig();
        if (config.getEnable() != null && !config.getEnable()) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "local key service is disabled");
            return false;
        }
        String adminKey = config.getAdminKey();
        if (adminKey == null || adminKey.trim().isEmpty()) {
            String address = req.getRemoteAddr();
            if ("127.0.0.1".equals(address) || "::1".equals(address) || "0:0:0:0:0:0:0:1".equals(address)) {
                return true;
            }
            writeError(resp, HttpServletResponse.SC_FORBIDDEN,
                    "local key management requires general.local_key.admin_key outside loopback");
            return false;
        }
        String supplied = req.getHeader("X-LinkMind-Admin-Key");
        if (supplied != null && MessageDigest.isEqual(adminKey.trim().getBytes(StandardCharsets.UTF_8),
                supplied.trim().getBytes(StandardCharsets.UTF_8))) {
            return true;
        }
        writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "invalid local key management credential");
        return false;
    }

    private LocalKeyConfig localKeyConfig() {
        if (ContextLoader.configuration == null || ContextLoader.configuration.getGeneral() == null
                || ContextLoader.configuration.getGeneral().getLocalKey() == null) {
            return new LocalKeyConfig();
        }
        return ContextLoader.configuration.getGeneral().getLocalKey();
    }

    private String action(HttpServletRequest req) {
        String uri = req.getRequestURI();
        if (uri == null || uri.endsWith("/local-keys")) {
            return "list";
        }
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    private Map<String, String> status(String status) {
        Map<String, String> value = new LinkedHashMap<String, String>();
        value.put("status", status);
        return value;
    }

    private void writeJson(HttpServletResponse resp, Object body) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter writer = resp.getWriter();
        writer.print(gson.toJson(body));
        writer.flush();
    }

    @Override
    protected void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        Map<String, String> body = new LinkedHashMap<String, String>();
        body.put("error", message);
        writeJson(resp, body);
    }

    @Data
    private static class CreateRequest {
        private String subjectId;
        private String displayName;
        private String remark;
    }

    @Data
    private static class IdRequest {
        private Long id;
    }

    @Data
    private static class RemarkRequest {
        private Long id;
        private String remark;
    }
}
