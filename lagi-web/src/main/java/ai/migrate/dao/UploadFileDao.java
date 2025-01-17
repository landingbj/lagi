package ai.migrate.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ai.dao.IConn;
import ai.index.BaseIndex;
import ai.migrate.db.Conn;
import ai.common.pojo.UploadFile;

public class UploadFileDao {
    public int deleteUploadFile(String category) throws SQLException {
        IConn conn = new Conn();
        int result = -1;
        String sql = "DELETE FROM lagi_upload_file WHERE category = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, category);
        result = ps.executeUpdate();
        BaseIndex.closeConnection(ps);
        return result;
    }

    public int deleteUploadFile(String fileId, Conn conn) throws SQLException {
        int result = -1;
        String sql = "DELETE FROM lagi_upload_file WHERE file_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, fileId);
        result = ps.executeUpdate();
        BaseIndex.closeConnection(ps);
        return result;
    }

    public int addUploadFile(UploadFile entity) throws SQLException {
        IConn conn = new Conn();
        int result = -1;
        String sql = "INSERT INTO lagi_upload_file (file_id, filename, filepath, category, create_time, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, entity.getFileId());
        ps.setString(2, entity.getFilename());
        ps.setString(3, entity.getFilepath());
        ps.setString(4, entity.getCategory());
        ps.setLong(5, entity.getCreateTime());
        ps.setString(6, entity.getUserId());
        result = ps.executeUpdate();
        BaseIndex.closeConnection(ps, conn);
        return result;
    }
    
    public UploadFile getUploadFileList(String fileId, Conn conn) throws SQLException {
        UploadFile result = null;
        String sql = "select file_id, filename, filepath, category, create_time from lagi_upload_file where file_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = conn.prepareStatement(sql);
        ps.setString(1, fileId);
        rs = ps.executeQuery();
        while (rs.next()) {
            result = new UploadFile();
            result.setFileId(rs.getString(1));
            result.setFilename(rs.getString(2));
            result.setFilepath(rs.getString(3));
            result.setCategory(rs.getString(4));
            result.setCreateTime(rs.getLong(5));
        }
        BaseIndex.closeConnection(rs, ps);
        return result;
    }
    
    public List<UploadFile> getUploadFileList(int pageNumber, int pageSize) throws SQLException {
        IConn conn = new Conn();
        List<UploadFile> result = new ArrayList<>();
        String sql = "select file_id, filename, filepath, category, create_time from lagi_upload_file limit ?,?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = conn.prepareStatement(sql);
        ps.setInt(1, (pageNumber - 1) * pageSize);
        ps.setInt(2, pageSize);
        rs = ps.executeQuery();
        while (rs.next()) {
            UploadFile entity = new UploadFile();
            entity.setFileId(rs.getString(1));
            entity.setFilename(rs.getString(2));
            entity.setFilepath(rs.getString(3));
            entity.setCategory(rs.getString(4));
            entity.setCreateTime(rs.getLong(5));
            result.add(entity);
        }
        BaseIndex.closeConnection(rs, ps, conn);
        return result;
    }

    public List<UploadFile> getUploadFileList(int pageNumber, int pageSize, String category) throws SQLException {
        IConn conn = new Conn();
        List<UploadFile> result = new ArrayList<>();
        String sql = "select file_id, filename, filepath, category, create_time from lagi_upload_file where category=? limit ?,?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = conn.prepareStatement(sql);
        ps.setString(1, category);
        ps.setInt(2, (pageNumber - 1) * pageSize);
        ps.setInt(3, pageSize);
        rs = ps.executeQuery();
        while (rs.next()) {
            UploadFile entity = new UploadFile();
            entity.setFileId(rs.getString(1));
            entity.setFilename(rs.getString(2));
            entity.setFilepath(rs.getString(3));
            entity.setCategory(rs.getString(4));
            entity.setCreateTime(rs.getLong(5));
            result.add(entity);
        }
        BaseIndex.closeConnection(rs, ps, conn);
        return result;
    }


    public List<UploadFile> getUploadFileList(int pageNumber, int pageSize, String category, String userId) throws SQLException {
        IConn conn = new Conn();
        List<UploadFile> result = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select file_id, filename, filepath, category, create_time, user_id from lagi_upload_file where 1=1 ");
        if(category != null) {
            stringBuilder.append("and  category=? ");
        }
        if(userId != null) {
            stringBuilder.append("and  user_id=? ");
        }
        stringBuilder.append("limit ?,?");
        String sql = stringBuilder.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = conn.prepareStatement(sql);
        int i = 1;
        if(category != null) {
            ps.setString(i++, category);
        }
        if(userId != null) {
            ps.setString(i++, userId);
        }
        ps.setInt(i++, (pageNumber - 1) * pageSize);
        ps.setInt(i, pageSize);
        rs = ps.executeQuery();
        while (rs.next()) {
            UploadFile entity = new UploadFile();
            entity.setFileId(rs.getString(1));
            entity.setFilename(rs.getString(2));
            entity.setFilepath(rs.getString(3));
            entity.setCategory(rs.getString(4));
            entity.setCreateTime(rs.getLong(5));
            entity.setUserId(rs.getString(6));
            result.add(entity);
        }
        BaseIndex.closeConnection(rs, ps, conn);
        return result;
    }


    public int getTotalRow() throws SQLException {
        Conn conn = new Conn();
        int result = -1;
        String sql = "SELECT count(*) FROM lagi_upload_file";
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = conn.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            result = rs.getInt(1);
        }
        BaseIndex.closeConnection(rs, ps, conn);
        return result;
    }
    
    public int getTotalRow(String category, String userId) throws SQLException {
        Conn conn = new Conn();
        int result = -1;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT count(*) FROM lagi_upload_file where 1=1 ");
        if(category != null) {
            stringBuilder.append("and  category=? ");
        }
        if(userId != null) {
            stringBuilder.append("and  user_id=? ");
        }
        String sql = stringBuilder.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = conn.prepareStatement(sql);
        int i = 1;
        if(category != null) {
            ps.setString(i++, category);
        }
        if(userId != null) {
            ps.setString(i++, userId);
        }
        rs = ps.executeQuery();
        while (rs.next()) {
            result = rs.getInt(1);
        }
        BaseIndex.closeConnection(rs, ps, conn);
        return result;
    }
}
