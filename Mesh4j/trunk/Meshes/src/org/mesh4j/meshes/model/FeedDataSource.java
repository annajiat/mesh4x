package org.mesh4j.meshes.model;

public class FeedDataSource extends DataSource {
	
	public static final String FILE_NAME_PROPERTY = "feed_filename";
	
	private String fileName;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		String oldFileName = this.fileName;
		this.fileName = fileName;
		firePropertyChange(FILE_NAME_PROPERTY, oldFileName, fileName);
	}

}
