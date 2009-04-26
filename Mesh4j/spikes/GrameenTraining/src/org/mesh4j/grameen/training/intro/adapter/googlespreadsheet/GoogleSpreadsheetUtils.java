package org.mesh4j.grameen.training.intro.adapter.googlespreadsheet;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.GoogleSpreadSheetSyncRepository.SyncColumn;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model.GSBaseElement;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model.GSCell;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model.GSRow;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model.GSSpreadsheet;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model.GSWorksheet;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model.IGSElement;
import org.mesh4j.sync.adapters.split.SplitAdapter;
import org.mesh4j.sync.id.generator.IIdGenerator;
import org.mesh4j.sync.security.IIdentityProvider;
import org.mesh4j.sync.validations.Guard;
import org.mesh4j.sync.validations.MeshException;

import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.ListQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.DocumentListEntry.MediaType;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;

/**
 * this is the utility class used by Google spreadsheet adapter
 * @author sharif
 * version 1.0, 29/03/09
 *
 */
public class GoogleSpreadsheetUtils {

	public static final String DOC_FEED_URL = "http://docs.google.com/feeds/documents/private/full";
	public static final String TMP_FILE_DIR = "/";

	
	@SuppressWarnings("unchecked")
	public static void flush(SpreadsheetService service, GSSpreadsheet<GSWorksheet> spreadsheet) {

		for (GSWorksheet<GSBaseElement> worksheet : spreadsheet
				.getChildElements().values()) {
			if (worksheet.isDirty()) {
				Map<String, GSBaseElement> insertPool = new LinkedHashMap<String, GSBaseElement>();
				Map<String, GSBaseElement> updatePool = new LinkedHashMap<String, GSBaseElement>();
				Map<String, GSBaseElement> deletePool = new LinkedHashMap<String, GSBaseElement>();
				// used two pool because update and delete operation are
				// implemented differently

				processElementForFlush(worksheet, insertPool, updatePool, deletePool);
				
				//process update pool
				if(insertPool.size() > 0 || updatePool.size() > 0 || deletePool.size() > 0){
					
					try {
						CellFeed batchRequest = new CellFeed();
						for (GSBaseElement elementToUpdate : updatePool.values()) {
							
							GSCell cellToUpdate = (GSCell) elementToUpdate;
							BatchUtils.setBatchId(cellToUpdate.getCellEntry(), cellToUpdate
									.getCellEntry().getId());
							BatchUtils.setBatchOperationType(cellToUpdate.getCellEntry(),
									BatchOperationType.UPDATE);
							
							batchRequest.getEntries().add(
									cellToUpdate.getCellEntry());
						} 
						
						if(updatePool.size() > 0){
							// Submit the batch request.
							CellFeed feed = service.getFeed(worksheet.getWorksheetEntry().getCellFeedUrl(), CellFeed.class);
						    Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
							
							CellFeed batchResultFeed = service.batch(new URL(batchLink.getHref()), batchRequest);			
				
							// Make sure all the operations were successful.
							for (CellEntry entry : batchResultFeed.getEntries()) {
							  String batchId = BatchUtils.getBatchId(entry);
							  if (!BatchUtils.isSuccess(entry)) {
							    BatchStatus status = BatchUtils.getBatchStatus(entry);
							    String errorMsg = "Failed entry \t" + batchId + " failed (" + status.getReason() + ") " + status.getContent();
							    System.err.println(errorMsg);
							    throw new MeshException(new Exception(errorMsg));
							    //TODO: Need to enhance the exception handling codes
							    //TODO: Need to think about roll-back mechanism for partial update if such happens
							  }	
							}
						
							//update succeed, so mark the cells not dirty
							for (GSBaseElement elementToUpdate : updatePool.values()) {
								elementToUpdate.unsetDirty();
							}
						}	
						
					}catch (Exception e) {
						throw new MeshException(e);
					}finally{
					}
					
					//process insert/update pool (row)
					for (GSBaseElement elementToInsert : insertPool.values()) {
						if(elementToInsert instanceof GSRow){
							ListEntry newEntry = (ListEntry) elementToInsert.getBaseEntry();
							try {
								BaseEntry le = null;						
								if(newEntry.getId() == null)
									le = service
											.insert(worksheet.getWorksheetEntry()
													.getListFeedUrl(), newEntry);
								else
									le = elementToInsert.getBaseEntry().update();
								
								elementToInsert.setBaseEntry(le);
								
								//refresh this but not its childs!
								elementToInsert.unsetDirty(false);
								elementToInsert.refreshMe();
							   
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ServiceException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
					
					//process delete pool
					for (GSBaseElement elementToDetete : deletePool.values()) {
						
						try {
							if(elementToDetete.getBaseEntry().getId() != null) //entry physically exists in the spreadsheet file
								elementToDetete.getBaseEntry().delete();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ServiceException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
				} // if(updatePool.size() > 0 || deletePool.size() > 0)
				
				//this will remove the deleted childs (if any) in memory and update list index of remaining childs
				worksheet.refreshMe();
				worksheet.unsetDirty();
				
			}// if (worksheet.isDirty())
		}//end for
	}


	/**
	 * iterate over the whole object graph to identify changed elements of the
	 * spreadsheet file and transfer them to corresponding pool for
	 * update/delete operation
	 * 
	 * @param element
	 * @param deletePool 
	 * @param updatePool 
	 */
	@SuppressWarnings("unchecked")
	private static void processElementForFlush(GSBaseElement<GSBaseElement> element,
			Map<String, GSBaseElement> insertPool,
			Map<String, GSBaseElement> updatePool,
			Map<String, GSBaseElement> deletePool) {
		
		if(element.isDeleteCandidate())
			deletePool.put(element.getId(), element);
		else
			for (GSBaseElement subElement : element.getChildElements().values()) {
				if (subElement.isDirty()) {
					if (subElement.isDeleteCandidate()) {
						// add subElement to delete pool
						deletePool.put(subElement.getId(), subElement);
					} else {
						if (subElement instanceof GSCell) {
							// add subElement to update pool
							updatePool.put(subElement.getId(), subElement);
						} else if (subElement instanceof GSRow){ 
								if(subElement.getBaseEntry().getId() == null ){//only for new rows
									insertPool.put(subElement.getElementId(), subElement);
								}else{
									//if all childs are new but row has an ID
									//then add it to insert pool () 
									boolean eligible = true;
									for (IGSElement child : ((GSRow<GSCell>) subElement)
										.getChildElements().values()) {
										if (child.isDirty()
											&& child.getBaseEntry().getId() != null) {
											eligible = false;
											break;
										}										
									}
									if(eligible)
										insertPool.put(subElement.getElementId(), subElement);
									else
										processElementForFlush(subElement, insertPool, updatePool, deletePool);
								}	
						} else
							processElementForFlush(subElement, insertPool, updatePool, deletePool);
					}
				}
			}

	}

	
	
	/**
	 * get a row by specific cell info   
	 * 
	 * @param service
	 * @param worksheet
	 * @param columnTag : Tag name of the column contains the cell
	 * @param value : cell value
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Deprecated
	public static ListEntry getRow(SpreadsheetService service,
			WorksheetEntry worksheet, String columnTag, String value)
			throws IOException, ServiceException {
		ListFeed feed = service.getFeed(worksheet.getListFeedUrl(),
				ListFeed.class);

		for (ListEntry entry : feed.getEntries()) {
			if (entry.getCustomElements().getValue(columnTag).equals(value))
				return entry;
		}
		return null;
	}

	/**
	 * get a row by row index
	 * 
	 * @param service
	 * @param worksheet
	 * @param rowIndex
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Deprecated
	public static ListEntry getRow(SpreadsheetService service,
			WorksheetEntry worksheet, int rowIndex)
			throws IOException, ServiceException {
		
	    ListQuery query = new ListQuery(worksheet.getListFeedUrl());
	    query.setStartIndex(rowIndex-1);
	    query.setMaxResults(1);

	    ListFeed feed = service.query(query, ListFeed.class);
	    
		if(feed.getEntries().size()>0)
			return feed.getEntries().get(0);
		else return null;	
	}	


	/**
	 * 
	 * get a {@link ListEntry} object from feed by http request
	 * @param worksheet
	 * @param rowIndex
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	public static ListEntry getListEntryFromFeed(WorksheetEntry worksheet, int rowIndex)
		throws IOException, ServiceException {

		ListQuery query = new ListQuery(worksheet.getListFeedUrl());
		query.setStartIndex(rowIndex-1);
		query.setMaxResults(1);
		
		ListFeed feed = worksheet.getService().query(query, ListFeed.class);
		
		if(feed.getEntries().size()>0)
			return feed.getEntries().get(0);
		else return null;	
	}
	
	/**
	 * get a row by rowId
	 * http://spreadsheets.google.com/feeds/list/key/worksheetId/visibility/projection/rowId
	 * 
	 * @param service
	 * @param worksheet
	 * @param rowId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Deprecated
	public static ListEntry getRow(SpreadsheetService service,
			WorksheetEntry worksheet, String rowId) throws IOException,
			ServiceException {

		ListFeed feed = service.getFeed(worksheet.getListFeedUrl(),
				ListFeed.class);

		for (ListEntry row : feed.getEntries()) {
			if (row.getId().endsWith(rowId))
				return row;
		}
		return null;
	}	
	
	/**
	 * get a cell by row and column index 
	 * 
	 * @param service
	 * @param worksheet
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Deprecated
	public static CellEntry getCell(SpreadsheetService service,
			WorksheetEntry worksheet, int rowIndex, int colIndex)
			throws IOException, ServiceException {

		CellQuery query = new CellQuery(worksheet.getCellFeedUrl());
		query.setMinimumRow(rowIndex);
		query.setMaximumRow(rowIndex);
		query.setMinimumCol(colIndex);
		query.setMaximumCol(colIndex);
		CellFeed feed = service.query(query, CellFeed.class);

		if (feed.getEntries().size() > 0)
			return feed.getEntries().get(0);
		else
			return null;
	}	
	
	/**
	 * get cell by cellId
	 * http://spreadsheets.google.com/feeds/cells/key/worksheetId/visibility/projection/cellId
	 * 
	 * @param service
	 * @param worksheet
	 * @param cellId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Deprecated
	public static CellEntry getCell(SpreadsheetService service,
			WorksheetEntry worksheet, String cellId) throws IOException,
			ServiceException {

		CellFeed feed = service.getFeed(worksheet.getCellFeedUrl(),
				CellFeed.class);

		for (CellEntry cell : feed.getEntries()) {
			if (cell.getId().endsWith(cellId))
				return cell;
		}

		return null;
	}	
	
	
	/**
	 * get a cell from a row by column index
	 * 
	 * @param service
	 * @param worksheet
	 * @param row
	 * @param columnIndex
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Deprecated
	public static CellEntry getCell(SpreadsheetService service,
			WorksheetEntry worksheet, ListEntry row, int columnIndex)
			throws IOException, ServiceException {
		ListFeed lFeed = service.getFeed(worksheet.getListFeedUrl(),
				ListFeed.class);
		int rowIndex = lFeed.getEntries().indexOf(row);

		CellQuery query = new CellQuery(worksheet.getCellFeedUrl());
		query.setMinimumRow(rowIndex);
		query.setMaximumRow(rowIndex);
		CellFeed cFeed = service.query(query, CellFeed.class);

		for (CellEntry entry : cFeed.getEntries()) {
			if (entry.getCell().getCol() == columnIndex)
				return entry;
		}

		return null;
	}
	
	
	/**
	 * get a cell by row and column index 
	 * 
	 * @param service
	 * @param worksheet
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static GSRow getGSRow(SpreadsheetService service,
			WorksheetEntry worksheet, int rowIndex)
			throws IOException, ServiceException {

		ListQuery query = new ListQuery(worksheet.getListFeedUrl());
		query.setStartIndex(rowIndex-1);
		query.setMaxResults(1);
		ListFeed feed = service.query(query, ListFeed.class);

		GSRow gsListEntry = null;
		
		if (feed.getEntries().size() > 0){
			gsListEntry = new GSRow(feed.getEntries().get(0), rowIndex);
			gsListEntry.populateClild_BLOCKED(/*service,*/ worksheet);
		}
		
		return gsListEntry;
	}	

	/**
	 * get a cell by row and column index 
	 * 
	 * @param service
	 * @param worksheet
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Deprecated
	public static GSCell getGSCell(SpreadsheetService service,
			WorksheetEntry worksheet, int rowIndex, int colIndex)
			throws IOException, ServiceException {

		CellQuery query = new CellQuery(worksheet.getCellFeedUrl());
		query.setMinimumRow(rowIndex);
		query.setMaximumRow(rowIndex);
		query.setMinimumCol(colIndex);
		query.setMaximumCol(colIndex);
		CellFeed feed = service.query(query, CellFeed.class);

		GSCell gsCellEntry = null;
		
		if (feed.getEntries().size() > 0){
			gsCellEntry = new GSCell(feed.getEntries().get(0),null, "TODO: need to provide header tag");
			gsCellEntry.populateParent_BLOCKED(/*service,*/ worksheet);
		}
		
