package ai.medusa.dao;


import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TreeDiversifyDao {

    private static final String DB_URL = "jdbc:sqlite:saas.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            // 创建数据库连接
            Connection conn = DriverManager.getConnection(DB_URL);
            // 创建表
            String sql = "CREATE TABLE IF NOT EXISTS tree_diversify (\n" +
                    "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    text VARCHAR(255) NOT NULL UNIQUE\n" +
                    ");";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            sql = "CREATE TABLE IF NOT EXISTS tree_diversify_relations (\n" +
                    "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    parent_id INT NOT NULL,\n" +
                    "    child_id INT NOT NULL,\n" +
                    "    hitCount INT NOT NULL DEFAULT 0,\n" +
                    "    UNIQUE (parent_id, child_id)\n" +
                    ");";
            pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            log.error("Error connecting to database", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> searchChildNode(String nodeText, int top) {
        List<String> result = new ArrayList<>();
        Integer parentId = getIdByText(nodeText);
        if(parentId == null) {
            return result;
        }
        String query = "SELECT td.text\n" +
                "   FROM tree_diversify td\n" +
                "   JOIN (\n" +
                "       SELECT child_id\n" +
                "       FROM tree_diversify_relations\n" +
                "       WHERE parent_id = ?\n" +
                "       ORDER BY hitCount DESC\n" +
                "       LIMIT ?\n" +
                "   ) tdr ON td.id = tdr.child_id;";
        try (PreparedStatement ps = DriverManager.getConnection(DB_URL).prepareStatement(query)) {
            ps.setInt(1, parentId);
            ps.setInt(2, top);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("text"));
            }
        } catch (SQLException e) {
            log.error("Error executing SQL query", e);
        }
        return result;
    }



    public void saveGraphNode(String text) {

        String query = "INSERT INTO tree_diversify (text) VALUES (?) ON CONFLICT(text) DO NOTHING";
        try (PreparedStatement ps = DriverManager.getConnection(DB_URL).prepareStatement(query)) {
            ps.setString(1, text);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error executing SQL query", e);
        }
    }

    public void saveGraphNodeAndRelation(String text, String lastText) {
        saveGraphNode(text);
//        saveGraphNode(lastText);

        Integer textId = getIdByText(text);
        if(textId == null) {
            log.error("can not find graph node by text {}", text);
            return;
        }
        Integer lastTextId = getIdByText(lastText);

        if(lastTextId == null) {
            log.error("can not find parent node  {}", lastText);
            return;
        }

        String query = "INSERT INTO tree_diversify_relations (parent_id, child_id, hitCount) VALUES (?, ?, 1) ON CONFLICT(parent_id, child_id) DO UPDATE SET hitCount = hitCount + 1";
        try (PreparedStatement ps = DriverManager.getConnection(DB_URL).prepareStatement(query)) {
            ps.setInt(1, lastTextId);
            ps.setInt(2, textId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error executing SQL query", e);
        }

    }


    public void saveRelation(Integer pId, Integer cId) {

        String query = "INSERT INTO tree_diversify_relations (parent_id, child_id, hitCount) VALUES (?, ?, 1) ON CONFLICT(parent_id, child_id) DO UPDATE SET hitCount = hitCount + 1";
        try (PreparedStatement ps = DriverManager.getConnection(DB_URL).prepareStatement(query)) {
            ps.setInt(1, pId);
            ps.setInt(2, cId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error executing SQL query", e);
        }

    }

    public Integer getIdByText(String text) {
        String query = "SELECT id FROM tree_diversify WHERE text = ?";
        try (PreparedStatement ps = DriverManager.getConnection(DB_URL).prepareStatement(query)) {
            ps.setString(1, text);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            log.error("Error executing SQL query", e);
        }
        return null;
    }

    public List<String> getAllNodeTexts() {
        List<String> res = new ArrayList<>();
        String query = "SELECT text FROM tree_diversify order by id";
        try (PreparedStatement ps = DriverManager.getConnection(DB_URL).prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                res.add(rs.getString("text"));
            }
        } catch (SQLException e) {
            log.error("Error executing SQL query", e);
        }
        return res;
    }


    public boolean hasRelation(Integer parentId, Integer childId) {
        boolean result = false;
        String query = "select count(1) as c from tree_diversify_relations where parent_id = ? and child_id = ?";
        try (PreparedStatement ps = DriverManager.getConnection(DB_URL).prepareStatement(query)) {
            ps.setInt(1, parentId);
            ps.setInt(2, childId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int c = rs.getInt("c");
                if(c > 0) {
                    result = true;
                    break;
                }
            }
        } catch (SQLException e) {
            log.error("Error executing SQL query", e);
        }
        return result;
    }

    public static void main(String[] args) {
        TreeDiversifyDao treeDiversifyDao = new TreeDiversifyDao();
//        treeDiversifyDao.saveGraphNode("你好");
//        treeDiversifyDao.saveGraphNodeAndRelation("你是谁", "你好");
//        List<String> strings = treeDiversifyDao.searchChildNode("你好", 1);
        List<String> strings = treeDiversifyDao.searchChildNode("你好", 1);
        System.out.println(strings);
    }
}
