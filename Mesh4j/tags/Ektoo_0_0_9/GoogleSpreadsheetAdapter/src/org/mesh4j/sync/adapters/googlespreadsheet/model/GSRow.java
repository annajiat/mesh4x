package org.mesh4j.sync.adapters.googlespreadsheet.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mesh4j.sync.validations.MeshException;

import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.ServiceException;

/**
 * This class is to wrap a {@link ListEntry}, also contains a list of references
 * to {@link GSCellEntry} as the cells it contains.
 * 
 * @author sharif
 * @Version 1.0, 29-03-09
 * 
 */
public class GSRow<C> extends GSBaseElement<C>{
	
	// MODEL VARIABLES
	
	
	// BUSINESS METHODS
	public GSRow(Map<String, C> gsCells,
			ListEntry rowEntry, int rowIndex,
			GSWorksheet<GSRow<GSCell>> parentElement) {
		super();
		this.childElements = gsCells;
		this.baseEntry = rowEntry;
		this.elementListIndex = rowIndex;
		this.parentElement = parentElement;
	}	
	
	@Deprecated
	public GSRow(ListEntry rowEntry, int rowIndex) {
		super();
		this.childElements = new LinkedHashMap<String, C>();
		this.baseEntry = rowEntry;
		this.elementListIndex = rowIndex;
	}	

	public GSRow(ListEntry rowEntry, int rowIndex,
			GSWorksheet<GSRow<GSCell>> parentElement) {
		super();
		this.childElements = new LinkedHashMap<String, C>();
		this.baseEntry = rowEntry;
		this.elementListIndex = rowIndex;
		this.parentElement = parentElement;
	}	
	
	/**
	 * get the core {@link ListEntry} object wrapped by this {@link GSRow}
	 * @return
	 */
	public ListEntry getRowEntry() {
		return (ListEntry) getBaseEntry();
	}

	/**
	 * get the parent/container {@link GSWorksheet} object of this {@link GSRow}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public GSWorksheet<GSRow<GSCell>> getParentWorksheet() {
		return (GSWorksheet) getParentElement();
	}
	
	/**
	 * get all the child {@link GSCell} contained in this {@link GSRow} 
	 * @return
	 */
	public Map<String, C> getGSCells() {
		return getNonDeletedChildElements();
	}

	/**
	 * get all the child {@link GSCell} contained in this {@link GSRow} in the
	 * form of {@link ArrayList}
	 * 
	 * @return
	 */
	
	public List<C> getGsCellsAsList() {
		return new ArrayList<C>(getNonDeletedChildElements().values());
	}
	
	/**
	 * get the row index of this {@link GSRow} in the container {@link GSWorksheet}
	 * @return
	 */
	public int getRowIndex() {
		return getElementListIndex();
	}

	/**
	 * get the {@link GSCell} from this {@link GSRow} by colIndex 
	 * 
	 * @param colIndex
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public C getGSCell(int colIndex){
		for(GSCell gsCell : ((GSRow<GSCell>)this).getNonDeletedChildElements().values()){
			if(gsCell.getCellEntry().getCell().getCol() == colIndex){
				return (C) gsCell;
			}			
		}
		return null;
	}

	/**
	 * get the {@link GSCell} from this {@link GSRow} by key/header tag 
	 * 
	 * @param key
	 * @return
	 */
	public C getGSCell(String key){
		return getChildElement(key);
	}
	