		return gsCellEntry;
	}	
	
	
/*	public static HSSFWorkbook getOrCreateWorkbookIfAbsent(String fileName) throws FileNotFoundException, IOException{
		HSSFWorkbook workbook = null;
		File file = new File(fileName);
		if(!file.exists()){
			workbook = new HSSFWorkbook();
		} else {
			workbook = new HSSFWorkbook(new FileInputStream(file));
		}
		return workbook;
	}
	
	public static HSSFSheet getOrCreateSheetIfAbsent(HSSFWorkbook workbook, String sheetName){
		HSSFSheet worksheet = null;
		if(workbook.getNumberOfSheets() == 0){
			worksheet = workbook.createSheet(sheetName);
		} else {
			worksheet = workbook.getSheet(sheetName);
			if(worksheet == null){
				worksheet = workbook.createSheet(sheetName);
			}
		}
		return worksheet;
	}
	
	public static HSSFRow getOrCreateRowHeaderIfAbsent(HSSFSheet worksheet){
		HSSFRow row = worksheet.getRow(0);
		if(row == null){
			row = worksheet.createRow(0);
		}
		return row;
	}
	
	@SuppressWarnings("unchecked")
	public static HSSFCell getOrCreateCellStringIfAbsent(HSSFRow row, String value){
		
		for (Iterator<HSSFCell> iterator = row.cellIterator(); iterator.hasNext();) {
			HSSFCell cell = iterator.next();
			String cellValue = cell.getRichStringCellValue().getString();
			if(cellValue.equals(value)){
				return cell;
			}
		}
		
		HSSFCell cell = row.createCell(row.getPhysicalNumberOfCells(), HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(value));
		return cell;
	}

	public static void updateOrCreateCellStringIfAbsent(HSSFRow row, int columnIndex, String value) {
		HSSFCell cell = row.getCell(columnIndex);
		if(cell == null){
			cell = row.createCell(columnIndex, HSSFCell.CELL_TYPE_STRING);
		}
		cell.setCellValue(new HSSFRichTextString(value));
	}

	@SuppressWarnings("unchecked")
	public static void updateRow(HSSFSheet worksheet, HSSFRow row, Element payload) {
		
		HSSFRow rowHeader = worksheet.getRow(0);
		HSSFCell cellHeader;
		
		Element child;
		for (Iterator<Element> iterator = payload.elementIterator(); iterator.hasNext();) {
			child = (Element) iterator.next();
			HSSFCell cell = getCell(worksheet, row, child.getName());
			if(cell == null){
				cellHeader = getOrCreateCellStringIfAbsent(rowHeader, child.getName());
				cell = row.createCell(cellHeader.getColumnIndex());
			}
			cell.setCellValue(new HSSFRichTextString(child.getText()));     // TODO (JMT) RDF Schema: data type formatters
		}
	}

	@SuppressWarnings("unchecked")
	public static boolean isPhantomRow(HSSFRow row) {
		if(row == null){
			return true;
		}
		
		for (Iterator<HSSFCell> iterator = row.cellIterator(); iterator.hasNext();) {
			HSSFCell cell = iterator.next();
			if(HSSFCell.CELL_TYPE_BLANK != cell.getCellType()){
				return false;
			}
		}
		return true;
	}
	
*/
	
	/**
	 * get a spreadsheet entry by sheetID
	 * 
	 * @param factory
	 * @param service
	 * @param sheetId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Deprecated
	public static SpreadsheetEntry getSpreadsheet(FeedURLFactory factory,
			SpreadsheetService service, String sheetId) throws IOException,
			ServiceException {

		SpreadsheetFeed feed = service.getFeed(
				factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);

		for (SpreadsheetEntry spreadsheet : feed.getEntries()) {
			if (spreadsheet.getId().substring(
					spreadsheet.getId().lastIndexOf("/") + 1).equals(sheetId))
				return spreadsheet;
		}
		return null;
	}

	/**
	 * get a Spreadsheet entry by index
	 * 
	 * @param factory
	 * @param service
	 * @param sheetIndex
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Deprecated
	public static SpreadsheetEntry getSpreadsheet(FeedURLFactory factory,
			SpreadsheetService service, int sheetIndex) throws IOException,
			ServiceException {

		SpreadsheetFeed feed = service.getFeed(
				factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);

		return feed.getEntries().get(sheetIndex);
	}	
	
	/**
	 * get a custom Spreadsheet entry by index
	 * 
	 * @param factory
	 * @param service
	 * @param sheetIndex
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	public static GSSpreadsheet getGSSpreadsheet(FeedURLFactory factory,
			SpreadsheetService service, int sheetIndex) throws IOException,
			ServiceException {

		SpreadsheetFeed feed = service.getFeed(
				factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);

		GSSpreadsheet<GSWorksheet> gsSpreadsheet = null;
		
		//pickup the specific spreadsheet and build a custom spreadsheet object
		if(feed.getEntries().size() >= sheetIndex)
			gsSpreadsheet = new GSSpreadsheet<GSWorksheet>(feed.getEntries().get(sheetIndex));
		
		return getGSSpreadsheet( factory,
				 service,  gsSpreadsheet);		
	}

	
	/**
	 * get a custom spreadsheet entry by sheetID
	 * 
	 * @param factory
	 * @param service
	 * @param sheetId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	public static GSSpreadsheet getGSSpreadsheet(FeedURLFactory factory,
			SpreadsheetService service, String sheetId) throws IOException,
			ServiceException {
	
		SpreadsheetFeed feed = service.getFeed(
				factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);

		GSSpreadsheet<GSWorksheet> gssSpreadsheet = null;
		
		//pickup the specific spreadsheet and build a custom spreadsheet object
		for (SpreadsheetEntry ss : feed.getEntries()) {
			if (ss.getId().substring(ss.getId().lastIndexOf("/") + 1).equals(
					sheetId)) {
				gssSpreadsheet = new GSSpreadsheet<GSWorksheet>(ss);
				break;
			}
		}
		
		return getGSSpreadsheet( factory,
				 service,  gssSpreadsheet);
	}
	
	
	/**
	 * get a spreadsheet entry by sheetID
	 * its takes 3 http request to populate the whole spreadsheet in our custom object structure
	 * 
	 * @param factory
	 * @param service
	 * @param sheetId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	public static GSSpreadsheet getGSSpreadsheet(FeedURLFactory factory,
			SpreadsheetService service, GSSpreadsheet<GSWorksheet> gsSpreadsheet) throws IOException,
			ServiceException {		
		
		//get all worksheets from the spreadsheet
		List<WorksheetEntry> wsList = getAllWorksheet(service, gsSpreadsheet.getSpreadsheet()); //1 http request
		
		for(WorksheetEntry ws: wsList){
			//create a custom worksheet object 
			GSWorksheet<GSRow> gsWorksheet = new GSWorksheet<GSRow>(
					ws, wsList.indexOf(ws) + 1, gsSpreadsheet);
			
			List<ListEntry> rowList = getAllRows( ws); //1 http request
			List<CellEntry> cellList = getAllCells( ws); //1 http request
			
			if(cellList.size() > 0 ){
				//get the header row and put it as the 1st row in the rowlist
				GSRow<GSCell> gsListHeaderEntry = new GSRow(
						new ListEntry(), 1, gsWorksheet);
				gsListHeaderEntry.populateClildWithHeaderTag(cellList, ws);				
				gsWorksheet.getChildElements().put(gsListHeaderEntry.getElementId(), gsListHeaderEntry);			
				
				for (ListEntry row : rowList){
					//create a custom row object and populate its child
					GSRow<GSCell> gsListEntry = new GSRow(
							row, rowList.indexOf(row) + 2, gsWorksheet); //+2 because #1 position is occupied by list header entry 
					gsListEntry.populateClildWithHeaderTag(cellList, ws);				
					
					//add a row to the custom worksheet object
					gsWorksheet.getChildElements().put(gsListEntry.getElementId(), gsListEntry);
					//TODO: right now index has been used as key; mjrow.getId() could have used, this need to review
				}
			} // if
			//add a custom worksheet object to the custom spreadsheet object 
			gsSpreadsheet.getChildElements().put(gsWorksheet.getElementId(),gsWorksheet);
		} //for		
		
		return gsSpreadsheet;
	}
	
	
	/**
	 * get all worksheet form a spreadsheet
	 * 
	 * @param service
	 * @param spreadsheet
	 * @param sheetId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	public static List<WorksheetEntry> getAllWorksheet(SpreadsheetService service,
			SpreadsheetEntry spreadsheet) throws IOException,
			ServiceException {

		Guard.argumentNotNull(spreadsheet, "spreadsheet");

		URL worksheetFeedUrl = spreadsheet.getWorksheetFeedUrl();
		WorksheetFeed worksheetFeed = service.getFeed(worksheetFeedUrl,
				WorksheetFeed.class);
		return worksheetFeed.getEntries();
	}
	

	/**
	 * get all rows form a worksheet
	 * 
	 * @param service
	 * @param spreadsheet
	 * @param sheetId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 * 
	 * use getAllRows({@link ListEntry}) instead
	 */
