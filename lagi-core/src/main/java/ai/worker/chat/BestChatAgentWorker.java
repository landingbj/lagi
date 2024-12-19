//package ai.worker.chat;
//
//import ai.agent.Agent;
//import ai.mr.IMapper;
//import ai.openai.pojo.ChatCompletionRequest;
//import ai.openai.pojo.ChatCompletionResult;
//import ai.utils.qa.ChatCompletionUtil;
//import ai.worker.DefaultBestWorker;
//import ai.worker.SkillMap;
//import ai.workflow.mapper.ChatAgentMapper;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Slf4j
//public class BestChatAgentWorker extends DefaultBestWorker<ChatCompletionRequest, ChatCompletionResult> {
//
//    public BestChatAgentWorker(List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
//        super(agents);
//        convert2Mapper(agents);
//    }
//
//
//    @Override
//    protected List<Agent<ChatCompletionRequest, ChatCompletionResult>> filterAgentsBySkillMap(List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents, ChatCompletionRequest data) {
//        SkillMap skillMap = new SkillMap();
//        return skillMap.filterAgentByIntentKeyword(agents, ChatCompletionUtil.getLastMessage(data), 5.0);
//    }
//
//    @Override
//    protected List<IMapper> convert2Mapper(List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
//        return agents.stream().map(agent -> {
//            ChatAgentMapper chatAgentMapper = new ChatAgentMapper();
//            chatAgentMapper.setAgent(agent);
//            return chatAgentMapper;
//        }).collect(Collectors.toList());
//    }
//}
