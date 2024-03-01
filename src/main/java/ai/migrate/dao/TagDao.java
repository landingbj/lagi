package ai.migrate.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.index.BaseIndex;
import ai.migrate.db.Conn;
import ai.migrate.pojo.ChannelTag;
import ai.migrate.pojo.TagEntity;

public class TagDao {
	public List<TagEntity> getChannelTag(int channelId)  throws SQLException {
		List<TagEntity> result = new ArrayList<>();
		Conn conn = new Conn();
		String sql = "SELECT A.id as id, A.name as tag_name, A.type_id as type_id, C.name AS type_name, "
				+ "IF(B.tag_id IS NULL, 0, 1) as status FROM ld_media_tag AS A LEFT JOIN ld_media_tag_channel "
				+ "AS B ON A.id = B.tag_id AND B.channel_id = ? LEFT JOIN ld_media_tag_type AS C ON A.type_id = C.id order by A.id;";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ps = conn.prepareStatement(sql);
		ps.setInt(1, channelId);
		rs = ps.executeQuery();
		while (rs.next()) {
			TagEntity entity = new TagEntity();
			entity.setId(rs.getInt(1));
			entity.setTagName(rs.getString(2));
			entity.setTypeId(rs.getInt(3));
			entity.setTypeName(rs.getString(4));
			entity.setStatus(rs.getInt(5));
			result.add(entity);
		}
		BaseIndex.closeConnection(rs, ps, conn);
		return result;
	}
	
	public Map<String, List<TagEntity>> getSelectedChannelTag(int channelId)  throws SQLException {
		Map<String, List<TagEntity>> result = new HashMap<>();
		Conn conn = new Conn();
		String sql = "SELECT A.id as id, A.name as tag_name, A.type_id as type_id, C.name AS type_name, "
				+ "1 as status FROM ld_media_tag AS A INNER JOIN ld_media_tag_channel AS B ON A.id = B.tag_id "
				+ "AND B.channel_id = ? LEFT JOIN ld_media_tag_type AS C ON A.type_id = C.id order by A.id;";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ps = conn.prepareStatement(sql);
		ps.setInt(1, channelId);
		rs = ps.executeQuery();
		while (rs.next()) {
			TagEntity entity = new TagEntity();
			entity.setId(rs.getInt(1));
			entity.setTagName(rs.getString(2));
			entity.setTypeId(rs.getInt(3));
			entity.setTypeName(rs.getString(4));
			entity.setStatus(rs.getInt(5));
			if(!result.containsKey(entity.getTypeName())) {
				List<TagEntity> list = new ArrayList<>();
				result.put(entity.getTypeName(), list);
			}
			result.get(entity.getTypeName()).add(entity);
		}
		BaseIndex.closeConnection(rs, ps, conn);
		return result;
	}
	
	public int deleteChannelTag(int channelId, Conn conn) throws SQLException {
		int result = -1;
		String sql = "DELETE FROM ld_media_tag_channel WHERE channel_id = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, channelId);
		result = ps.executeUpdate();
		BaseIndex.closeConnection(ps);
		return result;
	}
	
	public int addChannelTag(int channelId, int tagId, Conn conn) throws SQLException {
		int result = -1;
		String sql = "INSERT INTO ld_media_tag_channel (channel_id, tag_id) VALUES (?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, channelId);
		ps.setInt(2, tagId);
		result = ps.executeUpdate();
		BaseIndex.closeConnection(ps);
		return result;
	}
}
