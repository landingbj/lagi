package ai.servlet.api;

import ai.bigdata.BigdataService;
import ai.common.pojo.IndexSearchData;
import ai.migrate.service.UploadFileService;
import ai.migrate.service.VectorDbService;
import ai.dto.VectorPreloadRequest;
import ai.response.VectorStatusResponse;
import ai.vector.VectorCache;
import ai.vector.VectorDbService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.servlet.BaseServlet;
import ai.servlet.dto.VectorDeleteRequest;
import ai.servlet.dto.VectorQueryRequest;
import ai.servlet.dto.VectorUpsertRequest;
import ai.vector.VectorStoreService;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.UpsertRecord;
import ai.vector.pojo.VectorCollection;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class VectorApiServlet extends BaseServlet {
    private final VectorStoreService vectorStoreService = new VectorStoreService();
    private final VectorDbService vectorDbService = new VectorDbService(null);
    private final BigdataService bigdataService = new BigdataService();
    private final UploadFileService uploadFileService = new UploadFileService();

    private final Map<String, AtomicInteger> preloadMap = new ConcurrentHashMap<>();

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
        } else if (method.equals("search")) {
            this.search(req, resp);
        } else if (method.equals("deleteById")) {
            this.deleteById(req, resp);
        } else if (method.equals("deleteByMetadata")) {
            this.deleteByMetadata(req, resp);
        } else if (method.equals("deleteCollection")) {
            this.deleteCollection(req, resp);
        } else if(method.equals("preload")) {
            this.preload(req, resp);
        } else if(method.equals("preloadStatus")) {
            this.preloadStatus(req, resp);
        } else if(method.equals("clearLoadStatus")) {
            this.clearLoadStatus(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("listCollections")) {
            this.listCollections(req, resp);
        }
    }

    private void search(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        ChatCompletionRequest request = reqBodyToObj(req, ChatCompletionRequest.class);
        List<IndexSearchData> indexSearchData = vectorDbService.searchByContext(request);
        Map<String, Object> result = new HashMap<>();
        if (indexSearchData == null || indexSearchData.isEmpty()) {
            result.put("status", "failed");
        } else {
            result.put("status", "success");
            result.put("data", indexSearchData);
        }
        responsePrint(resp, toJson(result));
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

    private void deleteById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        VectorDeleteRequest vectorDeleteRequest = reqBodyToObj(req, VectorDeleteRequest.class);
        String category = vectorDeleteRequest.getCategory();
        List<String> ids = vectorDeleteRequest.getIds();
        vectorStoreService.delete(ids, category);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        responsePrint(resp, toJson(result));
    }

    private void deleteByMetadata(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        VectorDeleteRequest vectorDeleteRequest = reqBodyToObj(req, VectorDeleteRequest.class);
        String category = vectorDeleteRequest.getCategory();
        List<Map<String, String>> whereList = vectorDeleteRequest.getWhereList();
        vectorStoreService.deleteWhere(whereList, category);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        responsePrint(resp, toJson(result));
    }

    private void deleteCollection(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        VectorDeleteRequest vectorDeleteRequest = reqBodyToObj(req, VectorDeleteRequest.class);
        String category = vectorDeleteRequest.getCategory();
        vectorStoreService.deleteCollection(category);
        uploadFileService.deleteUploadFile(category);
        bigdataService.delete(category);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        responsePrint(resp, toJson(result));
    }

    private void listCollections(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        List<VectorCollection> collections = vectorStoreService.listCollections();
        Map<String, Object> result = new HashMap<>();
        if (collections.isEmpty()) {
            result.put("status", "failed");
        } else {
            result.put("status", "success");
            result.put("data", collections);
        }
        responsePrint(resp, toJson(result));
    }

    private void preload(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        VectorPreloadRequest preloadRequest = reqBodyToObj(req, VectorPreloadRequest.class);
        String category = preloadRequest.getCategory();
        AtomicInteger atomicInteger = preloadMap.putIfAbsent(category, new AtomicInteger(0));
        if (atomicInteger == null) {
            doPreload(preloadMap.get(category), preloadRequest);
        }
        responsePrint(resp, toJson(VectorStatusResponse.builder().status(preloadMap.get(category).get()).build()));
    }

    private void doPreload(AtomicInteger atomicInteger, VectorPreloadRequest preloadRequest) {
        if(atomicInteger.get() != 0) {
            return;
        }
        synchronized (atomicInteger) {
            if(atomicInteger.get() != 0) {
                return;
            }
            new Thread(()->{
                atomicInteger.set(1);
                QueryCondition queryCondition = new QueryCondition();
                queryCondition.setWhere(new HashMap<>());
                List<IndexRecord> recordList = vectorStoreService.query(queryCondition, preloadRequest.getCategory());
                if(recordList != null) {
                    log.error("start preload category {} size : {}. ", preloadRequest.getCategory(), recordList.size());
                    VectorCache instance = VectorCache.getInstance();
                    recordList.stream().map(vectorStoreService::toIndexSearchData).forEach(indexSearchData -> {
                        if(atomicInteger.get() != 1) {
                            return;
                        }
                        instance.putVectorCache(indexSearchData.getId(), indexSearchData);
                        if(indexSearchData.getParentId() != null) {
                            instance.putVectorChildCache(indexSearchData.getParentId(), indexSearchData);
                        }
                    });
                    atomicInteger.set(2);
                    log.error("finished preload category {} . ", preloadRequest.getCategory());
                }
            }).start();
        }
    }


    private void preloadStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        VectorPreloadRequest preloadRequest = reqBodyToObj(req, VectorPreloadRequest.class);
        String category = preloadRequest.getCategory();
        AtomicInteger atomicInteger = preloadMap.getOrDefault(category, new AtomicInteger(0));
        responsePrint(resp, toJson(VectorStatusResponse.builder().status(atomicInteger.get()).build()));
    }

    private void clearLoadStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        VectorPreloadRequest preloadRequest = reqBodyToObj(req, VectorPreloadRequest.class);
        String category = preloadRequest.getCategory();
        AtomicInteger atomicInteger = preloadMap.get(category);
        atomicInteger.set(0);
        responsePrint(resp, toJson(VectorStatusResponse.builder().status(atomicInteger.get()).build()));
    }
}
