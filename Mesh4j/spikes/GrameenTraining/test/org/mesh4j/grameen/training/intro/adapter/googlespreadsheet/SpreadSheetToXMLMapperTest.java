package org.mesh4j.grameen.training.intro.adapter.googlespreadsheet;

import java.util.Map;

import junit.framework.Assert;

import org.dom4j.Element;
import org.junit.Test;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model.GSRow;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model.GSWorksheet;
import org.mesh4j.sync.utils.XMLHelper;

public class SpreadSheetToXMLMapperTest {

	@Test
	public void ShouldConvertRowToXMLPayload(){
		
		String rawDataAsXML = "<user><name>Raju</name><age>18</age><city>Dhaka</city><country>Bangladesh</country></user>";
		//String user_Marcelo = "<user><name>Marcelo</name><age>19</age><city>Bandaras</city><country>Arjentian</country></user>";
		//String user_Sharif = "<user><name>Sharif</name><age>18</age><city>Dhaka</city><country>Bangladesh</country></user>";
		
		SpreadSheetToXMLMapper mapper = new SpreadSheetToXMLMapper("id","lastupdateColumnName");
		
		GoogleSpreadsheet spreadsheet = new GoogleSpreadsheet("pTOwHlskRe06LOcTpClQ-Bw","saiful.raju@gmail.com","ir0np0cket");
		for(Map.Entry<String, GSWorksheet> spSheet : spreadsheet.getGSSpreadsheet().getWorksheetList().entrySet()){
			String key = spSheet.getKey();
			GSWorksheet workSheet = spSheet.getValue();
			for(Map.Entry<String, GSRow> gsRowMap :workSheet.getRowList().entrySet()){
				GSRow row = gsRowMap.getValue();
				Element xmlElement = mapper.convertRowToXML(row, workSheet);
				Assert.assertEquals(xmlElement.asXML(), rawDataAsXML);
				break;
			}
		}
		
	}
	
	@Test
	public void ShouldConvertXMLToRow(){
		String rawDataAsXML = "<user><name>Raju</name><age>18</age><city>Dhaka</city><country>Bangladesh</country></user>";
		Element payLoad = XMLHelper.parseElement(rawDataAsXML);
		
		SpreadSheetToXMLMapper mapper = new SpreadSheetToXMLMapper("id","lastupdateColumnName");
		GoogleSpreadsheet spreadsheet = new GoogleSpreadsheet("pTOwHlskRe06LOcTpClQ-Bw","saiful.raju@gmail.com","ir0np0cket");
		
		for(Map.Entry<String, GSWorksheet> spSheetMpa : spreadsheet.getGSSpreadsheet().getWorksheetList().entrySet()){
			GSWorksheet workSheet = spSheetMpa.getValue();
			GSRow row = mapper.convertXMLElementToRow(payLoad, null, null);
			Element xmlElement = mapper.convertRowToXML(row, workSheet);
			Assert.assertEquals(xmlElement.asXML(), rawDataAsXML);
			break;
		}
	}
	
	
}
