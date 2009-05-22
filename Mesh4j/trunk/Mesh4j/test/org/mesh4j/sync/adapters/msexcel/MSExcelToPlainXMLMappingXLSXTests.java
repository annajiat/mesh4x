package org.mesh4j.sync.adapters.msexcel;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Assert;
import org.junit.Test;
import org.mesh4j.sync.utils.DateHelper;

public class MSExcelToPlainXMLMappingXLSXTests {

	private static final String COLUMN_NAME_ID = "id";
	private static final String COLUMN_NAME_STRING = "name";
	private static final String COLUMN_NAME_BOOLEAN = "ill";
	private static final String COLUMN_NAME_NUM = "age";
	private static final String COLUMN_NAME_DATETIME = "dateOnset";
	private static final String COLUMN_NAME_OTHER = "other";
	private static final String SHEET_NAME = "Oswego";
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldCreateMapperFailsWhenIDColumnNameIsNull(){
		new MSExcelToPlainXMLMapping(null, "columnLastUpdate");
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldCreateMapperFailsWhenIDColumnNameIsEmpty(){
		new MSExcelToPlainXMLMapping("", "columnLastUpdate");
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldCreateMapperFailsWhenLastUpdateColumnNameIsEmpty(){
		new MSExcelToPlainXMLMapping("id", "");
	}
	

	@Test
	public void shouldCreateMapperAcceptsNullLastUpdateColumnName(){
		new MSExcelToPlainXMLMapping("id", null);
	}
	
	@Test
	public void shouldConvertRowToXML(){
		
		Date date = new Date();
		HSSFWorkbook workbook = this.makeDefaultWorkbook("1", "jose", true, 30, date);
		HSSFSheet sheet = workbook.getSheet(SHEET_NAME);
		HSSFRow row = sheet.getRow(1);
		
		MSExcelToPlainXMLMapping mapping = new MSExcelToPlainXMLMapping(COLUMN_NAME_ID, null);
		Element element = mapping.convertRowToXML(workbook, sheet, row);
		
		String xml = xmlForDefaultWoorbook("1", "jose", true, 30, date, null);
		Assert.assertEquals(xml, element.asXML());
	}

	@Test
	public void shouldUpdateRowFromXML() throws DocumentException{
		
		HSSFWorkbook workbook = makeDefaultWorkbook("1", "jose", true, 30, new Date());
		
		HSSFSheet sheet = workbook.getSheet(SHEET_NAME);
		HSSFRow row = sheet.getRow(1);

		Date newDate = new Date();
		String xml = xmlForDefaultWoorbook("1", "maria", false, 10, newDate, null);
		Element payload = DocumentHelper.parseText(xml).getRootElement();
		
		MSExcelToPlainXMLMapping mapping = new MSExcelToPlainXMLMapping(COLUMN_NAME_ID, null);
		mapping.appliesXMLToRow(workbook, sheet, row, payload);
		
		HSSFCell cell = row.getCell(0);
		Assert.assertNotNull(cell);
		Assert.assertEquals("1", cell.getRichStringCellValue().getString());
		
		cell = row.getCell(1);
		Assert.assertNotNull(cell);
		Assert.assertEquals("maria", cell.getRichStringCellValue().getString());
		
		cell = row.getCell(2);
		Assert.assertNotNull(cell);
		Assert.assertEquals(false, cell.getBooleanCellValue());
		
		cell = row.getCell(3);
		Assert.assertNotNull(cell);
		Assert.assertEquals(10d, cell.getNumericCellValue());
		
		cell = row.getCell(4);
		Assert.assertNotNull(cell);
		Assert.assertEquals(DateHelper.normalize(newDate).getTime(), cell.getDateCellValue().getTime());
	}
	
	@Test
	public void shouldUpdateNewRowFromXML() throws DocumentException{
		
		HSSFWorkbook workbook = makeDefaultWorkbook("1", "jose", true, 30, new Date());
		
		HSSFSheet sheet = workbook.getSheet(SHEET_NAME);
		HSSFRow row = sheet.createRow(sheet.getFirstRowNum() + 1);

		Date newDate = new Date();
		String xml = xmlForDefaultWoorbook("1", "maria", false, 10, newDate, null);
		Element payload = DocumentHelper.parseText(xml).getRootElement();
		
		MSExcelToPlainXMLMapping mapping = new MSExcelToPlainXMLMapping(COLUMN_NAME_ID, null);
		mapping.appliesXMLToRow(workbook, sheet, row, payload);
		
		HSSFCell cell = row.getCell(0);
		Assert.assertNotNull(cell);
		Assert.assertEquals("1", cell.getRichStringCellValue().getString());
		
		cell = row.getCell(1);
		Assert.assertNotNull(cell);
		Assert.assertEquals("maria", cell.getRichStringCellValue().getString());
		
		cell = row.getCell(2);
		Assert.assertNotNull(cell);
		Assert.assertEquals("false", cell.getRichStringCellValue().getString());
		
		cell = row.getCell(3);
		Assert.assertNotNull(cell);
		Assert.assertEquals("10", cell.getRichStringCellValue().getString());
		
		cell = row.getCell(4);
		Assert.assertNotNull(cell);
		Assert.assertEquals(DateHelper.formatW3CDateTime(newDate), cell.getRichStringCellValue().getString());
	}
	

	@Test
	public void shouldAddNewCellFromXML() throws DocumentException{
		Date newDate = new Date();
		String xml = xmlForDefaultWoorbook("1", "maria", false, 10, newDate, "yes");
		Element payload = DocumentHelper.parseText(xml).getRootElement();
		
		Date date = new Date();
		HSSFWorkbook workbook = makeDefaultWorkbook("1", "maria", true, 30, date);
		HSSFSheet sheet = workbook.getSheet(SHEET_NAME);
		HSSFRow row = sheet.getRow(1);
		
		MSExcelToPlainXMLMapping mapping = new MSExcelToPlainXMLMapping(COLUMN_NAME_ID, null);
		mapping.appliesXMLToRow(workbook, sheet, row, payload);
		
		HSSFCell cell = row.getCell(0);
		Assert.assertNotNull(cell);
		Assert.assertEquals("1", cell.getRichStringCellValue().getString());
		
		cell = row.getCell(1);
		Assert.assertNotNull(cell);
		Assert.assertEquals("maria", cell.getRichStringCellValue().getString());
		
		cell = row.getCell(2);
		Assert.assertNotNull(cell);
		Assert.assertEquals(false, cell.getBooleanCellValue());
		
		cell = row.getCell(3);
		Assert.assertNotNull(cell);
		Assert.assertEquals(10d, cell.getNumericCellValue());
		
		cell = row.getCell(4);
		Assert.assertNotNull(cell);
		Assert.assertEquals(DateHelper.normalize(newDate).getTime(), cell.getDateCellValue().getTime());

		cell = row.getCell(5);
		Assert.assertNotNull(cell);
		Assert.assertEquals("yes", cell.getRichStringCellValue().getString());
	
	}
	private HSSFWorkbook makeDefaultWorkbook(String columnValue1, String columnValue2, boolean columnValue3, double columnValue4, Date columnValue5) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet(SHEET_NAME);
		
		HSSFRow row = sheet.createRow(0);
		
		HSSFCell cell = row.createCell(0, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(COLUMN_NAME_ID));
		
		cell = row.createCell(1, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(COLUMN_NAME_STRING));
		
		cell = row.createCell(2, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(COLUMN_NAME_BOOLEAN));
		
		cell = row.createCell(3, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(COLUMN_NAME_NUM));
		
		cell = row.createCell(4, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(COLUMN_NAME_DATETIME));
		
		row = sheet.createRow(1);
		
		cell = row.createCell(0, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(columnValue1));
		
		cell = row.createCell(1, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(columnValue2));
		
		cell = row.createCell(2, HSSFCell.CELL_TYPE_BOOLEAN);
		cell.setCellValue(columnValue3);
		
		cell = row.createCell(3, HSSFCell.CELL_TYPE_NUMERIC);
		cell.setCellValue(columnValue4);
		
		cell = row.createCell(4, HSSFCell.CELL_TYPE_NUMERIC);
		HSSFCellStyle cellStyle = workbook.createCellStyle();
	    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("m/d/yy h:mm"));
	    cell.setCellStyle(cellStyle);
		cell.setCellValue(columnValue5);

		return workbook;
	}	

	private String xmlForDefaultWoorbook(String columnValue1, String columnValue2, boolean columnValue3, long columnValue4, Date columnValue5, String columnValue6){
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		sb.append(SHEET_NAME);
		sb.append(">");
		
		addXMLElement(sb, COLUMN_NAME_ID, columnValue1);
		addXMLElement(sb, COLUMN_NAME_STRING, columnValue2);
		addXMLElement(sb, COLUMN_NAME_BOOLEAN, String.valueOf(columnValue3));
		addXMLElement(sb, COLUMN_NAME_NUM, String.valueOf(columnValue4));
		addXMLElement(sb, COLUMN_NAME_DATETIME, DateHelper.formatW3CDateTime(columnValue5));
		
		if(columnValue6 != null){
			addXMLElement(sb, COLUMN_NAME_OTHER, columnValue6);
		}
		sb.append("</");
		sb.append(SHEET_NAME);
		sb.append(">");
		
		return sb.toString();
	}

	private void addXMLElement(StringBuffer sb, String elememtName, String elementValue) {
		sb.append("<");
		sb.append(elememtName);
		sb.append(">");
		sb.append(elementValue);
		sb.append("</");
		sb.append(elememtName);
		sb.append(">");
	}
}
