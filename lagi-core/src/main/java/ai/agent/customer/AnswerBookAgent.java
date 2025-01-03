package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.AnswerBookTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class AnswerBookAgent extends CustomerAgent {
    public AnswerBookAgent(AgentConfig agentConfig) {
        super(agentConfig);
        AnswerBookTool answerBookTool = new AnswerBookTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(answerBookTool.getToolInfo(), finishTool.getToolInfo());
    }
}
