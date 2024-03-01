package ai.migrate.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ai.index.BaseIndex;
import ai.migrate.db.Conn;
import ai.migrate.pojo.TermWord;
import ai.utils.MigrateGlobal;

public class SearchDao {
	public List<TermWord> searchTerm(String term) throws SQLException {
		List<TermWord> result = new ArrayList<>();
		Conn conn = new Conn(MigrateGlobal.SYZX_DB);
		term = term.replaceAll("[+-@d><()~*\"]", "").trim(); 
		String sql = "SELECT * from tm_word where match(cn) against(? IN BOOLEAN MODE) union "
				+ "SELECT * from tm_word_tw where match(cn) against(? IN BOOLEAN MODE) union "
				+ "SELECT * from tm_word_other where match(cn) against(? IN BOOLEAN MODE);";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ps = conn.prepareStatement(sql);
		ps.setString(1, term);
		ps.setString(2, term);
		ps.setString(3, term);
		rs = ps.executeQuery();
		
		while (rs.next()) {
			TermWord entity = new TermWord();
			entity.setId(rs.getString("id"));
			entity.setAreaName(rs.getString("area_name"));
			entity.setEn(rs.getString("en"));
			entity.setCn(rs.getString("cn"));
			entity.setSource(rs.getString("source"));
			entity.setSubjectName(rs.getString("subject_name"));
			entity.setDefinition(rs.getString("definition"));
			result.add(entity);
		}
		BaseIndex.closeConnection(rs, ps, conn);
		return result;
	}
}
