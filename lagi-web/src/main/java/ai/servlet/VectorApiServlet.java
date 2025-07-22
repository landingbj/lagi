//package ai.servlet;
//
//
//import ai.servlet.dto.UserRagConfig;
//import ai.servlet.dto.VectorQueryRequest;
//import ai.sevice.UserRagConfigService;
//import ai.vector.VectorStoreService;
//import ai.vector.pojo.IndexRecord;
//import ai.vector.pojo.QueryCondition;
//import com.google.gson.Gson;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static ai.utils.GsonUtils.toJson;
//
//public class VectorApiServlet {
//    private final VectorStoreService vectorStoreService;
//    private final UserRagConfigService userRagConfigService;
//
//    public VectorApiServlet(VectorStoreService vectorStoreService, UserRagConfigService userRagConfigService) {
//        this.vectorStoreService = vectorStoreService;
//        this.userRagConfigService = userRagConfigService;
//    }
//
//    private void query(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        resp.setContentType("application/json;charset=utf-8");
//        VectorQueryRequest vectorQueryRequest = reqBodyToObj(req, VectorQueryRequest.class);
//
//        // 获取用户配置
//        String userId = vectorQueryRequest.getUserId();
//        String category = vectorQueryRequest.getCategory();
//        UserRagConfig config = userRagConfigService.getConfig(userId, category);
//
//        // 构造查询条件
//        QueryCondition queryCondition = new QueryCondition();
//        queryCondition.setN(vectorQueryRequest.getN() != null ? vectorQueryRequest.getN() : config.getSimilarityTopK());
//        queryCondition.setText(vectorQueryRequest.getText());
//        queryCondition.setWhere(vectorQueryRequest.getWhere());
//        queryCondition.setSimilarityCutoff(config.getSimilarityCutoff());
//
//        List<IndexRecord> recordList;
//        if (category == null) {
//            recordList = vectorStoreService.query(queryCondition);
//        } else {
//            recordList = vectorStoreService.query(queryCondition, category);
//        }
//
//        Map<String, Object> result = new HashMap<>();
//        if (recordList.isEmpty()) {
//            result.put("status", "failed");
//        } else {
//            result.put("status", "success");
//            result.put("data", recordList);
//        }
//        responsePrint(resp, toJson(result));
//    }
//
//    private <T> T reqBodyToObj(HttpServletRequest req, Class<T> clazz) throws IOException {
//        BufferedReader reader = req.getReader();
//        StringBuilder jsonBuilder = new StringBuilder();
//        String line;
//        while ((line = reader.readLine()) != null) {
//            jsonBuilder.append(line);
//        }
//        return new Gson().fromJson(jsonBuilder.toString(), clazz);
//    }
//    protected void responsePrint(HttpServletResponse resp, String ret) throws IOException {
//        PrintWriter out = resp.getWriter();
//        out.print(ret);
//        out.flush();
//        out.close();
//    }
//}
