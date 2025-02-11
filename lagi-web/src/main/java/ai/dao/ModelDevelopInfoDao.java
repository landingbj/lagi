package ai.dao;
import ai.dto.ModelDevelopInfo;
import ai.migrate.db.Conn;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModelDevelopInfoDao {


    // 插入数据
    public int insert(ModelDevelopInfo modelDevelopInfo) {
        String sql = "INSERT INTO model_develop_info (user_id, model_path, template, adapter_path, finetuning_type, port, running) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try ( Conn conn = new Conn();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, modelDevelopInfo.getUserId());
            pstmt.setString(2, modelDevelopInfo.getModelPath());
            pstmt.setString(3, modelDevelopInfo.getTemplate());
            pstmt.setString(4, modelDevelopInfo.getAdapterPath());
            pstmt.setString(5, modelDevelopInfo.getFinetuningType());
            pstmt.setString(6, modelDevelopInfo.getPort());
            pstmt.setInt(7, modelDevelopInfo.getRunning());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 更新数据
    public int update(ModelDevelopInfo modelDevelopInfo) {
        String sql = "UPDATE model_develop_info SET " +
                "user_id = ?, model_path = ?, template = ?, adapter_path = ?, finetuning_type = ?, port = ?, running = ? " +
                "WHERE id = ?";
        try ( Conn conn = new Conn();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, modelDevelopInfo.getUserId());
            pstmt.setString(2, modelDevelopInfo.getModelPath());
            pstmt.setString(3, modelDevelopInfo.getTemplate());
            pstmt.setString(4, modelDevelopInfo.getAdapterPath());
            pstmt.setString(5, modelDevelopInfo.getFinetuningType());
            pstmt.setString(6, modelDevelopInfo.getPort());
            pstmt.setInt(7, modelDevelopInfo.getRunning());
            pstmt.setInt(8, modelDevelopInfo.getId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 删除数据
    public int delete(int id) {
        String sql = "DELETE FROM model_develop_info WHERE id = ?";
        try ( Conn conn = new Conn();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 查询所有数据
    public List<ModelDevelopInfo> findAll() {
        String sql = "SELECT * FROM model_develop_info";
        List<ModelDevelopInfo> modelDevelopInfos = new ArrayList<>();
        try ( Conn conn = new Conn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ModelDevelopInfo modelDevelopInfo = new ModelDevelopInfo();
                modelDevelopInfo.setId(rs.getInt("id"));
                modelDevelopInfo.setUserId(rs.getString("user_id"));
                modelDevelopInfo.setModelPath(rs.getString("model_path"));
                modelDevelopInfo.setTemplate(rs.getString("template"));
                modelDevelopInfo.setAdapterPath(rs.getString("adapter_path"));
                modelDevelopInfo.setFinetuningType(rs.getString("finetuning_type"));
                modelDevelopInfo.setPort(rs.getString("port"));
                modelDevelopInfo.setRunning(rs.getInt("running"));
                modelDevelopInfos.add(modelDevelopInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modelDevelopInfos;
    }

    // 根据ID查询数据
    public List<ModelDevelopInfo> findByUserId(String  userId) {
        String sql = "SELECT * FROM model_develop_info WHERE user_id = ?";
        List<ModelDevelopInfo> modelDevelopInfos = new ArrayList<>();
        try ( Conn conn = new Conn();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ModelDevelopInfo modelDevelopInfo = new ModelDevelopInfo();
                modelDevelopInfo.setId(rs.getInt("id"));
                modelDevelopInfo.setUserId(rs.getString("user_id"));
                modelDevelopInfo.setModelPath(rs.getString("model_path"));
                modelDevelopInfo.setTemplate(rs.getString("template"));
                modelDevelopInfo.setAdapterPath(rs.getString("adapter_path"));
                modelDevelopInfo.setFinetuningType(rs.getString("finetuning_type"));
                modelDevelopInfo.setPort(rs.getString("port"));
                modelDevelopInfo.setRunning(rs.getInt("running"));
                modelDevelopInfos.add(modelDevelopInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modelDevelopInfos;
    }

    public ModelDevelopInfo findById(Integer id) {
        String sql = "SELECT * FROM model_develop_info WHERE id = ?";
        ModelDevelopInfo modelDevelopInfo = null;
        try ( Conn conn = new Conn();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                modelDevelopInfo = new ModelDevelopInfo();
                modelDevelopInfo.setId(rs.getInt("id"));
                modelDevelopInfo.setUserId(rs.getString("user_id"));
                modelDevelopInfo.setModelPath(rs.getString("model_path"));
                modelDevelopInfo.setTemplate(rs.getString("template"));
                modelDevelopInfo.setAdapterPath(rs.getString("adapter_path"));
                modelDevelopInfo.setFinetuningType(rs.getString("finetuning_type"));
                modelDevelopInfo.setPort(rs.getString("port"));
                modelDevelopInfo.setRunning(rs.getInt("running"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modelDevelopInfo;
    }

    public int countPort(String port) {
        String sql = "SELECT count(1) FROM model_develop_info WHERE port = ? and running=1";
        int count = 0;
        try ( Conn conn = new Conn();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, port);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }


    public List<String> runningPort(String port) {
        String sql = "SELECT port FROM model_develop_info WHERE port = ? and running= 1";
        List<String> ports = new ArrayList<>();
        try ( Conn conn = new Conn();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, port);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ports.add(rs.getString("port"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ports;
    }
}
