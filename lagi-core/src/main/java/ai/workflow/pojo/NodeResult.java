package ai.workflow.pojo;

import java.util.List;
import java.util.Arrays;

/**
 * 节点执行结果
 */
public class NodeResult {
    private final Object data;
    private final List<String> outputPorts;
    
    public NodeResult(Object data, List<String> outputPorts) {
        this.data = data;
        this.outputPorts = outputPorts;
    }
    
    public Object getData() { return data; }
    public List<String> getOutputPorts() { return outputPorts; }
    
    // 为了向后兼容，保留获取单个输出端口的方法
    public String getOutputPort() {     return outputPorts != null && !outputPorts.isEmpty() ? outputPorts.get(0) : null; 
    }
}