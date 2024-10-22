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
        String sql = "INSERT INTO lagi_upload_file (file_id, filename, filepath, category) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, entity.getFileId());
        ps.setString(2, entity.getFilename());
        ps.setString(3, entity.getFilepath());
        ps.setString(4, entity.getCategory());
        result = ps.executeUpdate();
        BaseIndex.closeConnection(ps, conn);
        return result;
    }
    
    public UploadFile getUploadFileList(String fileId, Conn conn) throws SQLException {
        UploadFile result = null;
        String sql = "select file_id, filename, filepath, category from lagi_upload_file where file_id = ?";
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
        }
        BaseIndex.closeConnection(rs, ps);
        return result;
    }
    
    public List<UploadFile> getUploadFileList(int pageNumber, int pageSize) throws SQLException {
        IConn conn = new Conn();
        List<UploadFile> result = new ArrayList<>();
        String sql = "select file_id, filename, filepath, category from lagi_upload_file limit ?,?";
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
            result.add(entity);
        }
        BaseIndex.closeConnection(rs, ps, conn);
        return result;
    }

    public List<UploadFile> getUploadFileList(int pageNumber, int pageSize, String category) throws SQLException {
        IConn conn = new Conn();
        List<UploadFile> result = new ArrayList<>();
        String sql = "select file_id, filename, filepath, category from lagi_upload_file where category=? limit ?,?";
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
    
    public int getTotalRow(String category) throws SQLException {
        Conn conn = new Conn();
        int result = -1;
        String sql = "SELECT count(*) FROM lagi_upload_file where category=?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = conn.prepareStatement(sql);
        ps.setString(1, category);
        rs = ps.executeQuery();
        while (rs.next()) {
            result = rs.getInt(1);
        }
        BaseIndex.closeConnection(rs, ps, conn);
        return result;
    }
}
