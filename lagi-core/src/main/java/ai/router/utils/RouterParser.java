package ai.router.utils;

import ai.agent.Agent;
import ai.agent.proxy.LlmProxyAgent;
import ai.common.exception.RRException;
import ai.manager.AgentManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.*;
import ai.router.pojo.Parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RouterParser {
    private static final char GROUP_START = RouteGlobal.GROUP_START;
    private static final char GROUP_END = RouteGlobal.GROUP_END;

    private static final char POLLING_SEPARATOR = RouteGlobal.POLLING_SEPARATOR;
    private static final char PARALLEL_SEPARATOR = RouteGlobal.PARALLEL_SEPARATOR;
    private static final char FAILOVER_SEPARATOR = RouteGlobal.FAILOVER_SEPARATOR;

    private static final String WILDCARD_STRING = RouteGlobal.WILDCARD_STRING;

    public static boolean checkValid(String router) {
        Stack<Integer> stack = new Stack<>();
        Map<Integer, Character> groupSplits = new HashMap<>();
        int index = 0;
        for (char c : router.toCharArray()) {
            if (c == GROUP_START) {
                stack.push(index);
            } else if (c == GROUP_END) {
                groupSplits.remove(stack.size());
                stack.pop();
            } else if (c == POLLING_SEPARATOR ||
                    c == PARALLEL_SEPARATOR ||
                    c == FAILOVER_SEPARATOR) {
                Character prevSeparator = groupSplits.get(stack.size());
                if (prevSeparator == null) {
                    groupSplits.put(stack.size(), c);
                } else if (prevSeparator != c) {
                    return false;
                }
            }
            index++;
        }
        return stack.isEmpty();
    }

    public static String getRuleName(String route) {
        int i = route.indexOf("(");
        return route.substring(0, i);
    }

    public static List<String> getParams(String route) {
        int s = route.indexOf("(");
        int e = route.lastIndexOf(")");
        return Arrays.stream(route.substring(s + 1, e).split("[|&,]+"))
                .map(String::trim)
                .collect(Collectors.toList());
    }


    public static Route parse(String path, String router) {
        if (!checkValid(router)) {
            throw new RRException(400, "Invalid router rule");
        }
        List<String> params = RouterParser.getParams(router);
        if (params.size() == 1 && WILDCARD_STRING.equals(params.get(0))) {
            return new WildcardRoute(path);
        }
        RouteExprParser parser = new RouteExprParser(router);
        Route route = parser.parse();
        return route;
    }

    public static Parser toParser(String input) {
        String regex = "^([a-zA-Z_][a-zA-Z0-9_]*)\\(([^)]*)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String functionName = matcher.group(1);
            String parameters = matcher.group(2);
            String[] argsArray = parameters.split("\\s*,\\s*");
            return new Parser(functionName, Arrays.asList(argsArray));
        }
        return null;
    }

    public static List<Agent<ChatCompletionRequest, ChatCompletionResult>> convert2Agents(List<String> params) {
        List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents = new ArrayList<>();
        if (params.size() == 1 && RouterParser.WILDCARD_STRING.equals(params.get(0))) {
            List<Agent<?, ?>> allAgents = AgentManager.getInstance().agents();
            for (Agent<?, ?> agent : allAgents) {
                String appId = agent.getAgentConfig().getName();
                if (appId != null) {
                    try {
                        agents.add((Agent<ChatCompletionRequest, ChatCompletionResult>) agent);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        if (params.size() > 1) {
            for (String param : params) {
                Parser parser = RouterParser.toParser(param);
                Agent<ChatCompletionRequest, ChatCompletionResult> agent = null;
                if (parser == null) {
                    agent = (Agent<ChatCompletionRequest, ChatCompletionResult>) AgentManager.getInstance().get(param);
                } else {
                    String name = parser.getName();
                    List<String> args = parser.getArgs();
                    if ("chat".equals(name)) {
                        agent = new LlmProxyAgent(args.get(0));
                    }
                }
                if (agent != null) {
                    agents.add(agent);
                }
            }
        }
        return agents;
    }

    public static void main(String[] args) {
        System.out.println(toParser("test"));
    }
}
