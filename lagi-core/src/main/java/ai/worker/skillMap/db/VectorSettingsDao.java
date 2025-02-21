package ai.worker.skillMap.db;

import ai.common.pojo.UserRagSetting;
import ai.dao.Conn;
import ai.dao.IConn;
import ai.index.BaseIndex;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VectorSettingsDao {

    private static final String DB_URL = "jdbc:sqlite:skillMap.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UserRagSetting> getUserRagVector(String category, String userId) throws SQLException {
        List<UserRagSetting> result = new ArrayList<>();
        IConn conn = new Conn();
        String sql = "select id, user_id, file_type, category, chunk_size, temperature from user_rag_settings where category = ? and user_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = conn.prepareStatement(sql);
        ps.setString(1, category);
        ps.setString(2, userId);
        rs = ps.executeQuery();
        while (rs.next()) {
            UserRagSetting userRagSetting = new UserRagSetting();
            userRagSetting.setId(rs.getInt(1));
            userRagSetting.setUserId(rs.getString(2));
            userRagSetting.setFileType(rs.getString(3));
            userRagSetting.setCategory(rs.getString(4));
            userRagSetting.setChunkSize(rs.getInt(5));
            userRagSetting.setTemperature(rs.getDouble(6));
            result.add(userRagSetting);
        }
        BaseIndex.closeConnection(rs, ps, conn);
        return result;
    }


}
