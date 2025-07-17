package ai.workflow.pojo;

import java.util.*;

/**
 * 工作流模型
 */
public class Workflow {
    private final Map<String, Node> nodes;
    private final List<Edge> edges;
    
    public Workflow() {
        this.nodes = new HashMap<>();
        this.edges = new ArrayList<>();
    }
    
    public void addNode(Node node) {
        nodes.put(node.getId(), node);
    }
    
    public void addEdge(Edge edge) {
        edges.add(edge);
    }
    
    public Node getNodeById(String id) {
        return nodes.get(id);
    }
    
    public Node getStartNode() {
        return nodes.values().stream()
            .filter(node -> "start".equals(node.getType()))
            .findFirst()
            .orElse(null);
    }
    
    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }
    
    public Collection<Node> getAllNodes() {
        return nodes.values();
    }
}