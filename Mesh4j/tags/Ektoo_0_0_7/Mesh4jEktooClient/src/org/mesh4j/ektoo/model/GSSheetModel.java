package org.mesh4j.ektoo.model;

import org.mesh4j.ektoo.controller.GSSheetUIController;

/**
 * @author Bhuiyan Mohammad Iklash
 * 
 */
public class GSSheetModel extends AbstractModel 
{
	
	// MODEL VARIABLES
	private String userName = null;
	private String userPassword = null;
//	private String spreadsheetKey = null;
	private String spreadsheetName = null;
	private String worksheetName = null;

	// TODO (NBL) Think how we can eliminate these items from gui
	private String uniqueColumnName = null;
	private int uniqueColumnPosition = -1;
	private String lastUpdatedColumnName = null;
	private int lastUpdatedColumnPosition = -1;

	// BUSINESS METHODS
	public void setUserName(String userName) {
		firePropertyChange(GSSheetUIController.USER_NAME_PROPERTY, this.userName, this.userName = userName);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserPassword(String userPassword) {
		firePropertyChange(GSSheetUIController.USER_PASSWORD_PROPERTY, this.userPassword,
				this.userPassword = userPassword);
	}

	public String getUserPassword() {
		return userPassword;
	}

//	public void setSpreadsheetKey(String spreadsheetKey) {
//		firePropertyChange(GSSheetUIController.SPREADSHEET_KEY_PROPERTY, this.spreadsheetKey,
//				this.spreadsheetKey = spreadsheetKey);
//	}
	
//	public String getSpreadsheetKey() {
//		return spreadsheetKey;
//	}
	
	public void setSpreadsheetName(String spreadsheetName) {
		firePropertyChange(GSSheetUIController.SPREADSHEET_NAME_PROPERTY, this.spreadsheetName,
				this.spreadsheetName = spreadsheetName);
	}
	
	public String getSpreadsheetName() {
		return spreadsheetName;
	}

	public void setWorksheetName(String worksheetName) {
		firePropertyChange(GSSheetUIController.WORKSHEET_NAME_PROPERTY, this.worksheetName,
				this.worksheetName = worksheetName);
	}

	public String getWorksheetName() {
		return worksheetName;
	}

	public void setUniqueColumnName(String uniqueColumnName) {
		firePropertyChange(GSSheetUIController.UNIQUE_COLUMN_NAME_PROPERTY, this.uniqueColumnName,
				this.uniqueColumnName = uniqueColumnName);
	}

	public String getUniqueColumnName() {
		return uniqueColumnName;
	}

	public void setUniqueColumnPosition(int uniqueColumnPosition) {
		firePropertyChange(GSSheetUIController.UNIQUE_COLUMN_POSITION_PROPERTY, this.uniqueColumnPosition,
				this.uniqueColumnPosition = uniqueColumnPosition);
	}

	public int getUniqueColumnPosition() {
		return uniqueColumnPosition;
	}

	public void setLastUpdatedColumnName(String lastUpdatedColumnName) {
		firePropertyChange(GSSheetUIController.LASTUPDATE_COLUMN_NAME_PROPERTY, this.lastUpdatedColumnName,
				this.lastUpdatedColumnName = lastUpdatedColumnName);
	}

	public String getLastUpdatedColumnName() {
		return lastUpdatedColumnName;
	}

	public void setLastUpdatedColumnPosition(int lastUpdatedColumnPosition) {
		firePropertyChange(GSSheetUIController.LASTUPDATE_COLUMN_POSITION_PROPERTY,
				this.lastUpdatedColumnPosition,
				this.lastUpdatedColumnPosition = lastUpdatedColumnPosition);
	}

	public int getLastUpdatedColumnPosition() {
		return lastUpdatedColumnPosition;
	}
	
  public String toString()
  {
    return "Cloud | " + getSpreadsheetName() + " | " + getWorksheetName() ; 
  } 	
}
