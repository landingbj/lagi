package ai.servlet.api;

import ai.servlet.BaseServlet;
import ai.servlet.dto.VectorQueryRequest;
import ai.servlet.dto.VectorUpsertRequest;
import ai.vector.VectorStoreService;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.UpsertRecord;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        } else if (method.equals("upsert")) {
            this.upsert(req, resp);
        }
    }

    private void query(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        VectorQueryRequest vectorQueryRequest = reqBodyToObj(req, VectorQueryRequest.class);
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setN(vectorQueryRequest.getN());
        queryCondition.setText(vectorQueryRequest.getText());
        queryCondition.setWhere(vectorQueryRequest.getWhere());
        List<IndexRecord> recordList;
        if (vectorQueryRequest.getCategory() == null) {
            recordList = vectorStoreService.query(queryCondition);
        } else {
            recordList = vectorStoreService.query(queryCondition, vectorQueryRequest.getCategory());
        }
        Map<String, Object> result = new HashMap<>();
        if (recordList.isEmpty()) {
            result.put("status", "failed");
        } else {
            result.put("status", "success");
            result.put("data", recordList);
        }
        responsePrint(resp, toJson(result));
    }

    private void upsert(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        VectorUpsertRequest vectorUpsertRequest = reqBodyToObj(req, VectorUpsertRequest.class);
        List<UpsertRecord> upsertRecords = vectorUpsertRequest.getData();
        String category = vectorUpsertRequest.getCategory();
        boolean isContextLinked = vectorUpsertRequest.getContextLinked();
        vectorStoreService.upsertCustomVectors(upsertRecords, category, isContextLinked);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        responsePrint(resp, toJson(result));
    }
}
