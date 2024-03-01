package ai.migrate.pojo;

import java.util.List;
import java.util.Map;

public class ExcelMigrateInfo {
	private String fileName;
	private String sheet;
	private String columnName;
	private String aspect;
	private Map<String, List<Map<String, String>>> data;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSheet() {
		return sheet;
	}

	public void setSheet(String sheet) {
		this.sheet = sheet;
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

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	@Override
	public String toString() {
		return "ExcelMigrateInfo [fileName=" + fileName + ", sheet=" + sheet + ", aspect=" + aspect + ", data=" + data + "]";
	}

}
