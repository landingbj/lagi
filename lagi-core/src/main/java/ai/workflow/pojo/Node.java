package ai.workflow.pojo;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * 工作流节点
 */
public class Node {
    private final String id;
    private final String type;
    private JsonNode data;
    private JsonNode meta;
    private List<Node> blocks;
    private List<Edge> blockEdges;
    
    public Node(String id, String type) {
        this.id = id;
        this.type = type;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public String getType() { return type; }
    public JsonNode getData() { return data; }
    public void setData(JsonNode data) { this.data = data; }
    public JsonNode getMeta() { return meta; }
    public void setMeta(JsonNode meta) { this.meta = meta; }
    public List<Node> getBlocks() { return blocks; }
    public void setBlocks(List<Node> blocks) { this.blocks = blocks; }
    public List<Edge> getBlockEdges() { return blockEdges; }
    public void setBlockEdges(List<Edge> blockEdges) { this.blockEdges = blockEdges; }
}