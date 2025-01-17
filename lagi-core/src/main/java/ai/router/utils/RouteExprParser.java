package ai.router.utils;

import ai.router.BasicRoute;
import ai.router.ParallelRoute;
import ai.router.PollingRoute;
import ai.router.Route;
import ai.router.FailOverRoute;

import java.util.ArrayList;
import java.util.List;

public class RouteExprParser {
    private final String input;
    private int pos;

    public RouteExprParser(String input) {
        this.input = new ExpressionParser().parseExpression(input);
        this.pos = 0;
    }

    public Route parse() {
        Route route = parseExpression();
        if (pos != input.length()) {
            throw new RuntimeException("Unexpected characters at end of input");
        }
        return route;
    }

    private Route parseExpression() {
        if (pos >= input.length()) {
            throw new RuntimeException("Unexpected end of input");
        }

        String identifier = parseIdentifier();
        if (identifier.equalsIgnoreCase("$parallel")) {
            return parseParallel();
        } else if (identifier.equalsIgnoreCase("$failover")) {
            return parseFailover();
        } else if (identifier.equalsIgnoreCase("$polling")) {
            return parsePolling();
        } else {
            return new BasicRoute(identifier);
        }
    }

    private String parseIdentifier() {
        int start = pos;
        while (pos < input.length() && (Character.isLetter(input.charAt(pos)) || input.charAt(pos) == '$')) {
            pos++;
        }
        if (start == pos) {
            throw new RuntimeException("Expected identifier at position " + pos);
        }
        return input.substring(start, pos);
    }

    private List<Route> parseArguments() {
        List<Route> args = new ArrayList<>();
        if (pos >= input.length() || input.charAt(pos) != '(') {
            throw new RuntimeException("Expected '(' at position " + pos);
        }
        pos++;
        while (pos < input.length() && input.charAt(pos) != ')') {
            args.add(parseExpression());
            if (pos < input.length() && input.charAt(pos) == ',') {
                pos++;
            }
        }
        if (pos >= input.length() || input.charAt(pos) != ')') {
            throw new RuntimeException("Expected ')' at position " + pos);
        }
        pos++;
        return args;
    }

    private ParallelRoute parseParallel() {
        return new ParallelRoute(parseArguments());
    }

    private FailOverRoute parseFailover() {
        return new FailOverRoute(parseArguments());
    }

    private PollingRoute parsePolling() {
        return new PollingRoute(parseArguments());
    }
}