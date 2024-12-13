package ai.worker.chat;

import ai.agent.Agent;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.worker.DefaultBestWorker;
import ai.workflow.mapper.ChatAgentMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class BestChatAgentWorker extends DefaultBestWorker<ChatCompletionRequest, ChatCompletionResult> {

    public BestChatAgentWorker(List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        super(agents);
        mappers = agents.stream().map(agent -> {
            ChatAgentMapper chatAgentMapper = new ChatAgentMapper();
            chatAgentMapper.setAgent(agent);
            return chatAgentMapper;
        }).collect(Collectors.toList());
    }
}
