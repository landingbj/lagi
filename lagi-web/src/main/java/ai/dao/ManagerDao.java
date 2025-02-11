package ai.dao;

import ai.dto.ManagerModel;
import ai.migrate.db.Conn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManagerDao {

    // Add ManagerModel
    public int addManagerModel(ManagerModel managerModel) {
        String sql = "INSERT INTO model_manager ( user_id, model_name, online, api_key, model_type, endpoint, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = new Conn();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, managerModel.getUserId());
            pstmt.setString(2, managerModel.getModelName());
            pstmt.setInt(3, managerModel.getOnline());
            pstmt.setString(4, managerModel.getApiKey());
            pstmt.setString(5, managerModel.getModelType());
            pstmt.setString(6, managerModel.getEndpoint());
            pstmt.setInt(7, managerModel.getStatus());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Delete ManagerModel
    public int delManagerModel(Integer id) {
        String sql = "DELETE FROM model_manager WHERE id = ?";
        try (Connection conn = new Conn();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Update ManagerModel
    public int updateManagerModel(ManagerModel managerModel) {
        String sql = "UPDATE model_manager SET user_id = ?,  model_name = ?, online = ?, api_key = ?, model_type = ?, endpoint = ?, status = ? WHERE id = ?";
        try (Connection conn = new Conn();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, managerModel.getUserId());
            pstmt.setString(2, managerModel.getModelName());
            pstmt.setInt(3, managerModel.getOnline());
            pstmt.setString(4, managerModel.getApiKey());
            pstmt.setString(5, managerModel.getModelType());
            pstmt.setString(6, managerModel.getEndpoint());
            pstmt.setInt(7, managerModel.getStatus());
            pstmt.setInt(8, managerModel.getId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get all ManagerModels
    public List<ManagerModel> getManagerModels(String userId) {
        String sql = "SELECT * FROM model_manager WHERE user_id = ?";
        List<ManagerModel> managerModels = new ArrayList<>();
        try (Connection conn = new Conn();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ManagerModel managerModel = new ManagerModel();
                managerModel.setId(rs.getInt("id"));
                managerModel.setUserId(rs.getString("user_id"));
                managerModel.setModelName(rs.getString("model_name"));
                managerModel.setOnline(rs.getInt("online"));
                managerModel.setApiKey(rs.getString("api_key"));
                managerModel.setModelType(rs.getString("model_type"));
                managerModel.setEndpoint(rs.getString("endpoint"));
                managerModel.setStatus(rs.getInt("status"));
                managerModels.add(managerModel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return managerModels;
    }


    public List<ManagerModel> getManagerModels(String userId, Integer status) {
        String sql = "SELECT * FROM model_manager WHERE user_id = ? and status = ?";
        List<ManagerModel> managerModels = new ArrayList<>();
        try (Connection conn = new Conn();
             PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, status);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ManagerModel managerModel = new ManagerModel();
                managerModel.setId(rs.getInt("id"));
                managerModel.setUserId(rs.getString("user_id"));
                managerModel.setModelName(rs.getString("model_name"));
                managerModel.setOnline(rs.getInt("online"));
                managerModel.setApiKey(rs.getString("api_key"));
                managerModel.setModelType(rs.getString("model_type"));
                managerModel.setEndpoint(rs.getString("endpoint"));
                managerModel.setStatus(rs.getInt("status"));
                managerModels.add(managerModel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return managerModels;
    }
}
