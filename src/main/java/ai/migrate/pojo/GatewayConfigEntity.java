package ai.migrate.pojo;

public class GatewayConfigEntity {
	private String fileName;
	private String fileType;
	private String fileContent;
	private String parentDir;
	private String filePath;
	private Boolean isEmpty;

	public String getParentDir() {
		return parentDir;
	}

	public void setParentDir(String parentDir) {
		this.parentDir = parentDir;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileContent() {
		return fileContent;
	}

	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Boolean isEmpty() {
		return isEmpty;
	}

	public void setEmpty(Boolean isEmpty) {
		this.isEmpty = isEmpty;
	}

	@Override
	public String toString() {
		return "GatewayConfigEntity [fileName=" + fileName + ", fileType=" + fileType + ", fileContent=" + 
				fileContent + ", parentDir=" + parentDir + ", filePath=" + filePath + ", isEmpty=" + isEmpty + "]";
	}
}
