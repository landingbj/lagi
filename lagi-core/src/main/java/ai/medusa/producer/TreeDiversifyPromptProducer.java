package ai.medusa.producer;

import ai.common.pojo.IndexSearchData;
import ai.config.ContextLoader;
import ai.medusa.dao.TreeDiversifyDao;
import ai.medusa.impl.CompletionCache;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.medusa.pojo.TreeDiversifyNode;
import ai.medusa.utils.PromptCacheConfig;
import ai.medusa.utils.PromptCacheTrigger;
import ai.openai.pojo.ChatCompletionResult;
import ai.vector.VectorStoreService;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.UpsertRecord;
import ai.vector.pojo.VectorCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TreeDiversifyPromptProducer extends DiversifyPromptProducer {
    private static final VectorStoreService vectorStoreService = new VectorStoreService();
    private static final String MEDUSA_CATEGORY = PromptCacheConfig.MEDUSA_TREE_CATEGORY;
    private static final int TREE_SIMILARITY_TOP_K = PromptCacheConfig.TREE_SIMILARITY_TOP_K;
    private static final double TREE_SIMILARITY_CUTOFF = PromptCacheConfig.TREE_SIMILARITY_CUTOFF;
    private static final TreeDiversifyDao treeDiversifyDao = new TreeDiversifyDao();
    private static final Logger log = LoggerFactory.getLogger(TreeDiversifyPromptProducer.class);

    public TreeDiversifyPromptProducer(int limit) {
        super(limit);
        loadGraphNode2VectorDB();
    }

    @Override
    public void init() {
    }

    @Override
    public void cleanup() {
    }

    @Override
    public Collection<PooledPrompt> produce(PooledPrompt item) {
        return diversify(item);
    }

    @Override
    public void consume(PooledPrompt item) throws Exception {
        super.consume(item);
    }

    public Collection<PooledPrompt> diversify(PooledPrompt item) {
        List<String> promptList = item.getPromptInput().getPromptList();
        List<String> nextPrompt = predictNextPrompt(promptList, TREE_SIMILARITY_TOP_K);
        Collection<PooledPrompt> result = new ArrayList<>();
        if (nextPrompt == null || nextPrompt.isEmpty()) {
            return result;
        }
        CompletionCache instance = CompletionCache.getInstance();
        nextPrompt.forEach(prompt -> {
            List<String> predictPromptList = new ArrayList<>(promptList);
            predictPromptList.add(prompt);
            PromptInput diversifiedPromptInput = PromptInput.builder()
                    .parameter(item.getPromptInput().getParameter())
                    .promptList(predictPromptList)
                    .build();
            PromptCacheTrigger promptCacheTrigger = new PromptCacheTrigger();
            diversifiedPromptInput = promptCacheTrigger.analyzeChatBoundaries(diversifiedPromptInput);
            // skill cached
            ChatCompletionResult chatCompletionResult = instance.get(diversifiedPromptInput);
            if (chatCompletionResult != null) {
                return;
            }
            PooledPrompt pooledPrompt = PooledPrompt.builder()
                    .promptInput(diversifiedPromptInput)
                    .status(PromptCacheConfig.POOL_INITIAL)
                    .indexSearchData(searchByContext(diversifiedPromptInput))
                    .build();
            result.add(pooledPrompt);
        });
        log.info("tree diversify prompt: {}", result);
        return result;
    }


    public void loadGraphNode2VectorDB() {
        CompletableFuture.runAsync(() -> {
            int allNodeCount = treeDiversifyDao.getAllNodeCount();
            int vectorNum = getVectorNum();
            if (allNodeCount == vectorNum) {
                return;
            }
            List<TreeDiversifyNode> allNodeTexts = treeDiversifyDao.getAllNodeTexts();
            allNodeTexts.forEach(node -> {
                try {
                    if (!vectorExists(node.getId())) {
                        insertVector(node.getId(), node.getText());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private List<String> predictNextPrompt(List<String> promptList, int top) {
        List<String> text = convertPrompt2TreeText(promptList);
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        String nodeText = text.get(0);
        return treeDiversifyDao.searchChildNode(nodeText, top);
    }

    private List<String> convertPrompt2TreeText(List<String> promptList) {
        List<String> texts = new ArrayList<>();
        if (promptList == null || promptList.isEmpty()) {
            return texts;
        }
        addAdjacentUnsavedNodesToDatabase(promptList, texts);
        return texts;
    }


    private void addAdjacentUnsavedNodesToDatabase(List<String> promptList, List<String> texts) {
        String lastText = null;
        Integer lastId = null;
        for (int i = promptList.size() - 1; i >= 0; i--) {
            // convert prompt to text ( real save to database text)

            // save text to database
            String currentText = promptList.get(i);
            Integer cId = treeDiversifyDao.getIdByText(currentText);
            if (cId == null) {
                treeDiversifyDao.saveGraphNode(currentText);
                cId = treeDiversifyDao.getIdByText(currentText);
            }

            if (!vectorExists(String.valueOf(cId))) {
                insertVector(String.valueOf(cId), currentText);
            }

            // add saved text to texts
            texts.add(currentText);
            // save relation
            if (lastText != null && lastId != null) {
                // if relation exists, break
                boolean b = treeDiversifyDao.hasRelation(cId, lastId);
                // update hitCount or save relation
                treeDiversifyDao.saveRelation(cId, lastId);
                if (b) {
                    break;
                }
            }
            // go to save next relation
            lastText = currentText;
            lastId = cId;
        }
    }


    private List<IndexSearchData> searchWordVector(String prompt) {
        return searchWordVector(prompt, TREE_SIMILARITY_CUTOFF);
    }

    private List<IndexSearchData> searchWordVector(String prompt, double cutoff) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("category", MEDUSA_CATEGORY);
        return vectorStoreService.search(prompt, TREE_SIMILARITY_TOP_K, cutoff, metadata, MEDUSA_CATEGORY);
    }

    private int getVectorNum() {
        List<VectorCollection> vectorCollections = vectorStoreService.listCollections();
        for (VectorCollection vectorCollection : vectorCollections) {
            if (MEDUSA_CATEGORY.equals(vectorCollection.getCategory())) {
                return vectorCollection.getVectorCount();
            }
        }
        return 0;
    }

    private boolean vectorExists(String id) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("id", id);
        List<IndexRecord> records = vectorStoreService.fetch(metadata, MEDUSA_CATEGORY);
        return !records.isEmpty();
    }

    public void insertVector(String id, String prompt) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("category", MEDUSA_CATEGORY);
        metadata.put("id", id);
        UpsertRecord upsertRecord = UpsertRecord.newBuilder().withDocument(prompt).withMetadata(metadata).build();
        vectorStoreService.upsertCustomVectors(Collections.singletonList(upsertRecord), MEDUSA_CATEGORY);
    }

    public static void main(String[] args) {
        ContextLoader.loadContext();
        TreeDiversifyPromptProducer treeDiversifyPromptProducer = new TreeDiversifyPromptProducer(PromptCacheConfig.PRODUCER_LIMIT);
        List<String> strings = new ArrayList<>();
        strings.add("你好");
        List<String> strings1 = treeDiversifyPromptProducer.predictNextPrompt(strings, 10);
        System.out.println(strings1);
    }
}
