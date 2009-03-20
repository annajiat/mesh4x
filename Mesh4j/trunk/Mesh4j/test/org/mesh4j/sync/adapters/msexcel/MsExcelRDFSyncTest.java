package org.mesh4j.sync.adapters.msexcel;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.dom4j.Element;
import org.junit.Test;
import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.SyncEngine;
import org.mesh4j.sync.adapters.split.SplitAdapter;
import org.mesh4j.sync.id.generator.IdGenerator;
import org.mesh4j.sync.model.IContent;
import org.mesh4j.sync.model.Item;
import org.mesh4j.sync.payload.schema.rdf.RDFInstance;
import org.mesh4j.sync.payload.schema.rdf.RDFSchema;
import org.mesh4j.sync.security.NullIdentityProvider;

public class MsExcelRDFSyncTest {

	@Test
	public void shouldSync() throws Exception{
			
		// schema
		String sheetName = "Oswego";
		String idColumn = "Code";

		RDFSchema schema = new RDFSchema(sheetName, "http://mesh4x/epiinfo/"+sheetName+"#", sheetName);
		schema.addStringProperty("Name", "name", "en");
		schema.addStringProperty("Code", "code", "en");
		schema.addIntegerProperty("AGE", "age", "en");
		schema.addStringProperty("SEX", "sex", "en");
		schema.addBooleanProperty("ILL", "ill", "en");
		schema.addDateTimeProperty("DateOnset", "dateOnset", "en");
		
		MsExcelToRDFMapping rdfMapping = new MsExcelToRDFMapping(schema, idColumn);
		
		// source split adapter
		HSSFWorkbook workbookSource = rdfMapping.createDataSource();	
		Date date1 = new Date();
		Date date2 = new Date();
		Date date3 = new Date();
		addData(workbookSource, sheetName, "juan1", "1", 30, "male", true, date1);
		addData(workbookSource, sheetName, "juan2", "2", 35, "female", true, date2);
		addData(workbookSource, sheetName, "juan3", "3", 3, "male", false, date3);
		Assert.assertEquals(3, workbookSource.getSheet(sheetName).getLastRowNum()); 
		
		MockMsExcel excelSource = new MockMsExcel(workbookSource);
		MsExcelSyncRepository syncRepoSource = new MsExcelSyncRepository(excelSource, NullIdentityProvider.INSTANCE, IdGenerator.INSTANCE);
		MsExcelContentAdapter contentAdapterSource = new MsExcelContentAdapter(excelSource, rdfMapping, sheetName);
		ISyncAdapter source = new SplitAdapter(syncRepoSource, contentAdapterSource, NullIdentityProvider.INSTANCE);

		// target split adapter
		HSSFWorkbook workbookTarget = rdfMapping.createDataSource();
		Date date4 = new Date();
		Date date5 = new Date();
		Date date6 = new Date();
		addData(workbookTarget, sheetName, "juan4", "4", 30, "male", true, date4);
		addData(workbookTarget, sheetName, "juan5", "5", 35, "female", true, date5);
		addData(workbookTarget, sheetName, "juan6", "6", 3, "male", false, date6);
		Assert.assertEquals(3, workbookTarget.getSheet(sheetName).getLastRowNum());
		
		MockMsExcel excelTarget = new MockMsExcel(workbookTarget);			
		MsExcelSyncRepository syncRepoTarget = new MsExcelSyncRepository(excelTarget, NullIdentityProvider.INSTANCE, IdGenerator.INSTANCE);
		MsExcelContentAdapter contentAdapterTarget = new MsExcelContentAdapter(excelTarget, rdfMapping, sheetName);
		ISyncAdapter target = new SplitAdapter(syncRepoTarget, contentAdapterTarget, NullIdentityProvider.INSTANCE);
		
		// sync
		SyncEngine syncEngine = new SyncEngine(source, target);
		List<Item> conflcts = syncEngine.synchronize();
		
		Assert.assertTrue(conflcts.isEmpty());
		
		Assert.assertTrue(excelSource.flushWasCalled());
		Assert.assertTrue(excelSource.dirtyWasCalled());
		
		List<Item> sourceItems = source.getAll();
		List<Item> targetItems = target.getAll();
		Assert.assertEquals(sourceItems.size(), targetItems.size());
		Assert.assertEquals(6, sourceItems.size());
		
		assertItem(schema, contentAdapterSource, "juan1", "1", 30, "male", true, date1);
		assertItem(schema, contentAdapterSource, "juan2", "2", 35, "female", true, date2);
		assertItem(schema, contentAdapterSource, "juan3", "3", 3, "male", false, date3);
		assertItem(schema, contentAdapterSource, "juan4", "4", 30, "male", true, date4);
		assertItem(schema, contentAdapterSource, "juan5", "5", 35, "female", true, date5);
		assertItem(schema, contentAdapterSource, "juan6", "6", 3, "male", false, date6);

		assertItem(schema, contentAdapterTarget, "juan1", "1", 30, "male", true, date1);
		assertItem(schema, contentAdapterTarget, "juan2", "2", 35, "female", true, date2);
		assertItem(schema, contentAdapterTarget, "juan3", "3", 3, "male", false, date3);
		assertItem(schema, contentAdapterTarget, "juan4", "4", 30, "male", true, date4);
		assertItem(schema, contentAdapterTarget, "juan5", "5", 35, "female", true, date5);
		assertItem(schema, contentAdapterTarget, "juan6", "6", 3, "male", false, date6);
	}
	
