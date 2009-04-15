package org.mesh4j.ektoo;

import java.io.File;

import org.junit.Assert;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.GoogleSpreadSheetContentAdapter;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.GoogleSpreadSheetSyncRepository;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.GoogleSpreadsheet;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.IGoogleSpreadSheet;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.ISpreadSheetToXMLMapper;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.SpreadSheetToXMLMapper;
import org.mesh4j.grameen.training.intro.adapter.googlespreadsheet.model.GSWorksheet;
import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.adapters.feed.ContentReader;
import org.mesh4j.sync.adapters.feed.ContentWriter;
import org.mesh4j.sync.adapters.feed.rss.RssSyndicationFormat;
import org.mesh4j.sync.adapters.http.HttpSyncAdapter;
import org.mesh4j.sync.adapters.http.HttpSyncAdapterFactory;
import org.mesh4j.sync.adapters.msaccess.MsAccessSyncAdapterFactory;
import org.mesh4j.sync.adapters.msexcel.MSExcelToPlainXMLMapping;
import org.mesh4j.sync.adapters.msexcel.MsExcel;
import org.mesh4j.sync.adapters.msexcel.MsExcelContentAdapter;
import org.mesh4j.sync.adapters.msexcel.MsExcelSyncRepository;
import org.mesh4j.sync.adapters.split.SplitAdapter;
import org.mesh4j.sync.id.generator.IIdGenerator;
import org.mesh4j.sync.id.generator.IdGenerator;
import org.mesh4j.sync.security.IIdentityProvider;
import org.mesh4j.sync.security.IdentityProvider;
import org.mesh4j.sync.security.LoggedInIdentityProvider;
import org.mesh4j.sync.security.NullIdentityProvider;
import org.mesh4j.sync.validations.Guard;
import org.mesh4j.sync.validations.MeshException;

public class SyncAdapterBuilder implements ISyncAdapterBuilder{
	private IGoogleSpreadSheet spreadsheet;
	
	@Override
	public ISyncAdapter createMsExcelAdapter(String sheetName,
			String idColumnName, String contentFileName, String syncFileName,
			IIdentityProvider identityProvider, IdGenerator idGenerator) {
		
		
		
		MsExcel contentExcel = null;
		MsExcel syncExcel = null;
		//TODO if file doesn't exist then create file with the help of schema
		//request the client to provide the schema
		if(contentFileName.equals(syncFileName)){
			File file = getFile(contentFileName);
			contentExcel = new MsExcel(file.getAbsolutePath());
			syncExcel = contentExcel;
		} else {
			File contentData = getFile(contentFileName);
			File syncData = getFile(syncFileName);
			
			contentExcel = new MsExcel(contentData.getAbsolutePath());
			syncExcel = new MsExcel(syncData.getAbsolutePath());
		}
		
		MsExcelSyncRepository syncRepo = new MsExcelSyncRepository(syncExcel, identityProvider, idGenerator);
		MSExcelToPlainXMLMapping mapper = new MSExcelToPlainXMLMapping(idColumnName, null);
		MsExcelContentAdapter contentAdapter = new MsExcelContentAdapter(contentExcel, mapper, sheetName);

		SplitAdapter splitAdapter = new SplitAdapter(syncRepo, contentAdapter, identityProvider);
		
		return splitAdapter;
	}


	@Override
	public ISyncAdapter createMsAccessAdapter(String baseDirectory,String rdfUrl,String sourceAlias,
										String mdbFileName, String tableName) {

		MsAccessSyncAdapterFactory msAccesSyncAdapter  = new MsAccessSyncAdapterFactory(baseDirectory,rdfUrl);
		try {
			return msAccesSyncAdapter.createSyncAdapterFromFile(sourceAlias, mdbFileName, tableName);
		} catch (Exception e) {
			throw new MeshException(e);
		}
	}
	
	@Override
	public ISyncAdapter createGoogleSpreadSheetAdapter(GoogleSpreadSheetInfo spreadSheetInfo){
		
		String idColumName = spreadSheetInfo.getIdColumnName();
		int lastUpdateColumnPosition = spreadSheetInfo.getLastUpdateColumnPosition();
		int idColumnPosition = spreadSheetInfo.getIdColumnPosition();
		String userName = spreadSheetInfo.getUserName();
		String passWord = spreadSheetInfo.getPassWord();
		String GOOGLE_SPREADSHEET_FIELD = spreadSheetInfo.getGOOGLE_SPREADSHEET_FIELD();
		
		ISpreadSheetToXMLMapper mapper = new SpreadSheetToXMLMapper(idColumName,idColumnPosition,lastUpdateColumnPosition);
		IGoogleSpreadSheet gSpreadSheet = getSpreadSheet(GOOGLE_SPREADSHEET_FIELD,userName,passWord);
		
		GSWorksheet contentWorkSheet = gSpreadSheet.getGSWorksheet(spreadSheetInfo.getSheetName());
		String syncWorkSheetName = spreadSheetInfo.getSheetName() + "_sync";
		GSWorksheet syncWorkSheet = gSpreadSheet.getGSWorksheet(syncWorkSheetName); 
	
		SplitAdapter spreadSheetAdapter = createGoogleSpreadSheetAdapter(gSpreadSheet,mapper,contentWorkSheet,
				syncWorkSheet,spreadSheetInfo.getIdentityProvider(),spreadSheetInfo.getIdGenerator());
		
		return spreadSheetAdapter;
	}
	
	public ISyncAdapter createHttpSyncAdapter(String rootUrl,String meshid, String datasetId){
		rootUrl = "http://localhost:8080/mesh4x/feeds/" + meshid + "/" +  datasetId;
		HttpSyncAdapter adapter = new HttpSyncAdapter(rootUrl, RssSyndicationFormat.INSTANCE, new LoggedInIdentityProvider(), IdGenerator.INSTANCE, ContentWriter.INSTANCE, ContentReader.INSTANCE);
		return adapter;
	}
	
	public IGoogleSpreadSheet getSpreadSheet(String spField,String userName,String passWord){
		if(spreadsheet == null){
			spreadsheet = new GoogleSpreadsheet(spField,userName,passWord);
		}
		return spreadsheet;
	}
	public static SplitAdapter createGoogleSpreadSheetAdapter(IGoogleSpreadSheet spreadsheet,ISpreadSheetToXMLMapper mapper,GSWorksheet contentWorkSheet,GSWorksheet syncWorkSheet,IIdentityProvider identityProvider,IIdGenerator idGenerator){
		
		GoogleSpreadSheetContentAdapter contentRepo = new GoogleSpreadSheetContentAdapter(spreadsheet,contentWorkSheet,mapper,contentWorkSheet.getName());
		//TODO if sync sheet doesn't exist please create the sync sheet
		GoogleSpreadSheetSyncRepository  syncRepo = new GoogleSpreadSheetSyncRepository(spreadsheet,syncWorkSheet,identityProvider,idGenerator,syncWorkSheet.getName());
		SplitAdapter splitAdapter = new SplitAdapter(syncRepo,contentRepo,identityProvider);
		return splitAdapter;
	}
	private File getFile(String fileName) {
		File file = new File(fileName);
		if(!file.exists()){
			Guard.throwsArgumentException(fileName);
		}
		return file;
	}
	
}
