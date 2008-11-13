package org.mesh4j.sync.adapters.msexcel;

import java.io.File;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;
import org.mesh4j.sync.SyncEngine;
import org.mesh4j.sync.adapters.hibernate.EntityContent;
import org.mesh4j.sync.adapters.split.SplitAdapter;
import org.mesh4j.sync.id.generator.IIdGenerator;
import org.mesh4j.sync.id.generator.IdGenerator;
import org.mesh4j.sync.model.IContent;
import org.mesh4j.sync.model.Item;
import org.mesh4j.sync.model.Sync;
import org.mesh4j.sync.security.IIdentityProvider;
import org.mesh4j.sync.security.NullIdentityProvider;
import org.mesh4j.sync.test.utils.TestHelper;

public class MsExcelSyncTests {
	
	@Test
	public void executeSync(){
		
		IIdentityProvider identityProvider = NullIdentityProvider.INSTANCE;
		IIdGenerator idGenerator = IdGenerator.INSTANCE;
		String sheetName = "patient";
		String idColumnName = "id";
		
		MsExcel excelA = new MsExcel(TestHelper.fileName("fileA.xls"));
		MsExcelSyncRepository syncRepoA = new MsExcelSyncRepository(excelA, identityProvider, idGenerator);
		MsExcelContentAdapter contentAdapterA = new MsExcelContentAdapter(excelA, sheetName, idColumnName);
		SplitAdapter splitAdapterA = new SplitAdapter(syncRepoA, contentAdapterA, identityProvider);

		MsExcel excelB = new MsExcel(TestHelper.fileName("fileB.xls"));
		MsExcelSyncRepository syncRepoB = new MsExcelSyncRepository(excelB, identityProvider, idGenerator);
		MsExcelContentAdapter contentAdapterB = new MsExcelContentAdapter(excelB, sheetName, idColumnName);
		SplitAdapter splitAdapterB = new SplitAdapter(syncRepoB, contentAdapterB, identityProvider);		

		SyncEngine syncEngine = new SyncEngine(splitAdapterA, splitAdapterB);
		
		List<Item> conflicts = syncEngine.synchronize();
		
		Assert.assertEquals(0, conflicts.size());
		
	}

	@Test
	public void shouldSync() throws DocumentException{
		
		String sheetName = "patient";
		String idColumnName = "id";
		
		SplitAdapter adapterA = makeSplitAdapter(sheetName, idColumnName, "excelA.xls", "syncA.xls", NullIdentityProvider.INSTANCE, IdGenerator.INSTANCE);
		makeHeader(adapterA);
		adapterA.add(makeNewItem());
		adapterA.add(makeNewItem());
		adapterA.add(makeNewItem());
		adapterA.add(makeNewItem());
		
		SplitAdapter adapterB = makeSplitAdapter(sheetName, idColumnName, "excelB.xls", "syncB.xls", NullIdentityProvider.INSTANCE, IdGenerator.INSTANCE);
		makeHeader(adapterB);
		adapterB.add(makeNewItem());
		
		SyncEngine syncEngine = new SyncEngine(adapterA, adapterB);
		
		List<Item> conflicts = syncEngine.synchronize();
		
		Assert.assertEquals(0, conflicts.size());
		
	}
	
	@Test
	public void shouldSyncSameFile() throws DocumentException{
		
		String sheetName = "patient";
		String idColumnName = "id";
		
		SplitAdapter adapterA = makeSplitAdapter(sheetName, idColumnName, "dataAndSyncA.xls", "dataAndSyncA.xls", NullIdentityProvider.INSTANCE, IdGenerator.INSTANCE);
		makeHeader(adapterA);
		adapterA.add(makeNewItem());
		adapterA.add(makeNewItem());
		adapterA.add(makeNewItem());
		adapterA.add(makeNewItem());
		
		SplitAdapter adapterB = makeSplitAdapter(sheetName, idColumnName, "dataAndSyncB.xls", "dataAndSyncB.xls", NullIdentityProvider.INSTANCE, IdGenerator.INSTANCE);
		
		SyncEngine syncEngine = new SyncEngine(adapterA, adapterB);
		
		List<Item> conflicts = syncEngine.synchronize();
		
		Assert.assertEquals(0, conflicts.size());
		
	}
	
	// PRIVATE METHODS
	private void makeHeader(SplitAdapter adapter) {
		HSSFSheet sheet = ((MsExcelContentAdapter)adapter.getContentAdapter()).getSheet();
		HSSFRow row = sheet.getRow(0);
			
		HSSFCell cell = row.createCell(1, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString("name"));
			
		cell = row.createCell(2, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString("age"));
		
		cell = row.createCell(3, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString("country"));
		
		cell = row.createCell(4, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString("city"));
	}
	
	private Item makeNewItem() throws DocumentException {
		
		String id = IdGenerator.INSTANCE.newID();
		String xml = "<patient><id>"+id+"</id><name>marcelo</name><age>33</age><country>Argentina</country><city>Brandsen</city></patient>";
		Element payload = DocumentHelper.parseText(xml).getRootElement();
		
		IContent content = new EntityContent(payload, "patient", id);
		Sync sync = new Sync(IdGenerator.INSTANCE.newID(), "jmt", new Date(), false);
		return new Item(content, sync);
	}

	private SplitAdapter makeSplitAdapter(String sheetName, String idColumnName, String contentFileName, String syncFileName, IIdentityProvider identityProvider, IdGenerator idGenerator) {
		
		MsExcel contentExcel = null;
		MsExcel syncExcel = null;
		if(contentFileName.equals(syncFileName)){
			File file = TestHelper.makeFileAndDeleteIfExists(contentFileName);
			contentExcel = new MsExcel(file.getAbsolutePath());
			syncExcel = contentExcel;
		} else {
			File fileData = TestHelper.makeFileAndDeleteIfExists(contentFileName);
			File fileSync = TestHelper.makeFileAndDeleteIfExists(syncFileName);
			
			contentExcel = new MsExcel(fileData.getAbsolutePath());
			syncExcel = new MsExcel(fileSync.getAbsolutePath());
		}
		
		MsExcelSyncRepository syncRepo = new MsExcelSyncRepository(syncExcel, identityProvider, idGenerator);
		MsExcelContentAdapter contentAdapter = new MsExcelContentAdapter(contentExcel, sheetName, idColumnName);

		SplitAdapter splitAdapter = new SplitAdapter(syncRepo, contentAdapter, identityProvider);
		return splitAdapter;
	}
}
