package ai.migrate.dao;

import ai.common.pojo.TraceAgentEntity;
import ai.common.pojo.TraceLlmEntity;
import ai.index.BaseIndex;
import ai.migrate.db.Conn;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TraceDao {

    public List<TraceAgentEntity> getAgentTraceList(int limit) throws SQLException {
        Conn conn = new Conn();
        List<TraceAgentEntity> result = new ArrayList<>();
        String sql = "select * from lagi_agent_trace order by count desc limit ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            TraceAgentEntity entity = TraceAgentEntity.builder()
                    .name(rs.getString("name"))
                    .agentId(rs.getInt("agent_id"))
                    .count(rs.getInt("count"))
                    .build();
            result.add(entity);
        }
        BaseIndex.closeConnection(rs, ps);
        return result;
    }

    public List<TraceLlmEntity> getLlmTraceList(int limit) throws SQLException {
        Conn conn = new Conn();
        List<TraceLlmEntity> result = new ArrayList<>();
        String sql = "select * from lagi_llm_trace order by count desc limit ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            TraceLlmEntity entity = TraceLlmEntity.builder()
                    .name(rs.getString("name"))
                    .count(rs.getInt("count"))
                    .build();
            result.add(entity);
        }
        BaseIndex.closeConnection(rs, ps);
        return result;
    }

    public int addAgentTrace(TraceAgentEntity entity) throws SQLException {
        return addAgentTrace(entity, 1);
    }

    public int addAgentTrace(TraceAgentEntity entity, int step) throws SQLException {
        Conn conn = new Conn();
        String sql = "INSERT INTO lagi_agent_trace(name, agent_id) " +
                "VALUES (?, ?) ON CONFLICT(name, agent_id) DO UPDATE SET count = count + ?;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, entity.getName());
        ps.setInt(2, entity.getAgentId());
        ps.setInt(3, step);
        int result = ps.executeUpdate();
        BaseIndex.closeConnection(null, ps);
        return result;
    }

    public int addLlmTrace(TraceLlmEntity entity) throws SQLException {
        return addLlmTrace(entity, 1);
    }

    public int addLlmTrace(TraceLlmEntity entity, int step) throws SQLException {
        Conn conn = new Conn();
        String sql = "INSERT INTO lagi_llm_trace(name) " +
                "VALUES (?) ON CONFLICT(name) DO UPDATE SET count = count + ?;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, entity.getName());
        ps.setInt(2, step);
        int result = ps.executeUpdate();
        BaseIndex.closeConnection(null, ps);
        return result;
    }
}
