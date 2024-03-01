package ai.migrate.pojo;

import java.util.List;

public class KeywordMigrateInfo {
	private String aspect;
	private String nodeTable;
	private List<String> keywords;

	public String getAspect() {
		return aspect;
	}

	public void setAspect(String aspect) {
		this.aspect = aspect;
	}

	public String getNodeTable() {
		return nodeTable;
	}

	public void setNodeTable(String nodeTable) {
		this.nodeTable = nodeTable;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
}
