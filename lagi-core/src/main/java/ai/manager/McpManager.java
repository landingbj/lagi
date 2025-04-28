package ai.manager;

import ai.common.pojo.McpBackend;
import ai.mcps.CommonSseMcpClient;
import ai.mcps.SyncMcpClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class McpManager {
    @Getter
    private static McpManager instance = new McpManager();
    private McpManager(){}
    private final Map<String, McpBackend> mcpBackendsMap = new ConcurrentHashMap<>();


    public void register(List<McpBackend> mcpBackends)
    {
        if(mcpBackends == null) {
            return;
        }
        for (McpBackend mcpBackend : mcpBackends) {
            register(mcpBackend);
        }
    }

    public void register(McpBackend mcpBackend)
    {
        McpBackend mcpBackend1 = mcpBackendsMap.putIfAbsent(mcpBackend.getName(), mcpBackend);
        if(mcpBackend1 != null) {
            log.error("mcpBackend ({}) already exists", mcpBackend.getName());
        }
    }

    public SyncMcpClient getNewMcpClient(String name)
    {
        McpBackend mcpBackend = mcpBackendsMap.get(name);
        String driver = mcpBackend.getDriver();
        if(driver == null) {
            return new CommonSseMcpClient(mcpBackend);
        }
        try {
            Constructor<?> constructor = Class.forName(driver).getConstructor(McpBackend.class);
            return (SyncMcpClient) constructor.newInstance(mcpBackend);
        } catch (Exception e) {
            log.error("get ({})error", driver);
        }
        return null;
    }

    public List<McpBackend> getMcpBackends() {
        return new ArrayList<>(mcpBackendsMap.values()).stream()
                .sorted(Comparator.comparing(McpBackend::getPriority))
                .collect(Collectors.toList());
    }

}
