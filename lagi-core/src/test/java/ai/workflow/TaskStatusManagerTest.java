package ai.workflow;

import ai.workflow.pojo.TaskReportOutput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * TaskStatusManager测试类
 */
public class TaskStatusManagerTest {
    
    @Test
    void testCreateNodeSnapshotWithNullInputs() {
        TaskStatusManager taskStatusManager = TaskStatusManager.getInstance();
        
        // 测试inputs为null的情况
        TaskReportOutput.Snapshot snapshot = taskStatusManager.createNodeSnapshot(
            "test_node", null, new HashMap<>(), "test_branch", null);
        
        assertNotNull(snapshot);
        assertEquals("test_node", snapshot.getNodeID());
        assertNotNull(snapshot.getInputs());
        assertTrue(snapshot.getInputs().isEmpty());
        assertNotNull(snapshot.getOutputs());
        assertTrue(snapshot.getOutputs().isEmpty());
        assertEquals("test_branch", snapshot.getBranch());
        assertNull(snapshot.getError());
    }
    
    @Test
    void testCreateNodeSnapshotWithNullOutputs() {
        TaskStatusManager taskStatusManager = TaskStatusManager.getInstance();
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("test_key", "test_value");
        
        // 测试outputs为null的情况
        TaskReportOutput.Snapshot snapshot = taskStatusManager.createNodeSnapshot(
            "test_node", inputs, null, "test_branch", null);
        
        assertNotNull(snapshot);
        assertEquals("test_node", snapshot.getNodeID());
        assertNotNull(snapshot.getInputs());
        assertEquals(1, snapshot.getInputs().size());
        assertEquals("test_value", snapshot.getInputs().get("test_key"));
        assertNotNull(snapshot.getOutputs());
        assertTrue(snapshot.getOutputs().isEmpty());
        assertEquals("test_branch", snapshot.getBranch());
        assertNull(snapshot.getError());
    }
    
    @Test
    void testCreateNodeSnapshotWithBothNull() {
        TaskStatusManager taskStatusManager = TaskStatusManager.getInstance();
        
        // 测试inputs和outputs都为null的情况
        TaskReportOutput.Snapshot snapshot = taskStatusManager.createNodeSnapshot(
            "test_node", null, null, "test_branch", null);
        
        assertNotNull(snapshot);
        assertEquals("test_node", snapshot.getNodeID());
        assertNotNull(snapshot.getInputs());
        assertTrue(snapshot.getInputs().isEmpty());
        assertNotNull(snapshot.getOutputs());
        assertTrue(snapshot.getOutputs().isEmpty());
        assertEquals("test_branch", snapshot.getBranch());
        assertNull(snapshot.getError());
    }
    
    @Test
    void testCreateNodeSnapshotWithValidData() {
        TaskStatusManager taskStatusManager = TaskStatusManager.getInstance();
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input_key", "input_value");
        
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("output_key", "output_value");
        
        // 测试正常数据的情况
        TaskReportOutput.Snapshot snapshot = taskStatusManager.createNodeSnapshot(
            "test_node", inputs, outputs, "test_branch", null);
        
        assertNotNull(snapshot);
        assertEquals("test_node", snapshot.getNodeID());
        assertNotNull(snapshot.getInputs());
        assertEquals(1, snapshot.getInputs().size());
        assertEquals("input_value", snapshot.getInputs().get("input_key"));
        assertNotNull(snapshot.getOutputs());
        assertEquals(1, snapshot.getOutputs().size());
        assertEquals("output_value", snapshot.getOutputs().get("output_key"));
        assertEquals("test_branch", snapshot.getBranch());
        assertNull(snapshot.getError());
    }
} 