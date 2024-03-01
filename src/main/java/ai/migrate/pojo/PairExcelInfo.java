package ai.migrate.pojo;

public class PairExcelInfo {
	private String fileName;
	private String sheet;
	private String qColumn;
	private String aColumn;

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

	public String getQColumn() {
		return qColumn;
	}

	public void setQColumn(String qColumn) {
		this.qColumn = qColumn;
	}

	public String getAColumn() {
		return aColumn;
	}

	public void setAColumn(String aColumn) {
		this.aColumn = aColumn;
	}

	@Override
	public String toString() {
		return "PairExcelInfo [fileName=" + fileName + ", sheet=" + sheet + ", qColumn=" + qColumn + ", aColumn=" + aColumn + "]";
	}
}