	@SuppressWarnings("unchecked")
	public void populateClildWithHeaderTag(List<CellEntry> cellList, GSWorksheet<GSRow> gsWs) throws IOException,
		ServiceException {
		if (this.elementListIndex > 0) {
			// iterate over all cells, only cells of corresponding row will be
			// entered in this filtered list
			List<CellEntry> filteredCellList = new ArrayList<CellEntry>();
			for (CellEntry cell : cellList) {
				if (cell.getCell().getRow() == this.elementListIndex) {
					//String key = Integer.toString(cell.getCell().getCol());
					//this.childElements.put( key, (C) new GSCell(cell, (GSRow<GSCell>) this));
					filteredCellList.add(cell);
				}
			}
			
			//pick a cell corresponding to a header tag and put it in the child map
			/*if(((ListEntry) this.getBaseEntry())
					.getCustomElements().getTags().size() == 0) {*/
			if(this.elementListIndex == 1) {
				for (CellEntry cell : cellList) {
					if (cell.getCell().getRow() == this.elementListIndex) {
										
						//the gdata api doesn't provide column tag for cell's of first row
						//that why it is generated from the cell's value
						
						String cellValue = cell.getCell().getValue();
						//TODO: need to review later if cellValue is null 
						/*if(cellValue == null || cellValue.length() ==0){							
							cellValue = "Column"+cell.getCell().getCol();
							cell.changeInputValueLocal(cellValue);
						}*/

						String key = extractCellHeadetTag(cellValue);						
						this.childElements.put(/*key*/cellValue, (C) new GSCell(cell,
								(GSRow<GSCell>) this, key)); 
					}
				}
				
			}else{
				GSRow headerRow = gsWs.getGSRow(1);
				
				for (String tag : ((ListEntry) this.getBaseEntry())
						.getCustomElements().getTags()) {							
					String value = ((ListEntry) this.getBaseEntry())
						.getCustomElements().getValue(tag);
					//TODO: determine the column position from tag position and see if it matches with column in cell
					for (CellEntry cell : filteredCellList) {
						if(cell.getCell().getValue().equals(value)){
							
							//my test
							//this.childElements.put( tag, (C) new GSCell(cell, (GSRow<GSCell>) this, tag));
							this.childElements.put( headerRow.getCellValueFromColumnTag(tag), (C) new GSCell(cell, (GSRow<GSCell>) this, tag));
							
							//this cell
							filteredCellList.remove(cell);
							break;
						}
					}
				}//for
				
			}
						
		} else {
			throw new MeshException("Element index cannot be less than 1");
		}
	}
	
	private String extractCellHeadetTag(String value) {
		StringBuffer tag = new StringBuffer("");
		for(char c: value.toLowerCase().toCharArray()){
			if(Character.isLetterOrDigit(c)){
				tag.append(c);
			}
		}		
		return tag.toString();
	}

	/**
 	 * update content/value of a {@link CellEntry} identified by column index in this row
 	 * 
	 * @param value
	 * @param colIndex
	 */
	@Deprecated
	public void updateCellValue(String value, int colIndex) {
		GSCell cellToUpdate = (GSCell) getGSCell(colIndex); 
		if (!cellToUpdate.getCellEntry().getCell().getInputValue()
				.equals(value)) 
			cellToUpdate.updateCellValue(value);
	}

	/**
	 * update content/value of a {@link CellEntry} identified by key in this row
	 * 
	 * @param value
	 * @param key
	 */
	public void updateCellValue(String value, String key) {
		//get header row and convert the key
		GSCell cellToUpdate = (GSCell) getGSCell(key); 		
		if (!cellToUpdate.getCellEntry().getCell().getInputValue()
				.equals(value)) 
			cellToUpdate.updateCellValue(value);
	}

	/**
	 * return the content/value of a cell 
	 * 
	 * @param key
	 * @return
	 */
	public String getCellValue(String key) {
		GSCell cell = (GSCell) getGSCell(key); 
		return cell.getCellValue();
	}	
	
	public String getCellValueFromColumnTag(String tag) {
		for(Entry<String, C> cellEntry : this.getChildElements().entrySet()){
			GSCell cell  = (GSCell) cellEntry.getValue();
			if(cell.getColumnTag().equals(tag))
				return cell.getCellValue();
		}
		return null;
	}
	
	/**
	 * this will create a new cell at column position col and add it to its child  
	 * elements with key 'key', any existing cell element with that key will be replaced
	 *  
	 * @param col
	 * @param key
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public GSCell createNewCell(int col, String key, String value) {
		if(col < 1)
			 throw new IllegalArgumentException("colIndex");
		
		CellEntry newCell = new CellEntry(this.elementListIndex, col, ""); //this is not supported for batch update :(
		GSCell newGSCell = new GSCell(newCell, (GSRow<GSCell>) this, extractCellHeadetTag(key));
		newGSCell.updateCellValue(value);
		this.addChildElement(key, (C) newGSCell);
		return newGSCell;
	}
	    	
}