package ai.workflow.pojo;

/**
 * 工作流边
 */
public class Edge {
    private final String sourceNodeId;
    private final String targetNodeId;
    private final String sourcePortId;
    
    public Edge(String sourceNodeId, String targetNodeId, String sourcePortId) {
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.sourcePortId = sourcePortId;
    }
    
    public String getSourceNodeId() { return sourceNodeId; }
    public String getTargetNodeId() { return targetNodeId; }
    public String getSourcePortId() { return sourcePortId; }
}