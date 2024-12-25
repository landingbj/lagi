/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

package ai.llm.service;

import ai.common.ModelService;
import ai.common.pojo.Backend;
import ai.common.pojo.EnhanceChatCompletionRequest;
import ai.common.pojo.IndexSearchData;
import ai.config.ContextLoader;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.GetRagContext;
import ai.llm.utils.CacheManager;
import ai.manager.LlmManager;
import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.mr.container.FastDirectContainer;
import ai.mr.mapper.llm.UniversalMapper;
import ai.mr.reducer.llm.QaReducer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LagiGlobal;
import ai.utils.SensitiveWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import cn.hutool.core.bean.BeanUtil;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;
import weixin.tools.TulingThread;

import java.util.*;

@Slf4j
public class CompletionsService implements ChatCompletion {
    private static TulingThread tulingProcessor = null;
    private static final double DEFAULT_TEMPERATURE = 0.8;
    private static final int DEFAULT_MAX_TOKENS = 1024;

    static {
        if (tulingProcessor == null) {
            tulingProcessor = new TulingThread();
            tulingProcessor.setDaemon(true);
            tulingProcessor.start();
        }
    }

    private String getPolicy() {
        if (ContextLoader.configuration != null
                && ContextLoader.configuration.getFunctions() != null
                && ContextLoader.configuration.getFunctions().getChatPolicy() != null) {
            String chatPolicy = ContextLoader.configuration.getFunctions().getChatPolicy();
            if ("failover".equals(chatPolicy) || "parallel".equals(chatPolicy)) {
                return chatPolicy;
            }
        }
        return "failover";
    }

    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest, List<IndexSearchData> indexSearchDataList) {
        ChatCompletionResult answer = null;
        try (IRContainer contain = new FastDirectContainer() {
            @Override
            public void onMapperFail(String mapperName) {
                super.onMapperFail(mapperName);
                IMapper iMapper = mappersGroup.get(mapperName);
                if (iMapper instanceof UniversalMapper) {
                    UniversalMapper universalMapper = (UniversalMapper) iMapper;
                    ILlmAdapter adapter = universalMapper.getAdapter();
                    if (adapter instanceof ModelService) {
                        ModelService modelService = (ModelService) adapter;
                        CacheManager.put(modelService.getModel(), false);
                    }
                }
            }
        }) {
            boolean doCompleted = false;
            if (chatCompletionRequest.getModel() != null) {
                ILlmAdapter appointAdapter = LlmManager.getInstance().getAdapter(chatCompletionRequest.getModel());
                if (appointAdapter != null) {
                    registerMapper(chatCompletionRequest, appointAdapter, contain);
                    doCompleted = true;
                }
            }
            chatCompletionRequest.setModel(null);
            if (!doCompleted) {
                List<ILlmAdapter> ragAdapters = null;
                if (indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
                    ragAdapters = LlmRouterDispatcher.getRagAdapter(indexSearchDataList.get(0).getText());
                } else {
                    ragAdapters = LlmManager.getInstance().getAdapters();
                }
                boolean register = false;
                for (ILlmAdapter adapter : ragAdapters) {
                    if (adapter instanceof ModelService) {
                        ModelService modelService = (ModelService) adapter;
                        if (CacheManager.get(modelService.getModel())) {
                            continue;
                        }
                        registerMapper(chatCompletionRequest, adapter, contain);
                        register = true;
                        if ("failover".equals(getPolicy())) {
                            break;
                        }
                    }
                }
                if (!register && !ragAdapters.isEmpty()) {
                    Optional.ofNullable(ragAdapters.get(0)).ifPresent(adapter -> {
                        registerMapper(chatCompletionRequest, adapter, contain);
                    });
                }
            }
            IReducer qaReducer = new QaReducer();
            contain.registerReducer(qaReducer);
            @SuppressWarnings("unchecked")
            List<ChatCompletionResult> resultMatrix = (List<ChatCompletionResult>) contain.Init().running();
            if (resultMatrix.get(0) != null) {
                answer = resultMatrix.get(0);
                answer = SensitiveWordUtil.filter(answer);
            }
        }
        return answer;
    }

    private void registerMapper(ChatCompletionRequest chatCompletionRequest, ILlmAdapter adapter, IRContainer contain) {
        Map<String, Object> params = new HashMap<>();
        params.put(LagiGlobal.CHAT_COMPLETION_REQUEST, chatCompletionRequest);
        Backend backend = new Backend();
        backend.setModel(chatCompletionRequest.getModel());
        BeanUtil.copyProperties(adapter, backend);
        params.put(LagiGlobal.CHAT_COMPLETION_CONFIG, backend);
        IMapper mapper = getMapper(backend, adapter);
        mapper.setParameters(params);
        mapper.setPriority(backend.getPriority());
        contain.registerMapper(mapper);
    }

    public ChatCompletionResult completions(ILlmAdapter adapter, ChatCompletionRequest chatCompletionRequest) {
        if (adapter != null) {
            return adapter.completions(chatCompletionRequest);
        }
        return null;
    }

    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        return completions(chatCompletionRequest, null);
    }

    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        if (chatCompletionRequest.getModel() != null) {
            ILlmAdapter adapter = LlmManager.getInstance().getAdapter(chatCompletionRequest.getModel());
            if (adapter != null) {
                return adapter.streamCompletions(chatCompletionRequest);
            }
        }
        chatCompletionRequest.setModel(null);
        // no effect backend
        for (ILlmAdapter adapter : LlmManager.getInstance().getAdapters()) {
            if (adapter != null) {
                return adapter.streamCompletions(chatCompletionRequest);
            }
        }
        throw new RuntimeException("Stream backend is not enabled.");
    }


    public ILlmAdapter getRagAdapter(ChatCompletionRequest chatCompletionRequest, List<IndexSearchData> indexSearchDataList) {
        if (chatCompletionRequest.getModel() != null) {
            ILlmAdapter appointAdapter = LlmManager.getInstance().getAdapter(chatCompletionRequest.getModel());
            if (appointAdapter != null) {
                ModelService modelService = (ModelService) appointAdapter;
                Boolean isEffectAdapter = CacheManager.get(modelService.getModel());
                if (isEffectAdapter) {
                    return appointAdapter;
                }
            }
        }
        chatCompletionRequest.setModel(null);
        String indexData = indexSearchDataList == null || indexSearchDataList.isEmpty() ? null : indexSearchDataList.get(0).getText();
        List<ILlmAdapter> ragAdapters = LlmRouterDispatcher.getRagAdapter(indexData);
        if (!(ragAdapters == null || ragAdapters.isEmpty())) {
            Optional<ILlmAdapter> first = ragAdapters
                    .stream()
                    .filter(adapter -> {
                        ModelService modelService = (ModelService) adapter;
                        return CacheManager.get(modelService.getModel());
                    })
                    .findFirst();
            return first.orElse(ragAdapters.get(0));
        }
        return null;
    }

    public Observable<ChatCompletionResult> streamCompletions(ILlmAdapter adapter, ChatCompletionRequest chatCompletionRequest) {
        if (adapter != null) {
            return adapter.streamCompletions(chatCompletionRequest);
        }
        throw new RuntimeException("Stream backend is not enabled.");
    }


    private IMapper getMapper(Backend backendConfig, ILlmAdapter adapter) {
        return new UniversalMapper(adapter);
    }

    public void addVectorDBContext(ChatCompletionRequest request, String context) {

        String lastMessage = ChatCompletionUtil.getLastMessage(request);

        String prompt = "以下是背景信息：\n--------------------\n%s\n--------------------\n" +
                "根据上下文信息而非先前知识，回答以下这个问题，回答只基于上下文信息，不要随意扩展和发散内容，不要出现上下文里没有的信息: %s";

        if (request instanceof EnhanceChatCompletionRequest) {
            EnhanceChatCompletionRequest enhanceRequest = (EnhanceChatCompletionRequest) request;
            String userId = enhanceRequest.getUserId();
            String identity = enhanceRequest.getIdentity();
            Boolean meeting = enhanceRequest.getMeeting();
            if (identity != null) {
                String identitySpecificContext = "";

                if (lastMessage.contains("请假")) {
                    identitySpecificContext = getLeaveProcessContext(identity);
                } else if (lastMessage.contains("出差")) {
                    identitySpecificContext = getBusinessTripProcessContext(identity);
                } else if (lastMessage.contains("加班")) {
                    identitySpecificContext = getOvertimeProcessContext(identity);
                } else if (lastMessage.contains("晋升")) {
                    identitySpecificContext = getPromotionProcessContext(identity);
                } else if (lastMessage.contains("薪酬") || lastMessage.contains("薪水")) {
                    identitySpecificContext = getSalaryAdjustmentContext(identity);
                } else if (lastMessage.contains("培训")) {
                    identitySpecificContext = getTrainingApprovalContext(identity);
                }

                /*if (!identitySpecificContext.isEmpty()) {
                    String roleInChinese = identity.equals("leader") ? "领导" : "普通员工";
                    prompt = "根据用户身份信息：\n" +
                            "身份: " + roleInChinese + "\n" +
                            "背景信息：\n--------------------\n" + identitySpecificContext + "\n--------------------\n" +
                            "根据上下文信息和用户身份，回答以下问题，仅依据上下文信息，不要随意扩展，不要重复说明身份或背景信息：\n%s";
                }*/
                if (!identitySpecificContext.isEmpty()) {
                    String roleInChinese = identity.equals("leader") ? "领导" : "普通员工";
                    prompt = "根据用户身份信息：\n" +
                            "身份: " + roleInChinese + "\n" +
                            "背景信息：\n--------------------\n" + identitySpecificContext + "\n--------------------\n" +
                            "请根据上下文信息和用户身份，回答以下问题，仅依据上下文信息，不要随意扩展，不要添加背景总结或扩展内容，不要重复身份或背景信息：\n%s";
                }

            }
            //业务类问答接口
            if (meeting!=null&&meeting){
                    if (enhanceRequest.getBusiness()!=null&&enhanceRequest.getBusiness().equals("GSSW")){
                        prompt = "以下是上下文信息：\n--------------------\n%s\n--------------------\n" +
                                "根据上下文信息而非先前知识，回答以下这个问题，回答只基于上下文信息，不要随意扩展和发散内容，不要出现上下文里没有的信息。"+
                                "在描述公司收文时须包括：\n标题: ,\n流水号： ,\n收文日期： ,\n来文字号： ,\n来文单位： ,\n收文内容:。"+
                                "如果上下文中有多个收文与问题相关请分别返回。请以md格式回答以下问题:\n %s\n";
                    } else if (enhanceRequest.getBusiness()!=null&&enhanceRequest.getBusiness().equals("HYJY")){
                        prompt = "以下是上下文信息：\n--------------------\n%s\n--------------------\n" +
                                "根据上下文信息而非先前知识，回答以下这个问题，回答只基于上下文信息，不要随意扩展和发散内容，不要出现上下文里没有的信息。"+
                                "在描述会议时须包括：\n会议编号：,\n类别： ,\n主题： ,\n会议时间： ,\n会议内容: ,每一项结束要以md格式换行。"+
                                "不用返回拟稿人，电话，份号，分送单位，出席人员。如果上下文中有多个会议与问题相关请分别返回。请以md格式回答以下问题:\n %s\n";
                        // +"如果上下文中的会议内容和我的问题相差非常远，就只用告诉我：“对不起，没有找到相关的会议！”";
                        //请在描述会议时请完整的描述出和问题相关的会议信息。
                        // 如果上下文中的会议内容和我的完全问题没有任何关系，就只用告诉我：“对不起，没有找到相关的会议纪要！”
                    }else {
                        prompt = "以下是上下文信息：\n--------------------\n%s\n--------------------\n" +
                                "根据上下文信息而非先前知识，回答以下这个问题，回答只基于上下文信息，不要随意扩展和发散内容，不要出现上下文里没有的信息。"+
                                "在描述会议时须包括：\n会议编号：,\n类别： ,\n主题： ,\n会议时间： ,\n会议内容: ,每一项结束要以md格式换行。"+
                                "不用返回拟稿人，电话，份号，分送单位，出席人员。如果上下文中有多个会议与问题相关请分别返回。请以md格式回答以下问题:\n %s\n";
                    }

            }
        }
        prompt = String.format(prompt, context, lastMessage);
        ChatCompletionUtil.setLastMessage(request, prompt);
    }

    public GetRagContext getRagContext(List<IndexSearchData> indexSearchDataList, int maxInput) {
        if (indexSearchDataList.isEmpty()) {
            return null;
        }
        List<String> filePaths = new ArrayList<>();
        List<String> filenames = new ArrayList<>();
        List<String> chunkIds = new ArrayList<>();
        String context = indexSearchDataList.get(0).getText();
        context+="\n"+indexSearchDataList.get(0).getTitle();
        if (indexSearchDataList.get(0).getFilepath() != null && indexSearchDataList.get(0).getFilename() != null) {
            filePaths.addAll(indexSearchDataList.get(0).getFilepath());
            filenames.addAll(indexSearchDataList.get(0).getFilename());
            chunkIds.add(indexSearchDataList.get(0).getId());
        }
        double firstDistance = indexSearchDataList.get(0).getDistance();
        double lastDistance = firstDistance;
        List<Double> diffList = new ArrayList<>();
        for (int i = 1; i < indexSearchDataList.size(); i++) {
            if (i == 1) {
                IndexSearchData data = indexSearchDataList.get(i);
                double diff = data.getDistance() - firstDistance;
                double threshold = diff / firstDistance;
                if (threshold < 0.25) {
                    if (data.getFilepath() != null && data.getFilename() != null) {
                        filePaths.addAll(data.getFilepath());
                        filenames.addAll(data.getFilename());
                        chunkIds.add(data.getId());
                    }
                    context += "\n" + data.getText();
                    context += "\n" + data.getTitle();
                    lastDistance = data.getDistance();
                    diffList.add(diff);
                    if (context.length() > maxInput) {
                        break;
                    }
                } else {
                    break;
                }
            } else if (i == 2) {
                IndexSearchData data = indexSearchDataList.get(i);
                double diff = data.getDistance() - lastDistance;
                if (diff < diffList.get(0) * 0.618) {
                    if (data.getFilepath() != null && data.getFilename() != null) {
                        filePaths.addAll(data.getFilepath());
                        filenames.addAll(data.getFilename());
                        chunkIds.add(data.getId());
                    }
                    context += "\n" + data.getText();
                    context += "\n" + data.getTitle();
                    lastDistance = data.getDistance();
                    diffList.add(diff);
                    if (context.length() > maxInput) {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                IndexSearchData data = indexSearchDataList.get(i);
                double diff = data.getDistance() - lastDistance;
                double average = diffList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                if (diff < average) {
                    context += "\n" + data.getText();
                    context += "\n" + data.getTitle();
                    lastDistance = data.getDistance();
                    diffList.add(diff);
                    if (data.getFilepath() != null && data.getFilename() != null) {
                        filePaths.addAll(data.getFilepath());
                        filenames.addAll(data.getFilename());
                        chunkIds.add(data.getId());
                    }
                    if (context.length() > maxInput) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return GetRagContext.builder()
                .filenames(filenames)
                .filePaths(filePaths)
                .context(context)
                .chunkIds(chunkIds)
                .build();
    }

    public ChatMessage getChatMessage(String question, String role) {
        ChatMessage message = new ChatMessage();
        message.setRole(role);
        message.setContent(question);
        return message;
    }

    public ChatCompletionRequest getCompletionsRequest(String prompt) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(getChatMessage(prompt, LagiGlobal.LLM_ROLE_USER));
        return getCompletionsRequest(messages);
    }

    public ChatCompletionRequest getCompletionsRequest(String systemPrompt, String prompt, String category) {
        List<ChatMessage> messages = new ArrayList<>();
        if (systemPrompt != null) {
            messages.add(getChatMessage(systemPrompt, LagiGlobal.LLM_ROLE_SYSTEM));
        }
        messages.add(getChatMessage(prompt, LagiGlobal.LLM_ROLE_USER));
        return getCompletionsRequest(messages, category);
    }

    public ChatCompletionRequest getCompletionsRequest(List<ChatMessage> messages) {
        return getCompletionsRequest(messages, null);
    }

    public ChatCompletionRequest getCompletionsRequest(List<ChatMessage> messages, String category) {
        return getCompletionsRequest(messages, DEFAULT_TEMPERATURE, DEFAULT_MAX_TOKENS, category);
    }

    public ChatCompletionRequest getCompletionsRequest(List<ChatMessage> messages, double temperature, int maxTokens, String category) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(temperature);
        chatCompletionRequest.setStream(false);
        chatCompletionRequest.setMax_tokens(maxTokens);
        chatCompletionRequest.setMessages(messages);
        chatCompletionRequest.setCategory(category);
        return chatCompletionRequest;
    }

    public String getLeaveProcessContext(String identity) {
        if ("leader".equals(identity)) {
            return "作为领导，您的请假申请需要先通过部门HR审核，然后提交给更高层管理者审批，最终由行政部门备案。";
        } else if ("personnel".equals(identity)) {
            return "作为普通员工，您需要向直接上级申请，请假获得批准后，再提交给HR部门备案。";
        }
        return "根据您的身份，具体的请假流程会有所不同。";
    }

    public String getBusinessTripProcessContext(String identity) {
        if ("leader".equals(identity)) {
            return "作为领导，您的出差申请需经过部门审批，并由行政或人力资源部门协调差旅安排。";
        } else if ("personnel".equals(identity)) {
            return "作为普通员工，您需要先向上级领导申请，获得批准后提交行政部门进行出差安排。";
        }
        return "根据您的身份，具体的出差申请流程会有所不同。";
    }

    public String getOvertimeProcessContext(String identity) {
        if ("leader".equals(identity)) {
            return "作为领导，您可以根据部门需求直接批准加班，并负责协调人员安排。";
        } else if ("personnel".equals(identity)) {
            return "作为普通员工，您需要先向直接上级申请加班，经批准后才可以进行加班。";
        }
        return "加班流程可能会根据身份有所不同。";
    }

    public String getPromotionProcessContext(String identity) {
        if ("leader".equals(identity)) {
            return "作为领导，您需要对员工晋升进行评估，并向上级部门报告决策。";
        } else if ("personnel".equals(identity)) {
            return "作为普通员工，您可以向上级申请晋升，审批流程需经过部门领导及HR审核。";
        }
        return "晋升流程可能会因身份不同而有所差异。";
    }

    public String getSalaryAdjustmentContext(String identity) {
        if ("leader".equals(identity)) {
            return "作为领导，您有权限决定团队成员的薪酬调整，并需与HR协作完成调整。";
        } else if ("personnel".equals(identity)) {
            return "作为普通员工，薪酬调整通常由领导提出并通过HR审核，员工可提出建议或申请。";
        }
        return "薪酬调整的具体流程会根据您的身份有所不同。";
    }

    public String getTrainingApprovalContext(String identity) {
        if ("leader".equals(identity)) {
            return "作为领导，您需要审核并批准团队成员的培训申请。";
        } else if ("personnel".equals(identity)) {
            return "作为普通员工，您可以向上级申请培训，申请需经过上级领导和HR的批准。";
        }
        return "培训审批流程会因身份不同而有所差异。";
    }
}
