package ai.migrate.service;

import ai.common.pojo.TraceAgentEntity;
import ai.common.pojo.TraceLlmEntity;
import ai.config.ContextLoader;
import ai.llm.pojo.ChatCompletionResultWithSource;
import ai.migrate.dao.TraceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TraceService {
    private static final Logger logger = LoggerFactory.getLogger(TraceService.class);
    private static final TraceDao traceDao = new TraceDao();
    private static final Map<String, String> modelNameMap = new ConcurrentHashMap<>();

    static {
        init();
    }

    private static void init() {
        ContextLoader.configuration.getModels().forEach(backend -> {
            try {
                traceDao.addLlmTrace(TraceLlmEntity.builder()
                        .name(backend.getName())
                        .build(), 0);
            } catch (SQLException e) {
                logger.error("add llm trace error", e);
            }
            if (backend.getDriver() != null && backend.getModel() != null) {
                String[] modelNames = backend.getModel().split(",");
                for (String modelName : modelNames) {
                    modelNameMap.put(modelName.trim().toLowerCase(), backend.getName());
                }
            }
            if (backend.getDrivers() != null) {
                backend.getDrivers().forEach(driver -> {
                    String[] modelNames = driver.getModel().split(",");
                    for (String modelName : modelNames) {
                        modelNameMap.put(modelName.trim().toLowerCase(), backend.getName());
                    }
                });
            }
        });

        ContextLoader.configuration.getAgents().forEach(agent -> {
            if (agent.getId() == null) {
                return;
            }
            try {
                traceDao.addAgentTrace(TraceAgentEntity.builder()
                        .name(agent.getName())
                        .agentId(agent.getId())
                        .build(), 0);
            } catch (SQLException e) {
                logger.error("add agent trace error", e);
            }
        });
    }

    public void syncAddAgentTrace(ChatCompletionResultWithSource resultWithSource) {
        TraceAgentEntity entity = TraceAgentEntity.builder()
                .name(resultWithSource.getSource())
                .agentId(resultWithSource.getSourceId())
                .build();
        try {
            traceDao.addAgentTrace(entity);
        } catch (SQLException e) {
            logger.error("add agent trace error", e);
        }
    }

    public List<TraceAgentEntity> agentHotRanking(int limit) throws SQLException {
        return traceDao.getAgentTraceList(limit);
    }

    public List<TraceLlmEntity> llmHotRanking(int limit) throws SQLException {
        return traceDao.getLlmTraceList(limit);
    }
}
