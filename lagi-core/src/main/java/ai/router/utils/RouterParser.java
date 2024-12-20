package ai.router.utils;

import ai.common.exception.RRException;
import ai.router.*;

import java.util.*;
import java.util.stream.Collectors;

public class RouterParser {

    public static final char GROUP_START = '(';
    public static final char GROUP_END = ')';

    public static final char POLLING_SEPARATOR = '|';
    public static final char PARALLEL_SEPARATOR = '&';
    public static final char FAILOVER_SEPARATOR = ',';

    public static final String POLLING_SEPARATOR_STRING = POLLING_SEPARATOR + "";
    public static final String PARALLEL_SEPARATOR_STRING = PARALLEL_SEPARATOR + "";
    public static final String FAILOVER_SEPARATOR_STRING = FAILOVER_SEPARATOR + "";

    public static final char WILDCARD = '%';

    public static final String WILDCARD_STRING = WILDCARD + "";

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

    public static String getRuleName(String route) {
        int i = route.indexOf("(");
        return route.substring(0, i);
    }

    public static List<String> getParams(String route) {
        int s = route.indexOf("(");
        int e = route.indexOf(")");
        return Arrays.stream(route.substring(s + 1, e).split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }


    public static Route parse(String path, String router) {
        if(!checkValid(router)) {
            throw new RRException(400, "Invalid router rule");
        }
        List<String> params = RouterParser.getParams(router);
        if(params.size() == 1 && WILDCARD_STRING.equals(params.get(0))) {
            return new WildcardRoute(path);
        }
        // only support one rule
        boolean contains = router.contains(POLLING_SEPARATOR_STRING);
        boolean contains1 = router.contains(PARALLEL_SEPARATOR_STRING);
        boolean contains2 = router.contains(FAILOVER_SEPARATOR_STRING);
        if(contains && !contains1 && !contains2) {
            return new PollingRoute(path);
        }
        if(contains1 && !contains && !contains2) {
            return new ParallelRoute(path);
        }
        if(contains2 && !contains && !contains1) {
            return new FailOverRoute(path);
        }
        throw new RRException(400, "not support rule");
    }


    public static void main(String[] args) {
        System.out.println(parse("a", "(小信智能体,a)"));
        System.out.println(parse("a", "((a&b&c),(c|d))"));
    }


}
