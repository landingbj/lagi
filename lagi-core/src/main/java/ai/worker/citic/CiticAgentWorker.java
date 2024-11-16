package ai.worker.citic;

import ai.common.pojo.Configuration;
import ai.config.pojo.AgentConfig;
import ai.llm.pojo.ChatCompletionResultWithSource;
import ai.llm.service.CompletionsService;
import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.utils.OkHttpUtil;
import ai.worker.WorkerGlobal;
import ai.workflow.container.AgentContainer;
import ai.workflow.mapper.ExchangeMapper;
import ai.workflow.mapper.RagMapper;
import ai.workflow.mapper.StockMapper;
import ai.workflow.mapper.XiaoxinMapper;
import ai.workflow.reducer.AgentReducer;
import cn.hutool.core.bean.BeanUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.channels.Channel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CiticAgentWorker {
    public ChatCompletionResult process(ChatCompletionRequest chatCompletionRequest, String url) {
        ChatCompletionResult chatCompletionResult = null;
        Map<String, Object> params = new HashMap<>();
        params.put(WorkerGlobal.MAPPER_CHAT_REQUEST, chatCompletionRequest);
        params.put(WorkerGlobal.MAPPER_RAG_URL, url);

        // 加载提示词模块，并加载到chatCompletionRequest中
        String prompt = "";
        chatCompletionRequest.getMessages().get(chatCompletionRequest.getMessages().size() - 1).setContent(prompt + chatCompletionRequest.getMessages().get(chatCompletionRequest.getMessages().size() - 1).getContent());

        try (IRContainer contain = new AgentContainer()) {
            IMapper ragMapper = new RagMapper();
            ragMapper.setParameters(params);
            ragMapper.setPriority(WorkerGlobal.MAPPER_PRIORITY - 5);
            contain.registerMapper(ragMapper);

            IMapper xiaoxinMapper = new XiaoxinMapper();
            xiaoxinMapper.setParameters(params);
            xiaoxinMapper.setPriority(WorkerGlobal.MAPPER_PRIORITY);
            contain.registerMapper(xiaoxinMapper);

            IMapper stockMapper = new StockMapper();
            stockMapper.setParameters(params);
            stockMapper.setPriority(WorkerGlobal.MAPPER_PRIORITY);
            contain.registerMapper(stockMapper);

            IMapper exchangeMapper = new ExchangeMapper();
            exchangeMapper.setParameters(params);
            exchangeMapper.setPriority(WorkerGlobal.MAPPER_PRIORITY);
            contain.registerMapper(exchangeMapper);

            IReducer agentReducer = new AgentReducer();
            contain.registerReducer(agentReducer);

            @SuppressWarnings("unchecked")
            List<ChatCompletionResult> resultMatrix = (List<ChatCompletionResult>) contain.Init().running();
            if (resultMatrix.get(0) != null) {
                chatCompletionResult = resultMatrix.get(0);
                System.out.println("CiticAgentWorker.process: chatCompletionResult = " + chatCompletionResult);
//                String prompt = "你是一位专业的金融领域审核员，负责审核由大模型生成的金融相关文本内容。在审核过程中，需要从合规性和语言表达与可读性两个重要维度进行严格审查。\n" +
//                        "合规性审查\n" +
//                        "法律法规方面\n" +
//                        "检查文本是否遵守所有适用的金融法律法规，包括但不限于《中华人民共和国商业银行法》《中华人民共和国证券法》《中华人民共和国保险法》《中华人民共和国证券投资基金法》等。确保没有任何内容涉及违法的金融活动，如非法吸收公众存款、内幕交易、操纵市场等表述。\n" +
//                        "对于金融产品和服务的描述，确认符合反洗钱法规，不存在可能协助洗钱的信息，比如模糊客户身份识别要求或鼓励异常资金流动的内容。同时，依据《中华人民共和国广告法》，杜绝夸大收益、虚假宣传或对风险轻描淡写的情况，特别是涉及到理财产品、投资项目等的回报承诺。\n" +
//                        "监管要求层面\n" +
//                        "严格参照银保监会、证监会等金融监管机构发布的最新规定和指导意见。确保金融业务开展、产品设计、信息披露等环节都在监管允许的范围内。例如，金融创新产品的介绍必须符合监管对创新业务的界定和规范，不得擅自突破监管设定的边界。\n" +
//                        "金融伦理道德考量\n" +
//                        "检查是否存在任何歧视性言论，无论是基于客户的年龄、性别、种族、地域还是其他不合理因素。确保金融产品推荐和服务建议秉持公正、公平原则，充分考虑客户利益，不能诱导客户进行不合理或高风险的金融操作，维护金融市场的公平秩序。\n" +
//                        "语言表达与可读性审查\n" +
//                        "语言准确性\n" +
//                        "核实金融术语的使用是否精准无误。常见的金融术语如 “净现值（NPV）”“内部收益率（IRR）”“资本充足率”“拨备覆盖率” 等，必须在正确的语境下使用，避免任何混淆或误用。同时，检查普通词汇的拼写和用法，杜绝错别字、语法错误，确保文本质量。\n" +
//                        "表达清晰性\n" +
//                        "审查语句结构是否清晰明了。避免过于复杂冗长的句子结构，尽量使用简洁易懂的短句和简单语法。同时，逻辑关系要明确，通过合理使用连接词（如 “因为”“所以”“然而”“并且” 等）使内容层次清晰，便于理解。例如，对于金融分析内容，因果关系要一目了然，如 “由于宏观经济形势向好，市场需求增加，因此该行业的企业盈利有望提升。”\n" +
//                        "通俗易懂程度\n" +
//                        "在保证专业性的同时，尽可能使用通俗易懂的表达方式来阐述金融概念和信息。对于复杂的金融原理或产品特点，要以简单易懂的方式解释，比如将复杂的金融衍生品交易原理用日常生活中的例子类比说明，让普通投资者也能理解。避免使用过于生僻或只有行业内部人员才懂的 “黑话”，若必须使用，则要进行适当的解释。\n" +
//                        "格式规范性\n" +
//                        "检查文本的排版格式是否规范。段落划分要合理，不同主题或内容板块之间要有明显的区分。对于涉及数字、符号、图表的内容，要确保数字准确、符号使用正确，图表要有清晰的标题、坐标轴标签和数据来源说明，使整个文本在视觉上和内容上都具有良好的可读性。\n" +
//                        "请仔细审查以下大模型生成的金融领域文本，如无明显问题按照原文返回结果；如果存在问题，则返回：小信最近学习了很多关于基金方面的知识，其他领域还有所欠缺，您可以尝试换个方式描述您的问题。\n";
//                String responseJson = null;
//                final Gson gson = new Gson();
//                chatCompletionRequest.getMessages().get(chatCompletionRequest.getMessages().size() - 1).setContent(prompt + chatCompletionResult.getChoices().get(0).getMessage().getContent());
//
//                CompletionsService completionsService = new CompletionsService();
//                chatCompletionResult = completionsService.completions(chatCompletionRequest);
//                try {
//                    responseJson = OkHttpUtil.post(url + "/v1/chat/completions", gson.toJson(chatCompletionRequest));
//                } catch (IOException e) {
////                    logger.error("RagMapper.myMapping: OkHttpUtil.post error", e);
//                }
//                if (responseJson != null) {
//                    chatCompletionResult = gson.fromJson(responseJson, ChatCompletionResult.class);
//                    ChatCompletionResultWithSource chatCompletionResultWithSource = new ChatCompletionResultWithSource("提示词");
//                    BeanUtil.copyProperties(chatCompletionResult, chatCompletionResultWithSource);
//                    chatCompletionResult = chatCompletionResultWithSource;
//                }
            }
        }
        return chatCompletionResult;
    }
}
