package ai.servlet.api;

import ai.servlet.BaseServlet;
import ai.servlet.dto.VectorQueryRequest;
import ai.vector.VectorStoreService;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.QueryCondition;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class VectorApiServlet extends BaseServlet {
    private final VectorStoreService vectorStoreService = new VectorStoreService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("query")) {
            this.query(req, resp);
        }
    }

    private void query(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        VectorQueryRequest vectorQueryRequest = reqBodyToObj(req, VectorQueryRequest.class);
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setN(vectorQueryRequest.getN());
        queryCondition.setText(vectorQueryRequest.getText());
        queryCondition.setWhere(vectorQueryRequest.getWhere());
        List<IndexRecord> result;
        if (vectorQueryRequest.getCategory() == null) {
            result = vectorStoreService.query(queryCondition);
        } else {
            result = vectorStoreService.query(queryCondition, vectorQueryRequest.getCategory());
        }
        responsePrint(resp, toJson(result));
    }
}
