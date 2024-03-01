package ai.migrate.pojo;

import java.util.List;
import java.util.Map;

public class DBMigrateInfo {
	private Map<String, String> dbInfo;
	private String table;
	private String fieldName;
	private String aspect;
	private Map<String, List<Map<String, String>>> data;
	
	public String getDbInfoString() {
		String host = dbInfo.get("host");
		String port =dbInfo.get("port");
		String dbName = dbInfo.get("dbName");
		String result = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
		return result;
	}

	public Map<String, String> getDbInfo() {
		return dbInfo;
	}

	public void setDbInfo(Map<String, String> dbInfo) {
		this.dbInfo = dbInfo;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getAspect() {
		return aspect;
	}

	public void setAspect(String aspect) {
		this.aspect = aspect;
	}

	public Map<String, List<Map<String, String>>> getData() {
		return data;
	}

	public void setData(Map<String, List<Map<String, String>>> data) {
		this.data = data;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public String toString() {
		return "DBMigrateInfo [dbInfo=" + dbInfo + ", table=" + table + ", aspect=" + aspect + ", data=" + data + "]";
	}
}
