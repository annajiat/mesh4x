package org.mesh4j.sync.adapters.multi.repositories;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import junit.framework.Assert;

import org.junit.Test;
import org.mesh4j.sync.SyncEngine;
import org.mesh4j.sync.adapters.hibernate.HibernateContentAdapter;
import org.mesh4j.sync.adapters.hibernate.HibernateSyncAdapterFactory;
import org.mesh4j.sync.adapters.msexcel.IMsExcel;
import org.mesh4j.sync.adapters.msexcel.MsExcel;
import org.mesh4j.sync.adapters.msexcel.MsExcelContentAdapter;
import org.mesh4j.sync.adapters.msexcel.MsExcelRDFSyncAdapterFactory;
import org.mesh4j.sync.adapters.split.SplitAdapter;
import org.mesh4j.sync.payload.schema.rdf.IRDFSchema;
import org.mesh4j.sync.security.IIdentityProvider;
import org.mesh4j.sync.security.NullIdentityProvider;
import org.mesh4j.sync.test.utils.TestHelper;
import org.mesh4j.sync.utils.FileUtils;

public class MsAccessVsMySqlSyncTestsRnD {

	@Test
	public void shouldSyncExcelWithBlankRowBeforeHeaderAndMysql() throws IOException{

		String rdfBaseURL = "http://localhost:8080/mesh4x/myExample";
		String tableName;
		String sheetName = tableName = "mesh_example";
		String[] idColumnNames =  new String[]{"user id"};
		String lastUpdateColumnName = null;
		IIdentityProvider identityProvider = NullIdentityProvider.INSTANCE;

		String originalFileName = this.getClass().getResource("excelWithBlankInHeader.xls").getFile();
		String newFileName = TestHelper.fileName("ExcelToMysqlTest.xls");
		FileUtils.copyFile(originalFileName, newFileName);
		IMsExcel excel = new MsExcel(newFileName);

		SplitAdapter adapterSource = MsExcelRDFSyncAdapterFactory.createSyncAdapter(excel, sheetName, idColumnNames, lastUpdateColumnName, identityProvider, rdfBaseURL);

		IRDFSchema rdfSchemaSource = (IRDFSchema)((MsExcelContentAdapter)adapterSource.getContentAdapter()).getSchema();

		//prepare mysql adapter
		String dbname = "mesh4xdb";
		String username = "root";
		String password = "admin";

		createMysqlTableForTest(dbname, username , password, tableName, idColumnNames[0], true);

		SplitAdapter adapterTarget = HibernateSyncAdapterFactory.createHibernateAdapter(
				"jdbc:mysql:///"+dbname+"",
				username,
				password,
				com.mysql.jdbc.Driver.class,
				org.hibernate.dialect.MySQLDialect.class,
				tableName,
				rdfBaseURL,
				TestHelper.baseDirectoryRootForTest(),
				identityProvider,
				null);

		IRDFSchema rdfSchemaTarget = (IRDFSchema)((HibernateContentAdapter)adapterTarget.getContentAdapter()).getMapping().getSchema();

		Assert.assertNotNull(adapterTarget);
		//Assert.assertEquals(rdfSchemaSource.asXML(), rdfSchemaTarget.asXML());
		Assert.assertTrue(rdfSchemaSource.isCompatible(rdfSchemaTarget));

		SyncEngine syncEngine = new SyncEngine(adapterTarget, adapterSource);
		TestHelper.assertSync(syncEngine);
	}

	public static void createMysqlTableForTest(String dbname, String username,
			String password, String dataTablename, String idColumn,	boolean addSampleData) {
		String url = "jdbc:mysql://localhost:3306/mysql";
		String drivername = "com.mysql.jdbc.Driver";

		String syncTableName = dataTablename+"_sync";

		//String dropDatabase = "DROP DATABASE IF EXISTS "+ dbname+"; ";
		//String createDatabase =	"CREATE DATABASE "+dbname+"; ";
		//String allowGrant = "GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP ON "+dbname+".* TO "+username+"@localhost IDENTIFIED BY '"+password+"';";

		String dropDataTable = "DROP TABLE IF EXISTS "+dbname+"."+dataTablename+"; ";
		String createDataTable=  "CREATE TABLE  "+dbname+"."+dataTablename+" ( " +
				""+idColumn+" varchar(50) NOT NULL, " +
				"name varchar(50) " + "default NULL," +
				"pass varchar(50) default NULL, PRIMARY KEY  USING BTREE ("+idColumn+") " +
				") ENGINE=InnoDB DEFAULT CHARSET=latin1;";

		String dropSyncTable = "DROP TABLE IF EXISTS "+dbname+"."+syncTableName+"; ";

		String createSyncTable = "CREATE TABLE  "+dbname+"."+syncTableName+" ( " +
				"  sync_id varchar(50) NOT NULL, " +
				"  entity_name varchar(50) default NULL, " +
				"  entity_id varchar(255) default NULL, " +
				"  entity_version varchar(50) default NULL, " +
				"  sync_data text, " +
				"  PRIMARY KEY  (sync_id) " +
				") ENGINE=InnoDB DEFAULT CHARSET=latin1;";

		String sampleData = "INSERT INTO "+dbname+"."+dataTablename+" VALUES ('P3','Marcelo','Argentina');";

		Connection con;
		Statement stmt;

		try {
			Class.forName(drivername);
			con = DriverManager.getConnection(url,username, password);
			stmt = con.createStatement();

			//stmt.addBatch(dropDatabase);
			//stmt.addBatch(createDatabase);
			//stmt.addBatch(allowGrant);

			stmt.addBatch(dropDataTable);
			stmt.addBatch(createDataTable);

			stmt.addBatch(dropSyncTable);
			stmt.addBatch(createSyncTable);

			if(addSampleData)
				stmt.addBatch(sampleData);

			stmt.executeBatch();

			stmt.close();
			con.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
