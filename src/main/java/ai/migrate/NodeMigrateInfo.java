package ai.migrate;

import java.util.Map;

public class NodeMigrateInfo {
	private String filename;
	private Map<String, String> migrateMap;
	private String nodeTable;

	public String getFileName() {
		return filename;
	}

	public void setFileName(String fileName) {
		this.filename = fileName;
	}

	public Map<String, String> getMigrateMap() {
		return migrateMap;
	}

	public void setMigrateMap(Map<String, String> migrateMap) {
		this.migrateMap = migrateMap;
	}

	public String getNodeTable() {
		return nodeTable;
	}

	public void setNodeTable(String nodeTable) {
		this.nodeTable = nodeTable;
	}
}
