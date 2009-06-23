package org.mesh4j.ektoo;

import java.util.HashMap;

import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.adapters.feed.ISyndicationFormat;
import org.mesh4j.sync.adapters.http.HttpSyncAdapter;
import org.mesh4j.sync.payload.schema.rdf.IRDFSchema;
/**
 * The builder interface for creating adapter with the
 * minimum requirements
 */
public interface ISyncAdapterBuilder {

	/**
	 * creates adapter for excel repository
	 * 
	 * @param contentFileName
	 *            ,the content file which actually contains the provided sheet
	 * @param sheetName
	 *            , the physical sheet name of the excel file
	 * @param idColumnName
	 *            , the name of the identity column of entity
	 * @param isRDF
	 * 			,which actually represents if the adapter going to create is RDF based or without RDF.<l>
	 * 			if user make to true then created excel adapter will be based on RDF and it must have RDF schema<l>
	 * 			and user can easilly get the schema from its Content adapter.
	 */
	public ISyncAdapter createMsExcelAdapter(String contentFileName, String sheetName, String idColumnName,boolean isRDF);
	
	/**
	 * creates adapter for excel repository
	 * 
	 * @param contentFileName
	 *            ,the content file which actually contains the provided sheet.
	 * @param sheetName
	 *            , the physical sheet name of the excel file.
	 * @param idColumnName
	 *            , the name of the identity column of entity.
	 * @param  sourceSchema
	 * 			 ,the rdf schema of which excel repository should be.  
	 * @return ISyncAdapter,instance of the excel adapter         
	 */
	public ISyncAdapter createMsExcelAdapter(String contentFileName, String sheetName, String idColumnName, IRDFSchema sourceSchema);
	
	/**
	 * Create adapter for access adapter
	 * 
	 * @param sourceAlias
	 *            , the alias for source
	 * @param mdbFileName
	 *            ,the name of the ms access database file name.
	 * @param tableName
	 *            ,the table name of the ms access database to be applied for
	 *            sync.
	 * @return ISyncAdapter
	 */
	public ISyncAdapter createMsAccessAdapter(String mdbFileName,
			String tableName);

	/**
	 * Creates MsAccess adapter for multi table
	 * 
	 * @param mdbFileName
	 * @param tables
	 * @return
	 */
	public ISyncAdapter createMsAccessMultiTablesAdapter(String mdbFileName, String [] tables);
	/**
	 * TODO create documentation (raju)
	 * 
	 * @param spreadSheetInfo
	 * @param rdfSchema
	 * @return
	 */
	public ISyncAdapter createPlainXMLBasedGoogleSpreadSheetAdapter(
			GoogleSpreadSheetInfo spreadSheetInfo);

	/**
	 * TODO create documentation (raju)
	 * @param serverUrl
	 * @param meshGroup
	 * @param dataSetId
	 * @return
	 */
	public ISyncAdapter createHttpSyncAdapter(String serverUrl,String meshGroup,String dataSetId);
	/**
	 * TODO create documentation (raju)
	 * 
	 * @param rootUrl
	 * @param meshId
	 * @param dataSetId
	 * @return
	 */
	public ISyncAdapter createHttpSyncAdapter(String serverUrl, String meshGroup, String dataSetId, IRDFSchema rdfSchema);

	/**
	 * Create {@link HttpSyncAdapter} for multi dataset 
	 * 
	 * @param serverUrl
	 * @param meshGroup
	 * @param rdfSchemas
	 * @return
	 */
	public ISyncAdapter createHttpSyncAdapterForMultiDataset(String serverUrl, String meshGroup, HashMap<IRDFSchema, String> rdfSchemas);
	
	/**
	 * TODO create documentation (raju)
	 * 
	 * @param userName
	 * @param password
	 * @param hostName
	 * @param portNo
	 * @param databaseName
	 * @param tableName
	 * @return
	 */
	public ISyncAdapter createMySQLAdapter(String userName, String password,
			String hostName, int portNo, String databseName, String tableName);
	
	/**
	 * TODO create documentation (raju)
	 */
	public ISyncAdapter createKMLAdapter(String kmlFileName);
	
	
	/**
	 * TODO create documentation (raju)
	 */
	public ISyncAdapter createFeedAdapter(String title, String description,
			String link, String fileName, ISyndicationFormat syndicationFormat);


	public ISyncAdapter createRdfBasedGoogleSpreadSheetAdapter(
			GoogleSpreadSheetInfo spreadSheetInfo, IRDFSchema sourceSchema);

	/**
	 * TODO create documentation (raju)
	 */
	public ISyncAdapter createFolderAdapter(String folderName);

	/**
	 * TODO create documentation (raju)
	 */
	public String generateMySqlFeed(String userName, String password, String hostName,
			int portNo, String databaseName, String tableName);

	/**
	 * This returns a composite adapter for sync multiple table using mysql/hibernate adapter 
	 * @param userName
	 * @param password
	 * @param hostName
	 * @param portNo
	 * @param databaseName
	 * @param tables
	 * @return
	 */
	public ISyncAdapter createMySQLAdapterForMultiTables(String userName,
			String password, String hostName, int portNo, String databaseName,
			String[] tables);

	/**
	 * This returns a composite adapter for sync multiple sheet using MsExcel adapter 
	 * @param contentFileName
	 * @param sheets
	 * @return
	 */
	public ISyncAdapter createMsExcelAdapterForMultiSheets(String contentFileName,
			HashMap<IRDFSchema, String> sheets);

	public ISyncAdapter createZipFeedAdapter(String zipFileName);

}
