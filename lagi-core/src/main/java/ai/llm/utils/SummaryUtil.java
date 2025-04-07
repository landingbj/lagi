package ai.llm.utils;

import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.JsonExtractor;
import ai.utils.LlmUtil;
import ai.utils.qa.ChatCompletionUtil;
import com.google.gson.Gson;
import lombok.*;


public class SummaryUtil {

    public static final String SUMMARY_PROMPT = "请根据以下规则处理连续提问的补全任务：\n" +
            "\n" +
            "1. **任务目标**：补全用户当前问题中因连续提问而省略的信息，确保补全内容完全来自对话历史记录，禁止添加任何未提及的内容, 禁止丢失当前问题中的信息。\n" +
            "\n" +
            "2. **操作步骤**：\n" +
            "   - **分析当前问题**：识别问题中可能缺失的实体（如时间、地点、产品名称、之前提到的事件等）。\n" +
            "   - **检索历史记录**：从对话历史中查找与当前问题直接相关的上下文信息。\n" +
            "   - **精准补全**：将缺失的信息替换或补充到当前问题中，确保语义完整且符合逻辑。\n" +
            "   - **验证来源**：所有补全内容必须能直接追溯到历史记录中的明确表述，避免推测或假设。\n" +
            "   - **验证当前问题**：当前问题中所有的信息都完全保留\n" +
            "\n" +
            "3. **输出格式**：\n" +
            "   - 返回 JSON 对象包含两个字段：\n" +
            "     - `original_question`：用户当前原始输入的未补全问题。\n" +
            "     - `supplemented_question`：补全后的完整问题（仅使用历史记录信息）。\n" +
            "\n" +
            "4. **示例参考**：\n" +
            "   - 若历史记录提到“我之前问过关于巴黎的酒店”，而当前问题为“酒店价格如何？”，\n" +
            "     补全后应为“巴黎的酒店价格如何？”。\n" +
            "\n" +
            "请严格遵循规则，确保输出符合格式要求。";


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    static class SummaryResult {
        private String original_question;
        private String supplemented_question;
    }

    public static String invoke(ChatCompletionRequest request) {
        String question = ChatCompletionUtil.getLastMessage(request);
        ChatCompletionResult chatCompletionResult = LlmUtil.callLLm(SUMMARY_PROMPT, request);
        String firstAnswer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        String json = JsonExtractor.extractJson(firstAnswer);
        Gson gson = new Gson();
        SummaryResult summary =  gson.fromJson(json, SummaryResult.class);
        if(summary != null && summary.getSupplemented_question() != null) {
            question = summary.getSupplemented_question();
            request.getMessages().get(request.getMessages().size() - 1).setContent(question);
        }
        return question;
    }





}