	private void assertItem(RDFSchema schema, MsExcelContentAdapter adapter, String name, String code,
			int age, String sex, boolean ill, Date dateOnset) {

		IContent content = adapter.get(code);
		Assert.assertNotNull(content);
		
		Element payload = content.getPayload();		
		RDFInstance instance = schema.createNewInstance("uri:urn:"+code, payload.asXML());
		
		Assert.assertEquals(name, instance.getPropertyValue("Name"));
		Assert.assertEquals(code, instance.getPropertyValue("Code"));
		Assert.assertEquals(age, instance.getPropertyValue("AGE"));
		Assert.assertEquals(sex, instance.getPropertyValue("SEX"));
		Assert.assertEquals(ill, instance.getPropertyValue("ILL"));
		Assert.assertEquals(dateOnset, instance.getPropertyValue("DateOnset"));
		
	}

	private void addData(HSSFWorkbook workbook, String sheetName, String name,
			String code, int age, String sex, boolean ill, Date dateOnset) {

		HSSFSheet sheet = workbook.getSheet(sheetName);		
		HSSFRow rowData = sheet.createRow(sheet.getLastRowNum() + 1);
		
		HSSFCell cell = rowData.createCell(0, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(name));
		
		cell = rowData.createCell(1, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(code));
		
		cell = rowData.createCell(2, HSSFCell.CELL_TYPE_NUMERIC);
		cell.setCellValue(age);
		
		cell = rowData.createCell(3, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(sex));
		
		cell = rowData.createCell(4, HSSFCell.CELL_TYPE_BOOLEAN);
		cell.setCellValue(ill);
		
		cell = rowData.createCell(5, HSSFCell.CELL_TYPE_NUMERIC);
		HSSFCellStyle cellStyle = workbook.createCellStyle();
	    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("m/d/yy h:mm"));
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue(dateOnset);	
	}

	private class MockMsExcel implements IMsExcel{

		private HSSFWorkbook workbook;
		private boolean flushWasCalled = false;
		private boolean dirtyWasCalled = false;
		
		private MockMsExcel(HSSFWorkbook workbook){
			this.workbook = workbook;
		}
		
		@Override public void flush() {
			flushWasCalled = true;
		}

		public boolean dirtyWasCalled() {
			return dirtyWasCalled;
		}

		public boolean flushWasCalled() {
			return flushWasCalled;
		}

		@Override
		public HSSFWorkbook getWorkbook() {
			return workbook;
		}

		@Override
		public void setDirty() {
			dirtyWasCalled = true;			
		}
		
	}
}
