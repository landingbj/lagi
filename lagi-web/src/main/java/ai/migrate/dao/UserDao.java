package ai.migrate.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import ai.index.BaseIndex;
import ai.migrate.db.Conn;
import ai.migrate.pojo.UserEntity;

public class UserDao {
	public int addTempCategory(UserEntity entity) throws SQLException {
	    Conn conn = new Conn();
		int result = -1;
		String sql = "INSERT INTO lagi_user (category, category_create_time) VALUES (?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, entity.getCategory());
		ps.setTimestamp(2, new java.sql.Timestamp(entity.getCategoryCreateTime().getTime()));
		result = ps.executeUpdate();
		BaseIndex.closeConnection(ps, conn);
		return result;
	}
}
