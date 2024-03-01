package ai.migrate.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ai.migrate.dao.TagDao;
import ai.migrate.db.Conn;
import ai.migrate.pojo.ChannelTag;
import ai.migrate.pojo.TagEntity;

public class TagService {
	private TagDao tagDao = new TagDao();
	
	
	public List<TagEntity> getChannelTag(int channelId) throws SQLException {
		return tagDao.getChannelTag(channelId);
	}
	
	public boolean saveChannelTag(ChannelTag channelTag) {
		boolean result = false;
		Conn conn = new Conn();
		try {
			conn.setAutoCommit(false);
			tagDao.deleteChannelTag(channelTag.getChannelId(), conn);
			for (Integer tagId: channelTag.getTagIdList()) {
				tagDao.addChannelTag(channelTag.getChannelId(), tagId, conn);
			}
			conn.commit();
			result = true;
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			conn.close();
		}
		return result;
	}
	
	public String getSelectedChannelTag(int channelId) {
		Map<String, List<TagEntity>> selectedTagMap = new HashMap<>();
		try {
			selectedTagMap = tagDao.getSelectedChannelTag(channelId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		for (Entry<String, List<TagEntity>> entry: selectedTagMap.entrySet()) {
			String tagTypeName = entry.getKey();
			List<TagEntity> tagList = entry.getValue();
			sb.append(tagTypeName).append(": ");
			for (TagEntity tagEntity : tagList) {
				sb.append(tagEntity.getTagName()).append(" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
