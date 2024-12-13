package ai.router.utils;

import ai.agent.Agent;
import ai.common.exception.RRException;
import ai.manager.AgentManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.Failover;
import ai.router.Parallel;
import ai.router.Polling;
import ai.router.Route;
import ai.worker.DefaultAppointWorker;
import ai.worker.DefaultBestWorker;
import ai.worker.chat.BestChatAgentWorker;
import cn.hutool.core.util.StrUtil;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RouterParser {

    public static final char GROUP_START = '(';
    public static final char GROUP_END = ')';

    public static final char POLLING_SEPARATOR = '|';
    public static final char PARALLEL_SEPARATOR = '&';
    public static final char FAILOVER_SEPARATOR = ',';

    public static final char WILDCARD = '%';

    public static boolean checkValid(String router) {
        Stack<Integer> stack = new Stack<>();
        Map<Integer, Character> groupSplits = new HashMap<>();
        int index = 0;
        for (char c : router.toCharArray()) {
            if (c == GROUP_START) {
                stack.push(index);
            }else if (c == GROUP_END) {
                groupSplits.remove(stack.size());
                stack.pop();
            } else if (c == POLLING_SEPARATOR ||
                    c == PARALLEL_SEPARATOR ||
                    c == FAILOVER_SEPARATOR) {
                Character prevSeparator = groupSplits.get(stack.size());
                if(prevSeparator == null) {
                    groupSplits.put(stack.size(), c);
                }else if(prevSeparator != c) {
                    return false;
                }
            }
            index++;
        }
        return stack.isEmpty();
    }

    public static Route parse(String path, String router) {
        if(!checkValid(router)) {
            throw new RRException(400, "Invalid router rule");
        }
        if((""+WILDCARD).equals(router)) {
            return buildWildcardRule(path);
        }
        return buildPatternRule(path, router);
    }

    private static Route buildPatternRule(String path, String router) {
        String regx = StrUtil.format("[\\w\\-_\\p{IsHan}]+([{}{}{}][\\w\\-_\\p{IsHan}]+)*", POLLING_SEPARATOR, FAILOVER_SEPARATOR, PARALLEL_SEPARATOR);
        String s = router.replaceAll(regx, "func");
        char separator = 0;
        if(s.contains(""+POLLING_SEPARATOR)) {
            separator = POLLING_SEPARATOR;
        } else if(s.contains(""+FAILOVER_SEPARATOR)) {
            separator = FAILOVER_SEPARATOR;
        } else if (s.contains(""+PARALLEL_SEPARATOR)) {
            separator = PARALLEL_SEPARATOR;
        }
        Pattern pattern = Pattern.compile(regx);
        Matcher matcher = pattern.matcher(router);
        List<Function<ChatCompletionRequest, ChatCompletionResult>> functions = new ArrayList<>();
        while (matcher.find()) {
            String group = matcher.group();
            functions.add( buildSingleFunc(group));
        }
        if(separator == 0) {
            return new Route(path, functions.stream().findFirst().orElse(null));
        }
        Function<ChatCompletionRequest, ChatCompletionResult> function = null;
        if(s.contains(""+POLLING_SEPARATOR)) {
            function = new Polling<>(functions);
        } else if(s.contains(""+FAILOVER_SEPARATOR)) {
            function = new Failover<>(functions);
        } else if (s.contains(""+PARALLEL_SEPARATOR)) {
//            function = new Parallel<>(functions);
            throw new RRException("not support rule");
        }
        return new Route(path, function);
    }

    private static Function<ChatCompletionRequest, ChatCompletionResult> buildSingleFunc(String r) {
        if(r.contains(""+FAILOVER_SEPARATOR)) {
            List<Function<ChatCompletionRequest, ChatCompletionResult>> functions = Arrays.stream(r.split("" + FAILOVER_SEPARATOR)).map(a -> {
                Agent<ChatCompletionRequest, ChatCompletionResult> agent = (Agent<ChatCompletionRequest, ChatCompletionResult>) AgentManager.getInstance().get(a);
                return (Function<ChatCompletionRequest, ChatCompletionResult>) agent::communicate;
            }) .collect(Collectors.toList());
            return new Failover<>(functions);
        } else if(r.contains(""+POLLING_SEPARATOR)) {
            List<Function<ChatCompletionRequest, ChatCompletionResult>> functions = Arrays.stream(r.split("" + POLLING_SEPARATOR)).map(a -> {
                Agent<ChatCompletionRequest, ChatCompletionResult> agent = (Agent<ChatCompletionRequest, ChatCompletionResult>) AgentManager.getInstance().get(a);
                return (Function< ChatCompletionRequest, ChatCompletionResult>) agent::communicate;
            }) .collect(Collectors.toList());
            return  new Polling<>(functions);
        } else if(r.contains(""+PARALLEL_SEPARATOR)) {
            List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents = Arrays.stream(r.split("" + PARALLEL_SEPARATOR)).map(a -> (Agent<ChatCompletionRequest, ChatCompletionResult>) AgentManager.getInstance().get(a)).filter(Objects::nonNull).collect(Collectors.toList());
            DefaultBestWorker<ChatCompletionRequest, ChatCompletionResult> worker = new BestChatAgentWorker(agents);
            return new Parallel<>(worker::work);
        }
        return null;
    }


    private static Route buildWildcardRule(String path) {
        List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents = AgentManager.getInstance().agents().stream()
                .map(a -> (Agent<ChatCompletionRequest, ChatCompletionResult>) a) .collect(Collectors.toList());
        DefaultAppointWorker<ChatCompletionRequest, ChatCompletionResult> defaultAppointWorker = new DefaultAppointWorker<>(agents);
        return new Route(path, defaultAppointWorker::call);
    }

    public static void main(String[] args) {
        System.out.println(parse("a", "(小信智能体,a)"));
        System.out.println(parse("a", "((a&b&c),(c|d))"));
//        System.out.println(parse("a", "((a&b&c),((d,c)|(e,f)))"));
    }


}
