package ai.vector;

import ai.bigdata.BigdataService;
import ai.bigdata.pojo.TextIndexData;
import ai.common.pojo.*;
import ai.common.utils.ThreadPoolManager;
import ai.intent.IntentService;
import ai.intent.enums.IntentStatusEnum;
import ai.intent.impl.SampleIntentServiceImpl;
import ai.intent.pojo.IntentResult;
import ai.learn.questionAnswer.KShingleFilter;
import ai.manager.VectorStoreManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatMessage;
import ai.utils.LagiGlobal;
import ai.utils.StoppingWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.impl.BaseVectorStore;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.UpsertRecord;
import ai.vector.pojo.VectorCollection;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class VectorStoreService {
    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);
    private final Gson gson = new Gson();
    private BaseVectorStore vectorStore;
    private final FileService fileService = new FileService();
    private static final ExecutorService executor;

    static {
        ThreadPoolManager.registerExecutor("vector-service", new ThreadPoolExecutor(30, 100, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                (r, executor) -> {
                    log.error(StrUtil.format("线程池队({})任务过多请求被拒绝", "vector-service"));
                }
        ));
        executor = ThreadPoolManager.getExecutor("vector-service");
    }

    private final IntentService intentService = new SampleIntentServiceImpl();
    private final BigdataService bigdataService = new BigdataService();
    private static final VectorCache vectorCache = VectorCache.getInstance();

    private static final double threshold = 0.01d;
    private static final double frequencyThreshold = 0;
    private static final KShingleFilter kShingleFilter = new KShingleFilter(2, threshold, frequencyThreshold);

    public VectorStoreService() {
        if (LagiGlobal.RAG_ENABLE) {
            this.vectorStore = (BaseVectorStore) VectorStoreManager.getInstance().getAdapter();
        }
    }

    public VectorStoreConfig getVectorStoreConfig() {
        if (vectorStore == null) {
            return null;
        }
        return vectorStore.getConfig();
    }

    public void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException {
        List<FileChunkResponse.Document> docs;
        FileChunkResponse response = null;
                //fileService.extractContent(file);
        if (response != null && response.getStatus().equals("success")) {
            docs = response.getData();
        } else {
            docs = fileService.splitChunks(file, 512);
        }
        List<FileInfo> fileList = new ArrayList<>();
        Map<String, String> fileTileMap = new HashMap<>();

        fileTileMap.put("承租人所需资料清单及示例","承租人所需资料清单及示例");
        fileTileMap.put("深圳大学职工政策性住房信息申报表","深圳大学职工政策性住房信息申报表");
        fileTileMap.put("政策性住房信息调查表（申请人原单位）","政策性住房信息调查表（申请人原单位）");
        fileTileMap.put("政策性住房信息调查表（配偶单位）","政策性住房信息调查表（配偶单位）");
        fileTileMap.put("个人诚信申报信息承诺书（政策性住房信息申报）","个人诚信申报信息承诺书（政策性住房信息申报）");
        fileTileMap.put("轮候到周转房时需提交的材料清单","轮候到周转房时需提交的材料清单");
        fileTileMap.put("深圳大学周转房入住申请表","深圳大学周转房入住申请表");
        fileTileMap.put("未婚声明","未婚声明");
        fileTileMap.put("后勤保障部开展暑期重点项目及安全生产工作检查","后勤保障部开展暑期重点项目及安全生产工作检查");
        fileTileMap.put("市场监管局南山局党组书记、局长崔红兵一行到我校调研餐饮保障工作","市场监管局南山局党组书记、局长崔红兵一行到我校调研餐饮保障工作");

        fileTileMap.put("（中电信京〔2024〕74号）《关于印发中国电信北京公司领导人员请假休假管理办法的通知》","关于印发中国电信北京公司领导人员请假休假管理办法的通知");
        fileTileMap.put("（中电信京〔2024〕112号）《关于印发中国电信北京公司员工培养锻炼实施办法及2024年培养锻炼实施方案的通知》", "关于印发中国电信北京公司员工培养锻炼实施办法及2024年培养锻炼实施方案的通知");
        fileTileMap.put("72.关于印发《中国电信北京公司领导人员转任非领导职务管理办法（试行）》的通知", "关于印发《中国电信北京公司领导人员转任非领导职务管理办法（试行）》的通知");
        fileTileMap.put("73.党委联系专家制度-中电信京党委〔2020〕17 号", "关于建立党委成员联系服务专家制度的通知");
        fileTileMap.put("关于规范北京市电信公司员工着装及仪容、仪表的通知", "关于规范北京市电信公司员工着装及仪容、仪表的通知");
        fileTileMap.put("关于进一步加强企业生产经营纪律的补充通知(中电信京人力[2013]15号)", "关于进一步加强企业生产经营纪律的补充通知");
        fileTileMap.put("关于进一步加强生产经营纪律的通知", "关于进一步加强生产经营纪律的通知");
        fileTileMap.put("关于完善领导人员管理相关事宜的通知（中电信京〔2019〕248号）", "关于完善领导人员管理相关事宜的通知");
        fileTileMap.put("关于修订中国电信北京公司困难员工帮扶资金管理办法（暂行）的通知", "关于修订中国电信北京公司困难员工帮扶资金管理办法（暂行）的通知");
        fileTileMap.put("关于印发《中国电信北京公司部门骨干人才选拔使用程序指导意见》的通知 (1)", "关于印发中国电信北京公司部门骨干人才选拔使用程序指导意见的通知");
        fileTileMap.put("关于印发《中国电信北京公司部门骨干人才选拔使用程序指导意见》的通知", "关于印发中国电信北京公司部门骨干人才选拔使用程序指导意见的通知");
        fileTileMap.put("关于印发《中国电信北京公司党委关于领导人员管理的规定》的通知 中电信京党委〔2023〕16号", "关于印发中国电信北京公司党委关于领导人员管理规定的通知");
        fileTileMap.put("关于印发《中国电信北京公司高潜质人才管理办法（试行）》的通知", "关于印发中国电信北京公司高潜质人才管理办法（试行）的通知");
        fileTileMap.put("关于印发《中国电信北京公司关于员工违反劳动合同法第三十九条予以解除劳动合同的实施细则》的通知", "关于印发《中国电信北京公司关于员工违反劳动合同法第三十九条予以解除劳动合同的实施细则》的通知");
        fileTileMap.put("关于印发《中国电信北京公司领导人员轮岗交流管理办法（试行）》的通知", "关于印发中国电信北京公司领导人员轮岗交流管理办法（试行）的通知");
        fileTileMap.put("关于印发《中国电信股份有限公司北京分公司员工纪律惩戒管理办法》的通知中电信京〔2021〕40号", "关于印发中国电信股份有限公司北京分公司员工纪律惩戒管理办法的通知");
        fileTileMap.put("关于印发北京公司党委与领导人员谈心谈话办法的通知（中电信京党委〔2020〕75号）", "关于印发北京公司党委与领导人员谈心谈话办法的通知");
        fileTileMap.put("关于印发北京公司专家专业工作考核实施办法的通知（试行）中电信京〔2021〕106号", "关于印发北京公司专家专业工作考核实施办法（试行）的通知");
        fileTileMap.put("关于印发中国电信北京公司干部人事档案管理办法的通知", "关于印发中国电信北京公司干部人事档案管理办法的通知");
        fileTileMap.put("关于印发中国电信北京公司高层次专业人才管理服务实施办法的通知中电信京〔2019〕113号", "关于印发中国电信北京公司高层次专业人才管理服务实施办法的通知");
        fileTileMap.put("关于印发中国电信北京公司人才云平台运营管理办法的通知中电信京〔2021〕102号", "关于印发中国电信北京公司人才云平台运营管理办法的通知");
        fileTileMap.put("关于印发中国电信北京公司员工胜任力管理办法（试行）的通知中电信京〔2019〕82号", "关于印发中国电信北京公司员工胜任力管理办法（试行）的通知");
        fileTileMap.put("关于印发中国电信北京公司员工职业发展管理办法（2021版）的通知中电信京〔2021〕110号", "关于印发中国电信北京公司员工职业发展管理办法（2021 版）的通知");
        fileTileMap.put("关于印发中国电信北京公司专业职务聘任管理办法的通知", "关于印发中国电信北京公司专业职务聘任管理办法的通知");
        fileTileMap.put("关于印发中国电信股份有限公司北京分公司员工考勤及请假休假管理办法的通知 中电信京〔2021〕92号","关于印发中国电信股份有限公司北京分公司员工考勤及请假休假理办法的通知");
        fileTileMap.put("关于中国电信北京公司领导人员转任非领导职务相关管理工作的通知", "关于中国电信北京公司领导人员转任非领导职务相关管理工作的通知");
        fileTileMap.put("中电信京〔2021〕111号", "关于印发中国电信北京公司岗位管理办法（2021 版）的通知");
        fileTileMap.put("中电信京〔2022〕170号", "关于完善领导人员管理相关事宜的通知");
        fileTileMap.put("中电信京〔2023〕40号", "关于印发中国电信北京公司领导人员转任非领导职务管理办法的通知");
        fileTileMap.put("中电信京〔2023〕41号", "关于印发中国电信北京公司总监制实施办法的通知");
        fileTileMap.put("中电信京〔2023〕51号", "关于印发中国电信北京公司员工职业发展管理办法（2023 版）的通知");
        fileTileMap.put("中电信京〔2023〕132号", "关于印发中国电信北京公司高层次专业人才管理服务办法的通知");
        fileTileMap.put("中电信京人力[2015]2号","关于印发《中国电信股份有限公司北京分公司员工退休管理办法》的通知");
        String ttt=(String) metadatas.get("filename");
        int i = ttt.lastIndexOf(".");
        String substring = ttt.substring(0, i);

        String title = fileTileMap.get(substring);
        System.out.println(substring);
        System.out.println(title);
        FileInfo fi = new FileInfo();
        String e = UUID.randomUUID().toString().replace("-", "");
        fi.setEmbedding_id(e);
        fi.setText(title);
        Map<String, Object> t = new HashMap<>(metadatas);
        fi.setMetadatas(t);
        fileList.add(fi);

        for (FileChunkResponse.Document doc : docs) {
            FileInfo fileInfo = new FileInfo();
            String embeddingId = UUID.randomUUID().toString().replace("-", "");
            fileInfo.setEmbedding_id(embeddingId);
            fileInfo.setText(doc.getText());
            Map<String, Object> tmpMetadatas = new HashMap<>(metadatas);
            if (doc.getImages() != null) {
                tmpMetadatas.put("image", gson.toJson(doc.getImages()));
            }
            fileInfo.setMetadatas(tmpMetadatas);
            fileList.add(fileInfo);
        }
        upsertFileVectors(fileList, category);
    }

    public void upsertCustomVectors(List<UpsertRecord> upsertRecords, String category) {
        this.upsertCustomVectors(upsertRecords, category, false);
    }

    public void upsertCustomVectors(List<UpsertRecord> upsertRecords, String category, boolean isContextLinked) {
        for (UpsertRecord upsertRecord : upsertRecords) {
            String embeddingId = UUID.randomUUID().toString().replace("-", "");
            upsertRecord.setId(embeddingId);
        }
        if (isContextLinked) {
            for (int i = 1; i < upsertRecords.size(); i++) {
                String parentId = upsertRecords.get(i - 1).getId();
                upsertRecords.get(i).getMetadata().put("parent_id", parentId);
            }
        }
        this.upsert(upsertRecords, category);
    }

    private void upsertFileVectors(List<FileInfo> fileList, String category) throws IOException {
        List<UpsertRecord> upsertRecords = new ArrayList<>();
        for (FileInfo fileInfo : fileList) {
            upsertRecords.add(convertToUpsertRecord(fileInfo));
        }
        for (int i = 1; i < upsertRecords.size(); i++) {
            String parentId = upsertRecords.get(i - 1).getId();
            upsertRecords.get(i).getMetadata().put("parent_id", parentId);
        }
        this.upsert(upsertRecords, category);
    }

    private UpsertRecord convertToUpsertRecord(FileInfo fileInfo) {
        UpsertRecord upsertRecord = new UpsertRecord();
        upsertRecord.setDocument(fileInfo.getText());
        upsertRecord.setId(fileInfo.getEmbedding_id());

        Map<String, String> metadata = new HashMap<>();
        for (Map.Entry<String, Object> entry : fileInfo.getMetadatas().entrySet()) {
            metadata.put(entry.getKey(), entry.getValue().toString());
        }
        upsertRecord.setMetadata(metadata);

        return upsertRecord;
    }

    public void upsert(List<UpsertRecord> upsertRecords) {
        this.upsert(upsertRecords, vectorStore.getConfig().getDefaultCategory());
    }

    public void upsert(List<UpsertRecord> upsertRecords, String category) {
        upsertRecords = upsertRecords.stream()
            .filter(record -> record.getDocument() != null)
            .collect(Collectors.toList());
        for (UpsertRecord upsertRecord : upsertRecords) {
            TextIndexData data = new TextIndexData();
            data.setId(upsertRecord.getId());
            data.setText(upsertRecord.getDocument());
            data.setCategory(category);
            bigdataService.upsert(data);
        }
        this.vectorStore.upsert(upsertRecords, category);
    }

    public List<IndexRecord> query(QueryCondition queryCondition) {
        return this.vectorStore.query(queryCondition);
    }

    public List<IndexRecord> query(QueryCondition queryCondition, String category) {
        return this.vectorStore.query(queryCondition, category);
    }

    public List<IndexRecord> fetch(List<String> ids) {
        return this.vectorStore.fetch(ids);
    }

    public List<IndexRecord> fetch(List<String> ids, String category) {
        return this.vectorStore.fetch(ids, category);
    }

    public IndexRecord fetch(String id) {
        List<String> ids = Collections.singletonList(id);
        List<IndexRecord> indexRecords = this.vectorStore.fetch(ids);
        IndexRecord result = null;
        if (indexRecords.size() == 1) {
            result = indexRecords.get(0);
        }
        return result;
    }

    public IndexRecord fetch(String id, String category) {
        List<String> ids = Collections.singletonList(id);
        List<IndexRecord> indexRecords = this.vectorStore.fetch(ids, category);
        IndexRecord result = null;
        if (indexRecords.size() == 1) {
            result = indexRecords.get(0);
        }
        return result;
    }

    public List<IndexRecord> fetch(Map<String, String> where) {
        return this.vectorStore.fetch(where);
    }

    public void delete(List<String> ids) {
        this.vectorStore.delete(ids);
    }

    public void delete(List<String> ids, String category) {
        this.vectorStore.delete(ids, category);
    }

    public void deleteWhere(List<Map<String, String>> whereList) {
        this.vectorStore.deleteWhere(whereList);
    }

    public void deleteWhere(List<Map<String, String>> whereList, String category) {
        this.vectorStore.deleteWhere(whereList, category);
    }

    public void deleteCollection(String category) {
        this.vectorStore.deleteCollection(category);
    }

    public List<IndexSearchData> search(ChatCompletionRequest request) {
        String lastMessage = ChatCompletionUtil.getLastMessage(request);
        List<IndexSearchData> indexSearchDataList = search(lastMessage, request.getCategory());
        return indexSearchDataList;
    }

    public List<IndexSearchData> searchByContext(ChatCompletionRequest request) {
        List<ChatMessage> messages = request.getMessages();
        IntentResult intentResult = intentService.detectIntent(request);
        if (intentResult.getIndexSearchDataList() != null) {
            return intentResult.getIndexSearchDataList();
        }
        String question = null;
        if (intentResult.getStatus() != null && intentResult.getStatus().equals(IntentStatusEnum.CONTINUE.getName())) {
            if (intentResult.getContinuedIndex() != null) {
                ChatMessage chatMessage = messages.get(intentResult.getContinuedIndex());
                String content = chatMessage.getContent();
                String[] split = content.split("[， ,.。！!?？]");
                String source = Arrays.stream(split).filter(StoppingWordUtil::containsStoppingWorlds).findAny().orElse("");
                if (StrUtil.isBlank(source)) {
                    source = content;
                }
                if (chatMessage.getRole().equals(LagiGlobal.LLM_ROLE_SYSTEM)) {
                    source = "";
                }
                question = source + ChatCompletionUtil.getLastMessage(request);
            } else {
                List<ChatMessage> userMessages = messages.stream().filter(m -> m.getRole().equals("user")).collect(Collectors.toList());
                if (userMessages.size() > 1) {
                    question = userMessages.get(userMessages.size() - 2).getContent().trim();
                }
            }
        }
        if (question == null) {
            question = ChatCompletionUtil.getLastMessage(request);
        }
        if (request instanceof EnhanceChatCompletionRequest) {
            EnhanceChatCompletionRequest enhanceRequest = (EnhanceChatCompletionRequest) request;
            String userId = enhanceRequest.getUserId();
            String identity = enhanceRequest.getIdentity();
            if ("leader".equals(identity)) {
                question = adjustQuestionForLeader(question);
            } else if ("personnel".equals(identity)) {
                question = adjustQuestionForPersonnel(question);
            }
        }
        return search(question, request.getCategory());
    }

    private String adjustQuestionForLeader(String originalQuestion) {
        if (originalQuestion.contains("请假")) {
            return "作为领导，您的请假流程是什么？" + originalQuestion;
        } else if (originalQuestion.contains("出差")) {
            return "作为领导，您的出差流程是什么？" + originalQuestion;
        } else if (originalQuestion.contains("加班")) {
            return "作为领导，如何安排团队成员加班？" + originalQuestion;
        } else if (originalQuestion.contains("晋升")) {
            return "作为领导，您如何评估晋升候选人？" + originalQuestion;
        } else if (originalQuestion.contains("薪酬") || originalQuestion.contains("薪水")) {
            return "作为领导，您如何调整团队成员薪酬？" + originalQuestion;
        } else if (originalQuestion.contains("培训")) {
            return "作为领导，如何审批培训申请？" + originalQuestion;
        }
        return "作为领导：" + originalQuestion;
    }

    private String adjustQuestionForPersonnel(String originalQuestion) {
        if (originalQuestion.contains("年假")) {
            return "作为普通员工，您的年假请假流程是什么？" + originalQuestion;
        } else if (originalQuestion.contains("病假")) {
            return "作为普通员工，您的病假请假流程是什么？" + originalQuestion;
        } else if (originalQuestion.contains("请假")) {
            return "作为普通员工，您的请假流程是什么？" + originalQuestion;
        } else if (originalQuestion.contains("出差")) {
            return "作为普通员工，您的出差申请流程是什么？" + originalQuestion;
        } else if (originalQuestion.contains("加班")) {
            return "作为普通员工，如何申请加班？" + originalQuestion;
        } else if (originalQuestion.contains("晋升")) {
            return "作为普通员工，如何申请晋升？" + originalQuestion;
        } else if (originalQuestion.contains("薪酬") || originalQuestion.contains("薪水")) {
            return "作为普通员工，如何申请薪酬调整？" + originalQuestion;
        } else if (originalQuestion.contains("培训")) {
            return "作为普通员工，如何申请参加培训？" + originalQuestion;
        }
        return "作为普通员工：" + originalQuestion;
    }



    public List<IndexSearchData> search(String question, String category) {
        question = question.replace(" \n", "");
        int similarity_top_k = vectorStore.getConfig().getSimilarityTopK();
        double similarity_cutoff = vectorStore.getConfig().getSimilarityCutoff();
        Map<String, String> where = new HashMap<>();
        category = ObjectUtils.defaultIfNull(category, vectorStore.getConfig().getDefaultCategory());
        List<IndexSearchData> indexSearchDataList = search(question, similarity_top_k, similarity_cutoff, where, category);
        Set<String> esIds = bigdataService.getIds(question, category);
        if (esIds != null && !esIds.isEmpty()) {
            Set<String> indexIds = indexSearchDataList.stream().map(IndexSearchData::getId).collect(Collectors.toSet());
            indexIds.retainAll(esIds);
            indexSearchDataList = indexSearchDataList.stream()
                    .filter(indexSearchData -> indexIds.contains(indexSearchData.getId()))
                    .collect(Collectors.toList());
        }
        String finalCategory = category;
        List<Future<IndexSearchData>> futureResultList = indexSearchDataList.stream()
                .map(indexSearchData -> executor.submit(() -> extendIndexSearchData(indexSearchData, finalCategory)))
                .collect(Collectors.toList());
        return futureResultList.stream().map(indexSearchDataFuture -> {
            try {
                return indexSearchDataFuture.get();
            } catch (Exception e) {
                log.error("indexData get error", e);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

//    private IndexSearchData extendIndexSearchData(IndexSearchData indexSearchData, String category) {
//        IndexSearchData extendedIndexSearchData = vectorCache.getFromVectorLinkCache(indexSearchData.getId());
//        if (extendedIndexSearchData == null) {
//            extendedIndexSearchData = extendText(indexSearchData, category);
//            vectorCache.putToVectorLinkCache(indexSearchData.getId(), extendedIndexSearchData);
//        }
//        extendedIndexSearchData.setDistance(indexSearchData.getDistance());
//        return extendedIndexSearchData;
//    }

    private IndexSearchData extendIndexSearchData(IndexSearchData indexSearchData, String category) {
        return extendText(indexSearchData, category);
    }

    public List<IndexSearchData> searchByIds(List<String> ids, String category) {
        List<IndexRecord> fetch = fetch(ids, category);
        if (fetch == null) {
            return Collections.emptyList();
        }
        List<IndexSearchData> indexSearchDataList = fetch.stream().map(this::toIndexSearchData).collect(Collectors.toList());
        List<Future<IndexSearchData>> futureResultList = indexSearchDataList.stream()
                .map(indexSearchData -> executor.submit(() -> extendIndexSearchData(indexSearchData, category)))
                .collect(Collectors.toList());
        return futureResultList.stream().map(indexSearchDataFuture -> {
            try {
                return indexSearchDataFuture.get();
            } catch (Exception e) {
                log.error("indexData get error", e);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<IndexSearchData> search(String question, int similarity_top_k, double similarity_cutoff,
                                        Map<String, String> where, String category) {
        List<IndexSearchData> result = new ArrayList<>();
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setText(question);
        queryCondition.setN(Math.max(similarity_top_k, 500));
        queryCondition.setWhere(where);
        List<IndexRecord> indexRecords = this.query(queryCondition, category);
        result = indexRecords.stream().filter(indexRecord -> indexRecord.getDistance() <= similarity_cutoff).limit(similarity_top_k).map(this::toIndexSearchData).collect(Collectors.toList());
        return result;
    }

    public IndexSearchData toIndexSearchData(IndexRecord indexRecord) {
        if (indexRecord == null) {
            return null;
        }
        IndexSearchData indexSearchData = new IndexSearchData();
        indexSearchData.setId(indexRecord.getId());
        indexSearchData.setText(indexRecord.getDocument());
        indexSearchData.setCategory((String) indexRecord.getMetadata().get("category"));
        indexSearchData.setLevel((String) indexRecord.getMetadata().get("level"));
        indexSearchData.setFileId((String) indexRecord.getMetadata().get("file_id"));
        String filename = (String) indexRecord.getMetadata().get("filename");
        Long seq = indexRecord.getMetadata().get("seq") == null ? 0L : Long.parseLong((String) indexRecord.getMetadata().get("seq"));
        indexSearchData.setSeq(seq);
        if (filename != null) {
            indexSearchData.setFilename(Collections.singletonList(filename));
        }
        if (indexRecord.getMetadata().get("filepath") != null) {
            indexSearchData.setFilepath(Collections.singletonList((String) indexRecord.getMetadata().get("filepath")));
        }
        indexSearchData.setImage((String) indexRecord.getMetadata().get("image"));
        indexSearchData.setDistance(indexRecord.getDistance());
        indexSearchData.setParentId((String) indexRecord.getMetadata().get("parent_id"));
        return indexSearchData;
    }

    public IndexSearchData getParentIndex(String parentId) {
        return getParentIndex(parentId, vectorStore.getConfig().getDefaultCategory());
    }

    public IndexSearchData getParentIndex(String parentId, String category) {
        if (parentId == null) {
            return null;
        }
        IndexSearchData cache = vectorCache.getVectorCache(parentId);
        if (cache != null) {
            return cache;
        }
        IndexSearchData indexSearchData = toIndexSearchData(this.fetch(parentId, category));
        vectorCache.putVectorCache(parentId, indexSearchData);
        return indexSearchData;
    }

    public IndexSearchData getChildIndex(String parentId, String category) {
        IndexSearchData result = null;
        if (parentId == null) {
            return null;
        }
        IndexSearchData cache = vectorCache.getVectorChildCache(parentId);
        if (cache != null) {
            return cache;
        }
        Map<String, String> where = new HashMap<>();
        where.put("parent_id", parentId);
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setWhere(where);
        queryCondition.setN(1);
        List<IndexRecord> indexRecords = this.query(queryCondition, category);
        if (indexRecords != null && !indexRecords.isEmpty()) {
            result = toIndexSearchData(indexRecords.get(0));
        }
        vectorCache.putVectorChildCache(parentId, result);
        return result;
    }

    public IndexSearchData extendText(IndexSearchData data) {
        return extendText(data, vectorStore.getConfig().getDefaultCategory());
    }

    public IndexSearchData extendText(IndexSearchData data, String category) {
        int parentDepth = vectorStore.getConfig().getParentDepth();
        int childDepth = vectorStore.getConfig().getChildDepth();
        return extendText(parentDepth, childDepth, data, category);
    }

    public IndexSearchData extendText(int parentDepth, int childDepth, IndexSearchData data, String category) {
        String text = data.getText().trim();
        String splitChar = "";
        if (data.getFilename() != null && data.getFilename().size() == 1
                && data.getFilename().get(0).isEmpty()) {
            splitChar = "\n";
        }

        String parentId = data.getParentId();
        int parentCount = 0;
        for (int i = 0; i < parentDepth; i++) {
            IndexSearchData parentData = getParentIndex(parentId, category);
            if (parentData != null) {
                text = parentData.getText() + splitChar + text;
                parentId = parentData.getParentId();
                parentCount++;
            } else {
                break;
            }
        }
        if (parentCount < parentDepth) {
            childDepth = childDepth + parentDepth - parentCount;
        }
        parentId = data.getId();
        for (int i = 0; i < childDepth; i++) {
            IndexSearchData childData = getChildIndex(parentId, category);
            if (childData != null) {
                text = text + splitChar + childData.getText();
                parentId = childData.getId();
            } else {
                break;
            }
        }
        data.setText(text);
        return data;
    }

    public List<String> getImageFiles(IndexSearchData indexData) {
        List<String> imageList = null;

        if (indexData.getImage() != null && !indexData.getImage().isEmpty()) {
            imageList = new ArrayList<>();
            List<JsonObject> imageObjectList = gson.fromJson(indexData.getImage(), new TypeToken<List<JsonObject>>() {
            }.getType());
            for (JsonObject image : imageObjectList) {
                String url = image.get("path").getAsString();
                imageList.add(url);
            }
        }
        return imageList;
    }

    public List<VectorCollection> listCollections() {
        return this.vectorStore.listCollections();
    }
}