/*	@Deprecated
	public static List<ListEntry> getAllRows(SpreadsheetService service,
			WorksheetEntry worksheet) throws IOException,
			ServiceException {

		return getAllRows(worksheet);
	}*/

	public static List<ListEntry> getAllRows(WorksheetEntry worksheet) throws IOException,
			ServiceException {

		Guard.argumentNotNull(worksheet, "worksheet");

		URL listFeedUrl = worksheet.getListFeedUrl();
		ListFeed listFeed = worksheet.getService().getFeed(listFeedUrl,
				ListFeed.class);
		return listFeed.getEntries();
	}
	
	/**
	 * get all cells  form a worksheet 
	 * 
	 * @param service
	 * @param spreadsheet
	 * @param sheetId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 * 
	 * use getAllCells({@link WorksheetEntry}) instead
	 */
/*	@Deprecated
	public static List<CellEntry> getAllCells(SpreadsheetService service,
			WorksheetEntry worksheet) throws IOException,
			ServiceException {

		return getAllCells(worksheet);
	}*/

	public static List<CellEntry> getAllCells(WorksheetEntry worksheet) throws IOException,
			ServiceException {

		Guard.argumentNotNull(worksheet, "worksheet");

		URL cellFeedUrl = worksheet.getCellFeedUrl();
		CellFeed cellFeed = worksheet.getService().getFeed(cellFeedUrl,
				CellFeed.class);
		return cellFeed.getEntries();
	}	
	
	/**
	 * get specific worksheet by index form a spreadsheet
	 * 
	 * @param service
	 * @param spreadsheet
	 * @param sheetId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Deprecated
	public static WorksheetEntry getWorksheet(SpreadsheetService service,
			SpreadsheetEntry spreadsheet, String sheetId) throws IOException,
			ServiceException {

		URL worksheetFeedUrl = spreadsheet.getWorksheetFeedUrl();
		WorksheetFeed worksheetFeed = service.getFeed(worksheetFeedUrl,
				WorksheetFeed.class);
		for (WorksheetEntry worksheet : worksheetFeed.getEntries()) {
			if (worksheet.getId().substring(
					worksheet.getId().lastIndexOf("/") + 1).equals(sheetId))
				return worksheet;
		}

		return null;
	}

	/**
	 * get specific worksheet by index form a spreadsheet 
	 * 
	 * @param service
	 * @param spreadsheet
	 * @param sheetIndex
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Deprecated
	public static WorksheetEntry getWorksheet(SpreadsheetService service,
			SpreadsheetEntry spreadsheet, int sheetIndex) throws IOException,
			ServiceException {
		URL worksheetFeedUrl = spreadsheet.getWorksheetFeedUrl();
		WorksheetFeed worksheetFeed = service.getFeed(worksheetFeedUrl,
				WorksheetFeed.class);
		return worksheetFeed.getEntries().get(sheetIndex);
	}
	
	public Date getDate(String dateString){
		
		return null;
	}
	
	public static GSRow<GSCell> getRow(GSWorksheet<GSRow<GSCell>> worksheet,int columnIndex,String cellValue){
		GSRow<GSCell> row ;
		for(Map.Entry<String, GSRow<GSCell>> mpRow : worksheet.getGSRows().entrySet()){
			 row = mpRow.getValue();
			if(row.getGSCells().size()>0){
				GSCell cell = row.getGSCell(columnIndex);
				String cellContentAsString = cell.getCellValue();
				if(cellContentAsString != null && !cellContentAsString.equals("")){
					if(cellContentAsString.equals(cellValue)){
						//comparison is successful so, the desired  row the is current row
						 return row;
					}
				} 
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param gsRow
	 * @param cellValue
	 * @return
	 * @author Raju
	 */
	public static GSCell getCell(GSRow<GSCell> gsRow,String ColumName){
		for(Map.Entry<String, GSCell> mapCell :gsRow.getGSCells().entrySet()){
			GSCell cell = mapCell.getValue();
			if(cell.getCellEntry().getCell().getValue().equals(ColumName)){
				return cell;
			}
		}
		return null;
	}
	
	public static Date normalizeDate(String dateAsString,String format){
		Guard.argumentNotNull(dateAsString, "dateAsString");
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		Date dateAndTime;
		try {
			dateAndTime = simpleDateFormat.parse(dateAsString);
		} catch (ParseException e) {
			throw new MeshException(e);
		}
		return  dateAndTime;
	}	
	
	public static SplitAdapter createGoogleSpreadSheetAdapter(
			IGoogleSpreadSheet spreadsheet, ISpreadSheetToXMLMapper mapper,
			GSWorksheet contentWorkSheet, GSWorksheet syncWorkSheet,
			IIdentityProvider identityProvider, IIdGenerator idGenerator) {
		
		GoogleSpreadSheetContentAdapter contentRepo = new GoogleSpreadSheetContentAdapter(
				spreadsheet, contentWorkSheet, mapper);
		
		GoogleSpreadSheetSyncRepository syncRepo = new GoogleSpreadSheetSyncRepository(
				spreadsheet, identityProvider, idGenerator,
				getSyncWorksheetName(contentWorkSheet.getName()));
		
		SplitAdapter splitAdapter = new SplitAdapter(syncRepo, contentRepo,
				identityProvider);
		
		return splitAdapter;
	}

	public static String getSyncWorksheetName(String baseWorksheetName) {
		return baseWorksheetName+"_sync";
	}
	
	public static GSWorksheet<GSRow<GSCell>> getOrCreateSyncSheetIfAbsent(IGoogleSpreadSheet spreadsheet, String syncWorksheetName) {

		GSWorksheet<GSRow<GSCell>> gsWorksheet = spreadsheet.getGSWorksheet(syncWorksheetName);

		if(gsWorksheet == null){
			 try {
				    /*WorksheetEntry worksheet = new WorksheetEntry();
					worksheet.setTitle(new PlainTextConstruct(syncWorksheetName));
					//TODO: need to review what should be the default row count of the sheet 
					worksheet.setRowCount(80);
					worksheet.setColCount(10);
					
					worksheet = spreadsheet.getService().insert(
							((SpreadsheetEntry) spreadsheet.getGSSpreadsheet()
									.getBaseEntry()).getWorksheetFeedUrl(),
							worksheet);
					
					gsWorksheet =
						new GSWorksheet<GSRow<GSCell>>(worksheet, spreadsheet.getGSSpreadsheet().getChildElements().size() + 1,
								spreadsheet.getGSSpreadsheet());								
					spreadsheet.getGSSpreadsheet().addChildElement(
							gsWorksheet.getElementId(), gsWorksheet);*/
					
				 	gsWorksheet = getOrCreateWorkSheetIfAbsent(spreadsheet, syncWorksheetName);
				 
					GSRow<GSCell> parentRow = new GSRow<GSCell>(new ListEntry(), 1,
							gsWorksheet);
					gsWorksheet.addChildElement(parentRow.getElementId(), parentRow);
					
					int i = 1;
					for (SyncColumn sc : SyncColumn.values()) {
						CellEntry newCellEntry = new CellEntry(1, i++, sc.name());
						newCellEntry = spreadsheet.getService().insert(
							gsWorksheet.getWorksheetEntry().getCellFeedUrl(),
							newCellEntry);
						GSCell newGsCell = new GSCell(newCellEntry, parentRow,  sc.toString());
						parentRow.addChildElement(newGsCell.getColumnTag(), newGsCell);
						newGsCell.unsetDirty();
					}
					
					
				 } catch (Exception e) {
				        throw new MeshException(e);
				 }
		}
		
		return gsWorksheet;
	}
	
	
	public static GSSpreadsheet getOrCreateGSSpreadsheetIfAbsent(FeedURLFactory factory,
			SpreadsheetService service, String sheetId) throws IOException,
			ServiceException {
	
		SpreadsheetFeed feed = service.getFeed(
				factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);

		GSSpreadsheet<GSWorksheet> gssSpreadsheet = null;
		
		//pickup the specific spreadsheet and build a custom spreadsheet object
		for (SpreadsheetEntry ss : feed.getEntries()) {
			if (ss.getId().substring(ss.getId().lastIndexOf("/") + 1).equals(
					sheetId)) {
				gssSpreadsheet = new GSSpreadsheet<GSWorksheet>(ss);
				break;
			}
		}
		
		if (gssSpreadsheet == null) {

			createNewDocAndUpload("New Spredsheet");

			feed = service.getFeed(factory.getSpreadsheetsFeedUrl(),
					SpreadsheetFeed.class);

			for (SpreadsheetEntry ss : feed.getEntries()) {
				if (ss.getTitle().equals("New Spredsheet")) {
					gssSpreadsheet = new GSSpreadsheet<GSWorksheet>(ss);
					break;
				}
			}
		}
		
		return gssSpreadsheet;
	}
		
	private static void createNewDocAndUpload(String fileName)
			throws IOException, ServiceException {

		URL documentListFeedUrl = new URL(DOC_FEED_URL);
		DocsService service = new DocsService("Document Service");
		service.setUserCredentials("sharif.uddin.ku@gmail.com", "sharif123");
		
		File documentFile = new File(TMP_FILE_DIR + fileName + ".xls");
		if(!documentFile.exists()){
			documentFile.createNewFile();
		}
		
		DocumentEntry newDocument = new DocumentEntry();		
		newDocument.setFile(documentFile, MediaType.XLS.getMimeType());
		newDocument.setTitle(new PlainTextConstruct(documentFile.getName()));
		
		try{
			newDocument = service.insert(documentListFeedUrl, newDocument);
		}
		finally{
			if(documentFile.exists())
				documentFile.delete();
		}
	}
  
	public static GSWorksheet<GSRow<GSCell>> getOrCreateWorkSheetIfAbsent(IGoogleSpreadSheet spreadsheet, String worksheetName) {
		
		GSWorksheet<GSRow<GSCell>> gsWorksheet = spreadsheet.getGSWorksheet(worksheetName);
		
		if(gsWorksheet == null){
			 try {
			    WorksheetEntry worksheet = new WorksheetEntry();
				worksheet.setTitle(new PlainTextConstruct(worksheetName));
				//TODO: need to review what should be the default row count of the sheet 
				worksheet.setRowCount(100);
				worksheet.setColCount(10);
				
				worksheet = spreadsheet.getService().insert(
						((SpreadsheetEntry) spreadsheet.getGSSpreadsheet()
								.getBaseEntry()).getWorksheetFeedUrl(),
						worksheet);
				
				gsWorksheet =
					new GSWorksheet<GSRow<GSCell>>(worksheet, spreadsheet.getGSSpreadsheet().getChildElements().size() + 1,
							spreadsheet.getGSSpreadsheet());								
				spreadsheet.getGSSpreadsheet().addChildElement(
						gsWorksheet.getElementId(), gsWorksheet);
				
				
			 } catch (Exception e) {
			        throw new MeshException(e);
			 }
		}
		
		return gsWorksheet;
	}	

	public static GSRow<GSCell> getOrCreateHeaderRowIfAbsent(
			GSWorksheet<GSRow<GSCell>> worksheet) {
		GSRow<GSCell> row = worksheet.getGSRow(0);
		if (row == null) {
			row = worksheet.createAndAddNewRow(0);
		}
		return row;
	}

	public static GSCell getOrCreateHeaderCellIfAbsent(GSRow<GSCell> row,
			String propertyName) {
		GSCell cell = row.getGSCell(propertyName);
		if (cell == null) {
			cell = row.createAndAddNewCell(row.getChildElements().size() + 1,
					propertyName, propertyName);
		}
		return cell;
	}	
}
