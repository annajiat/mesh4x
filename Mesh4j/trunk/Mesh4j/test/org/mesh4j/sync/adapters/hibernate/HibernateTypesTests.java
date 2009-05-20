package org.mesh4j.sync.adapters.hibernate;

import org.junit.Test;
import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.SyncEngine;
import org.mesh4j.sync.adapters.feed.Feed;
import org.mesh4j.sync.adapters.feed.FeedAdapter;
import org.mesh4j.sync.adapters.feed.rss.RssSyndicationFormat;
import org.mesh4j.sync.adapters.msexcel.MsExcelRDFSyncAdapterFactory;
import org.mesh4j.sync.adapters.split.SplitAdapter;
import org.mesh4j.sync.id.generator.IdGenerator;
import org.mesh4j.sync.payload.schema.rdf.RDFSchema;
import org.mesh4j.sync.security.NullIdentityProvider;
import org.mesh4j.sync.test.utils.TestHelper;

public class HibernateTypesTests {

	@Test
	public void shouldSyncDecimalVsFeed(){
			
		ISyncAdapter adapterSource = HibernateSyncAdapterFactory.createHibernateAdapter(
			"jdbc:mysql:///mesh4xdb", 
			"root", 
			"", 
			com.mysql.jdbc.Driver.class,
			org.hibernate.dialect.MySQLDialect.class,
			"mesh_example_1", 
			"mesh_example_1_sync", 
			"http://mesh4x/test", 
			TestHelper.baseDirectoryRootForTest());
		
		String fileName = TestHelper.fileName(IdGenerator.INSTANCE.newID() + "_feed_test_hibernate_types.xml");
		Feed feed = new Feed("test", "test", "http://mesh4x/test/testHibernateTypes");
		ISyncAdapter adapterTarget = new FeedAdapter(fileName, NullIdentityProvider.INSTANCE, IdGenerator.INSTANCE, RssSyndicationFormat.INSTANCE, feed);
		
		SyncEngine syncEngine = new SyncEngine(adapterSource, adapterTarget);
		TestHelper.syncAndAssert(syncEngine);
	}
	
	@Test
	public void shouldSyncDecimalVsMsExcel(){
		
		String rdfBaseURL = "http://mesh4x/test";
		String tableName = "mesh_example_1";
		
		SplitAdapter adapterSource = HibernateSyncAdapterFactory.createHibernateAdapter(
			"jdbc:mysql:///mesh4xdb", 
			"root", 
			"", 
			com.mysql.jdbc.Driver.class,
			org.hibernate.dialect.MySQLDialect.class,
			tableName, 
			tableName + "_sync", 
			rdfBaseURL, 
			TestHelper.baseDirectoryRootForTest());

		RDFSchema rdfSchema = (RDFSchema)((HibernateContentAdapter)adapterSource.getContentAdapter()).getMapping().getSchema();
		
		String excelFileName = TestHelper.fileName(IdGenerator.INSTANCE.newID() + "_test_hibernate_types.xls");
		MsExcelRDFSyncAdapterFactory msExcelFactory = new MsExcelRDFSyncAdapterFactory(rdfBaseURL);
		ISyncAdapter adapterTarget = msExcelFactory.createSyncAdapter(excelFileName, tableName, "uid", NullIdentityProvider.INSTANCE, rdfSchema);
					
		SyncEngine syncEngine = new SyncEngine(adapterSource, adapterTarget);
		TestHelper.syncAndAssert(syncEngine);
		
		TestHelper.syncAndAssert(syncEngine);
	}
}
