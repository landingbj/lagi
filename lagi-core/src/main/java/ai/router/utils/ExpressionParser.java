package ai.router.utils;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {
    private abstract static class Node {
        abstract String toExpression();
    }

    private static class OperatorNode extends Node {
        char operator;
        List<Node> children;

        OperatorNode(char operator) {
            this.operator = operator;
            this.children = new ArrayList<>();
        }

        @Override
        String toExpression() {
            StringBuilder sb = new StringBuilder();
            switch (operator) {
                case '&':
                    sb.append("$parallel(");
                    break;
                case '|':
                    sb.append("$polling(");
                    break;
                case ',':
                    sb.append("$failover(");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown operator: " + operator);
            }
            for (int i = 0; i < children.size(); i++) {
                sb.append(children.get(i).toExpression());
                if (i != children.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }

    private static class OperandNode extends Node {
        String value;

        OperandNode(String value) {
            this.value = value;
        }

        @Override
        String toExpression() {
            return value;
        }
    }

    private static class Parser {
        private final String input;
        private int pos;
        private final int length;

        public Parser(String input) {
            this.input = input;
            this.pos = 0;
            this.length = input.length();
        }

        public Node parse() {
            return parseFailover();
        }

        // Parse failover operations (',')
        private Node parseFailover() {
            List<Node> nodes = new ArrayList<>();
            nodes.add(parsePolling());
            while (match(',')) {
                consume(); // consume ','
                nodes.add(parsePolling());
            }
            if (nodes.size() == 1) {
                return nodes.get(0);
            } else {
                OperatorNode failoverNode = new OperatorNode(',');
                failoverNode.children.addAll(nodes);
                return failoverNode;
            }
        }

        private Node parsePolling() {
            List<Node> nodes = new ArrayList<>();
            nodes.add(parseParallel());
            while (match('|')) {
                consume(); // consume '|'
                nodes.add(parseParallel());
            }
            if (nodes.size() == 1) {
                return nodes.get(0);
            } else {
                OperatorNode pollingNode = new OperatorNode('|');
                pollingNode.children.addAll(nodes);
                return pollingNode;
            }
        }

        private Node parseParallel() {
            List<Node> nodes = new ArrayList<>();
            nodes.add(parsePrimary());
            while (match('&')) {
                consume(); // consume '&'
                nodes.add(parsePrimary());
            }
            if (nodes.size() == 1) {
                return nodes.get(0);
            } else {
                OperatorNode parallelNode = new OperatorNode('&');
                parallelNode.children.addAll(nodes);
                return parallelNode;
            }
        }

        private Node parsePrimary() {
            skipWhitespace();
            if (match('(')) {
                consume(); // consume '('
                Node node = parseFailover();
                if (!match(')')) {
                    throw new RuntimeException("Expected ')' at position " + pos);
                }
                consume(); // consume ')'
                return node;
            } else {
                return parseOperand();
            }
        }

        private Node parseOperand() {
            skipWhitespace();
            StringBuilder sb = new StringBuilder();
            while (pos < length && (Character.isLetterOrDigit(input.charAt(pos))
                    || input.charAt(pos) == '_') || input.charAt(pos) == '-' || input.charAt(pos) == ':' || input.charAt(pos) == '/'
            ) {
                sb.append(input.charAt(pos));
                pos++;
            }
            if (sb.length() == 0) {
                throw new RuntimeException("Expected operand at position " + pos);
            }
            return new OperandNode(sb.toString());
        }

        private boolean match(char c) {
            return pos < length && input.charAt(pos) == c;
        }

        private void consume() {
            pos++;
        }

        private void skipWhitespace() {
            while (pos < length && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }
    }

    // Public method to parse expression and get the formatted output
    public String parseExpression(String expr) {
        Parser parser = new Parser(extractExpression(expr));
        Node root = parser.parse();
        return root.toExpression();
    }

    private String extractExpression(String expr) {
        expr = expr.replaceAll("\\s+", "");
        int index = expr.indexOf('(');
        if (index == -1) {
            return expr;
        }
        String result = expr.substring(index);
        return result;
    }
}
