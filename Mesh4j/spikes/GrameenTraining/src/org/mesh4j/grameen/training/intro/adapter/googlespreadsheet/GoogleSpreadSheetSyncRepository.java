package org.mesh4j.grameen.training.intro.adapter.googlespreadsheet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model.GSCell;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model.GSRow;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model.GSWorksheet;
import org.mesh4j.sync.ISyncAware;
import org.mesh4j.sync.adapters.SyncInfo;
import org.mesh4j.sync.adapters.feed.rss.RssSyndicationFormat;
import org.mesh4j.sync.adapters.split.ISyncRepository;
import org.mesh4j.sync.id.generator.IIdGenerator;
import org.mesh4j.sync.model.IContent;
import org.mesh4j.sync.model.Sync;
import org.mesh4j.sync.parsers.SyncInfoParser;
import org.mesh4j.sync.security.IIdentityProvider;
import org.mesh4j.sync.validations.Guard;
import org.mesh4j.sync.validations.MeshException;
/**
 * Sync information repository, responsible for storing sync information
 * in google spreadsheet,basically CRUD operation
 * 
 * @author Raju
 * @version 1.0,29/4/2009
 */
public class GoogleSpreadSheetSyncRepository implements ISyncRepository,ISyncAware{

	//This attributes are usually used to represent the sync information
	//in a spreadsheet,where every sync row will have following column to
	//hold the items/contents necessary sync information.
	public final static String COLUMN_NAME_SYNC_ID = "syncId";
	public final static String COLUMN_NAME_ENTITY_NAME = "entityName";
	public final static String COLUMN_NAME_ENTITY_ID = "entityId";
	public final static String COLUMN_NAME_VERSION = "version";
	public final static String COLUMN_NAME_SYNC = "sync";
	
	private IGoogleSpreadSheet spreadSheet = null;
	private IIdentityProvider identityProvider = null;
	private IIdGenerator idGenerator = null;
	//represents a specific sheet of a google spreadsheet
	private GSWorksheet workSheet;
	private ISpreadSheetToXMLMapper mapper;
	
	public GoogleSpreadSheetSyncRepository(IGoogleSpreadSheet spreadSheet,GSWorksheet workSheet,
											IIdentityProvider identityProvider,IIdGenerator idGenerator,String sheetName){
		
		Guard.argumentNotNull(spreadSheet, "spreadSheet");
		Guard.argumentNotNull(identityProvider, "identityProvider");
		Guard.argumentNotNull(idGenerator, "idGenerator");
		Guard.argumentNotNullOrEmptyString(sheetName, "sheetName");
		
		this.spreadSheet = spreadSheet;
		this.identityProvider = identityProvider;
		this.idGenerator = idGenerator;
		this.workSheet = workSheet;
	}
	
	@Override
	public SyncInfo get(String syncId) {
		GSRow row ;
		int syncIdIndex = 1;
		row = GoogleSpreadsheetUtils.getRow(workSheet,syncIdIndex,syncId);
		if(row == null){
			return null;
		} else {
			SyncInfo syncInfo = convertRowToSyncInfo(row);
			return syncInfo;
		}
	}

	
	
	@Override
	public List<SyncInfo> getAll(String entityName) {
		List<SyncInfo> listOfAll = new LinkedList<SyncInfo>();
		
		for(Map.Entry<String, GSRow> mapRow : workSheet.getRowList().entrySet()){
			GSRow row = mapRow.getValue();
			if(row != null){
				SyncInfo syncInfo = convertRowToSyncInfo(row);
				if(syncInfo.getType().equals(entityName)){
					listOfAll.add(syncInfo);
				}
			}
		}
		return listOfAll;
	}

	@Override
	public String newSyncID(IContent content) {
		return this.idGenerator.newID();
	}

	@Override
	public void save(SyncInfo syncInfo) {
		GSRow row = this.workSheet.getGSRow(Integer.parseInt(syncInfo.getSyncId()));
		if(row == null){
			addRow(syncInfo);
		} else {
			updateRow(row,syncInfo);
		}
	}
	private void addRow(SyncInfo syncInfo){
		int rowIndex = this.workSheet.getRowList().size() + 1;
		Element payLoad = SyncInfoParser.convertSync2Element(syncInfo.getSync(), RssSyndicationFormat.INSTANCE, this.identityProvider);
		GSRow row = mapper.convertXMLElementToRow(payLoad, rowIndex);
		this.workSheet.addChildEntry(row);
	}
	private void updateRow(GSRow row  ,SyncInfo syncInfo){
		Element payLoad = SyncInfoParser.convertSync2Element(syncInfo.getSync(), RssSyndicationFormat.INSTANCE, this.identityProvider);
		
		GSRow updatedRow = mapper.convertXMLElementToRow(payLoad, row.getRowIndex());
		this.workSheet.addChildEntry(updatedRow);
	}
	
	@Override
	public void beginSync() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endSync() {
		// TODO Auto-generated method stub
		
	}
	
	private SyncInfo convertRowToSyncInfo(GSRow row){

		String entityName = row.getGsCell(2).getCellEntry().getCell().getValue();
		String entityId = row.getGsCell(3).getCellEntry().getCell().getValue();
		String version = row.getGsCell(4).getCellEntry().getCell().getValue();
		String syncXml = row.getGsCell(5).getCellEntry().getCell().getValue();
		try {
			Document doc = DocumentHelper.parseText(syncXml);
			Sync sync = SyncInfoParser.convertSyncElement2Sync(doc.getRootElement(), RssSyndicationFormat.INSTANCE, identityProvider, idGenerator);
			return new SyncInfo(sync,entityName,entityId,Integer.parseInt(version));
		} catch (Exception e) {
			throw new MeshException(e);
		}
	}

}
