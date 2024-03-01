package ai.migrate.pojo;

import java.util.List;

public class ChannelTag {
	private int channelId;
	private List<Integer> tagIdList;

	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public List<Integer> getTagIdList() {
		return tagIdList;
	}

	public void setTagIdList(List<Integer> tagIdList) {
		this.tagIdList = tagIdList;
	}

	@Override
	public String toString() {
		return "ChannelTag [channelId=" + channelId + ", tagIdList=" + tagIdList + "]";
	}

}
