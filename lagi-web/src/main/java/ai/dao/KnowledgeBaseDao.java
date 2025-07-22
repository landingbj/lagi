package ai.dao;

import ai.common.pojo.KnowledgeBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class KnowledgeBaseDao {

    private Connection connection;

    public KnowledgeBaseDao(Connection connection) {
        this.connection = connection;
    }

    // 插入知识库
    public boolean insert(KnowledgeBase kb) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO knowledge_base (user_id");
        StringBuilder placeholders = new StringBuilder("?");
        List<Object> params = new ArrayList<>();
        params.add(kb.getUserId());

        if (kb.getName() != null) {
            sql.append(", name");
            placeholders.append(", ?");
            params.add(kb.getName());
        }
        if (kb.getDescription() != null) {
            sql.append(", description");
            placeholders.append(", ?");
            params.add(kb.getDescription());
        }
        if (kb.getRegion() != null) {
            sql.append(", region");
            placeholders.append(", ?");
            params.add(kb.getRegion());
        }
        if (kb.getCategory() != null) {
            sql.append(", category");
            placeholders.append(", ?");
            params.add(kb.getCategory());
        }
        if (kb.getSettingsId() != null) {
            sql.append(", settings_id");
            placeholders.append(", ?");
            params.add(kb.getSettingsId().intValue());
        }
        if (kb.isPublic()) {
            sql.append(", is_public");
            placeholders.append(", ?");
            params.add(kb.isPublic() ? 1 : 0);
        }
        if (kb.getEnableFulltext()) {
            sql.append(", enable_fulltext");
            placeholders.append(", ?");
            params.add(kb.getEnableFulltext() ? 1 : 0);
        }
        if (kb.getEnableGraph()) {
            sql.append(", enable_graph");
            placeholders.append(", ?");
            params.add(kb.getEnableGraph() ? 1 : 0);
        }
        if (kb.getEnableText2qa()) {
            sql.append(", enable_text2qa");
            placeholders.append(", ?");
            params.add(kb.getEnableText2qa() ? 1 : 0);
        }
        if (kb.getWenbenChunkSize() > 0) {
            sql.append(", wenben_chunk_size");
            placeholders.append(", ?");
            params.add(kb.getWenbenChunkSize());
        }
        if (kb.getBiaogeChunkSize() > 0) {
            sql.append(", biaoge_chunk_size");
            placeholders.append(", ?");
            params.add(kb.getBiaogeChunkSize());
        }
        if (kb.getTuwenChunkSize() > 0) {
            sql.append(", tuwen_chunk_size");
            placeholders.append(", ?");
            params.add(kb.getTuwenChunkSize());
        }
        if (kb.getSimilarityTopK() > 0) {
            sql.append(", similarity_top_k");
            placeholders.append(", ?");
            params.add(kb.getSimilarityTopK());
        }
        if (kb.getSimilarityCutoff() > 0) {
            sql.append(", similarity_cutoff");
            placeholders.append(", ?");
            params.add(kb.getSimilarityCutoff());
        }
        if (kb.getCreateTime() > 0) {
            sql.append(", create_time");
            placeholders.append(", ?");
            params.add(kb.getCreateTime());
        }
        if (kb.getUpdateTime() > 0) {
            sql.append(", update_time");
            placeholders.append(", ?");
            params.add(kb.getUpdateTime());
        }

        sql.append(") VALUES (").append(placeholders).append(")");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            return stmt.executeUpdate() > 0;
        }
    }


    // 查询所有知识库
    public List<KnowledgeBase> getByUserRegion(String userId, String region) throws SQLException {
        String sql = "SELECT * FROM knowledge_base WHERE user_id = ? and region = ?";
        List<KnowledgeBase> result = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, region);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(mapToKnowledgeBase(rs));
            }
        }

        return result;
    }

    // 查询单个知识库
    public KnowledgeBase getById(Long id) throws SQLException {
        String sql = "SELECT * FROM knowledge_base WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapToKnowledgeBase(rs);
            }
        }
        return null;
    }

    // 删除知识库
    public boolean deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM knowledge_base WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // 更新知识库
    public boolean update(KnowledgeBase kb) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE knowledge_base SET ");
        List<Object> params = new ArrayList<>();

        if (kb.getName() != null) {
            sql.append("name = ?, ");
            params.add(kb.getName());
        }
        if (kb.getDescription() != null) {
            sql.append("description = ?, ");
            params.add(kb.getDescription());
        }
        if (kb.getRegion() != null) {
            sql.append("region = ?, ");
            params.add(kb.getRegion());
        }
        if (kb.getCategory() != null) {
            sql.append("category = ?, ");
            params.add(kb.getCategory());
        }
        if (kb.getSettingsId() != null) {
            sql.append("settings_id = ?, ");
            params.add(kb.getSettingsId().intValue());
        }
        if (kb.isPublic()) {
            sql.append("is_public = ?, ");
            params.add(kb.isPublic() ? 1 : 0);
        }
        if (kb.getEnableFulltext()) {
            sql.append("enable_fulltext = ?, ");
            params.add(kb.getEnableFulltext() ? 1 : 0);
        }
        if (kb.getEnableGraph()) {
            sql.append("enable_graph = ?, ");
            params.add(kb.getEnableGraph() ? 1 : 0);
        }
        if (kb.getEnableText2qa()) {
            sql.append("enable_text2qa = ?, ");
            params.add(kb.getEnableText2qa() ? 1 : 0);
        }
        if (kb.getWenbenChunkSize() > 0) {
            sql.append("wenben_chunk_size = ?, ");
            params.add(kb.getWenbenChunkSize());
        }
        if (kb.getBiaogeChunkSize() > 0) {
            sql.append("biaoge_chunk_size = ?, ");
            params.add(kb.getBiaogeChunkSize());
        }
        if (kb.getTuwenChunkSize() > 0) {
            sql.append("tuwen_chunk_size = ?, ");
            params.add(kb.getTuwenChunkSize());
        }
        if (kb.getSimilarityTopK() > 0) {
            sql.append("similarity_top_k = ?, ");
            params.add(kb.getSimilarityTopK());
        }
        if (kb.getSimilarityCutoff() > 0) {
            sql.append("similarity_cutoff = ?, ");
            params.add(kb.getSimilarityCutoff());
        }
        sql.append("update_time = ? WHERE id = ?");
        params.add(kb.getUpdateTime());
        params.add(kb.getId());

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            return stmt.executeUpdate() > 0;
        }
    }


    // 将 ResultSet 映射为 KnowledgeBase
    private KnowledgeBase mapToKnowledgeBase(ResultSet rs) throws SQLException {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(rs.getLong("id"));
        kb.setUserId(rs.getString("user_id"));
        kb.setName(rs.getString("name"));
        kb.setDescription(rs.getString("description"));
        kb.setRegion(rs.getString("region"));
        kb.setCategory(rs.getString("category"));
        kb.setSettingsId(Long.valueOf(rs.getInt("settings_id")));
        kb.setPublic(rs.getInt("is_public") == 1);
        kb.setEnableFulltext(rs.getInt("enable_fulltext") == 1);
        kb.setEnableGraph(rs.getInt("enable_graph") == 1);
        kb.setEnableText2qa(rs.getInt("enable_text2qa") == 1);
        kb.setWenbenChunkSize(rs.getInt("wenben_chunk_size"));
        kb.setBiaogeChunkSize(rs.getInt("biaoge_chunk_size"));
        kb.setTuwenChunkSize(rs.getInt("tuwen_chunk_size"));
        kb.setSimilarityTopK(rs.getInt("similarity_top_k"));
        kb.setSimilarityCutoff(rs.getDouble("similarity_cutoff"));
        kb.setCreateTime(rs.getLong("create_time"));
        kb.setUpdateTime(rs.getLong("update_time"));
        return kb;
    }


    // 使其他 useId 和 region 相同的 is_public 为 1 的记录设为 0
    public boolean unPublicOtherKnowledgeBase(String useId, String region) throws SQLException {
        String sql = "UPDATE knowledge_base SET is_public = 0 WHERE user_id = ? AND region = ? AND is_public = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, useId);
            stmt.setString(2, region);
            return stmt.executeUpdate() > 0;
        }
    }

    // 查询指定 useId 和 region 下最新的 is_public 为 1 的 KnowledgeBase
    public KnowledgeBase getLatestPublicKnowledgeBase(String useId, String region) throws SQLException {
        String sql = "SELECT * FROM knowledge_base WHERE user_id = ? AND region = ? AND is_public = 1 ORDER BY create_time DESC LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, useId);
            stmt.setString(2, region);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapToKnowledgeBase(rs);
            }
        }
        return null;
    }

    public KnowledgeBase getFirstKnowledgeBase(String useId, String region) throws SQLException {
        String sql = "SELECT * FROM knowledge_base WHERE user_id = ? AND region = ? ORDER BY create_time ASC LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, useId);
            stmt.setString(2, region);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapToKnowledgeBase(rs);
            }
        }
        return null;
    }

}
