package org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gdata.data.spreadsheet.SpreadsheetEntry;

/**
 * This class is to wrap a {@link SpreadsheetEntry}, also contains a list of reference
 * to {@link GSWorksheet} as the worksheets it contains
 * 
 * @author sharif
 * @Version 1.0, 29-03-09
 * 
 */
public class GSSpreadsheet<C> extends GSBaseElement<C>{

	// MODEL VARIABLES
	//all moved to base class
	
	// BUSINESS METHODS	
	public GSSpreadsheet(SpreadsheetEntry spreadsheet) {
		super();
		this.parentElement = null;
		this.baseEntry = spreadsheet;
		this.childElements = new LinkedHashMap<String, C>();
	}

	/**
	 * get the core {@link SpreadsheetEntry} object wrapped by this
	 * {@link GSSpreadsheet}
	 * @return
	 */
	public SpreadsheetEntry getSpreadsheet() {
		return (SpreadsheetEntry) getBaseEntry();
	}

	/**
	 * get all child {@link GSWorksheet} of this {@link GSSpreadsheet}
	 * @return
	 */
	public Map<String, C> getGSWorksheets() {
		return getChildElements();
	}	
	
	/**
	 * get a {@link GSWorksheet} from this {@link GSSpreadsheet} by row index
	 * @param rowIndex
	 * @return
	 */
	public C getGSWorksheet(int rowIndex) {
		return getChildElement(Integer.toString(rowIndex));
	}	
	
	/**
	 * get a {@link GSWorksheet} by key from this {@link GSSpreadsheet} 
	 * 
	 * @param key
	 * @return
	 */
	public C getGSWorksheet(String key){
		return getChildElement(key);
	}
	
	/**
	 * get a {@link GSWorksheet} by sheet name or title
	 * @param sheetName
	 * @return
	 */
	public C getGSWorksheetBySheetName(String sheetName) {
		for (C gsWorksheet : getChildElements().values()) {
			if (((GSWorksheet<?>) gsWorksheet)
					.getWorksheetEntry().getTitle().getPlainText()
					.equalsIgnoreCase(sheetName))
				return gsWorksheet;
		}
		return null;
	}
	
}
