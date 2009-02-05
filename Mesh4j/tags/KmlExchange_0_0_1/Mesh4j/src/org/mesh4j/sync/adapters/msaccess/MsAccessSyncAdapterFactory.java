package org.mesh4j.sync.adapters.msaccess;

import java.io.File;

import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.adapters.ISyncAdapterFactory;
import org.mesh4j.sync.adapters.feed.rss.RssSyndicationFormat;
import org.mesh4j.sync.adapters.hibernate.HibernateContentAdapter;
import org.mesh4j.sync.adapters.hibernate.HibernateSessionFactoryBuilder;
import org.mesh4j.sync.adapters.hibernate.HibernateSyncRepository;
import org.mesh4j.sync.adapters.hibernate.IHibernateSessionFactoryBuilder;
import org.mesh4j.sync.adapters.split.SplitAdapter;
import org.mesh4j.sync.id.generator.IdGenerator;
import org.mesh4j.sync.parsers.SyncInfoParser;
import org.mesh4j.sync.security.IIdentityProvider;
import org.mesh4j.sync.security.NullIdentityProvider;
import org.mesh4j.sync.validations.Guard;

import sun.jdbc.odbc.JdbcOdbcDriver;

public class MsAccessSyncAdapterFactory implements ISyncAdapterFactory {

	public static final String SOURCE_TYPE = "MsAccess";
	private static final String MS_ACCESS = SOURCE_TYPE+":";		
	public static final String DEFAULT_SEPARATOR = "@";
	
	// MODEL VARIABLES
	private String baseDirectory;

	// BUSINESS METHODS
	public MsAccessSyncAdapterFactory(String baseDirectory){
		super();
		Guard.argumentNotNull(baseDirectory, "baseDirectory");
		
		this.baseDirectory = baseDirectory;
	}	
	
	public static SplitAdapter createSyncAdapterFromFile(String mdbFileName, String tableName, String mappingsDirectory) throws Exception{
		if(mdbFileName == null || mdbFileName.length() == 0 || tableName == null || tableName.length() == 0){
			return null;
		}
		
		String contentMappingFileName = mappingsDirectory + "/" + tableName + ".hbm.xml";
		String syncMappingFileName =  mappingsDirectory + "/" + tableName + "_sync.hbm.xml";
		
		MsAccessHibernateMappingGenerator.createMappingsIfAbsent(mdbFileName, tableName, contentMappingFileName, syncMappingFileName);
//		MsAccessHibernateMappingGenerator.createSyncTableIfAbsent(mdbFileName, tableName);
		return createSyncAdapterFromFile(mdbFileName, tableName, contentMappingFileName, syncMappingFileName);	
	}

	public static SplitAdapter createSyncAdapterFromFile(String mdbFileName, String tableName, String contentMappingFileName, String syncMappingFileName){
		String dbURL = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + mdbFileName.trim() + ";DriverID=22;READONLY=false}"; 
		return createSplitAdapter(dbURL, tableName, "", "", contentMappingFileName, syncMappingFileName);	
	}

	public static SplitAdapter createSyncAdapterFromODBC(String odbcName, String tableName, String user, String password, String contentMappingFileName, String syncMappingFileName){
		String dbURL = "jdbc:odbc:"+odbcName;
		return createSplitAdapter(tableName, user, password, dbURL, contentMappingFileName, syncMappingFileName);	
	}
	
	private static SplitAdapter createSplitAdapter(String dbURL, String tableName, String user, String password, String contentMappingFileName, String syncMappingFileName) {
		HibernateSessionFactoryBuilder builder = createHibernateSessionBuilder(dbURL, tableName, user, password, contentMappingFileName, syncMappingFileName);
		SyncInfoParser syncInfoParser = new SyncInfoParser(RssSyndicationFormat.INSTANCE, NullIdentityProvider.INSTANCE, IdGenerator.INSTANCE);
		HibernateSyncRepository syncRepository = new HibernateSyncRepository(builder, syncInfoParser);
		HibernateContentAdapter contentAdapter = new HibernateContentAdapter(builder, tableName);
		return new SplitAdapter(syncRepository, contentAdapter, NullIdentityProvider.INSTANCE);
	}
	
	public static HibernateSessionFactoryBuilder createHibernateSessionBuilder(String dbURL, String tableName, String user, String password, String contentMappingFileName, String syncMappingFileName) {
		
		HibernateSessionFactoryBuilder builder = new HibernateSessionFactoryBuilder();
		builder.setProperty("hibernate.dialect", MsAccessDialect.class.getName());
		builder.setProperty("hibernate.connection.driver_class", JdbcOdbcDriver.class.getName());
		builder.setProperty("hibernate.connection.url", dbURL);
		builder.setProperty("hibernate.connection.username",user);
		builder.setProperty("hibernate.connection.password",password);
		builder.addMapping(new File(contentMappingFileName));
		builder.addMapping(new File(syncMappingFileName));
		return builder;
	}

	public static boolean isMsAccess(String sourceDefinition) {
		return sourceDefinition.startsWith(MS_ACCESS);
	}

	public static boolean isValidAccessTable(String mdbFileName, String mdbTableName) {
		return MsAccessHelper.existTable(mdbFileName, mdbTableName);
	}
	
	// ISyncAdapterFactry methods
	
	@Override
	public boolean acceptsSource(String sourceId, String sourceDefinition) {
		return sourceDefinition != null && isMsAccess(sourceDefinition);
	}

	@Override
	public SplitAdapter createSyncAdapter(String sourceAlias, String sourceDefinition, IIdentityProvider identityProvider) throws Exception {
		String mdbFileName = getFileName(sourceDefinition);
		String tableName = getTableName(sourceDefinition);
		SplitAdapter msAccessAdapter = createSyncAdapterFromFile(mdbFileName, tableName, this.baseDirectory);
		return msAccessAdapter;
	}

	@Override
	public String getSourceType() {
		return SOURCE_TYPE;
	}

	public static String createSourceDefinition(String mdbFileName, String mdbTableName) {
		return MS_ACCESS + mdbFileName + DEFAULT_SEPARATOR + mdbTableName;
	}

	private static String getDataSource(String sourceDefinition) {
		if(isMsAccess(sourceDefinition)){
			return sourceDefinition.substring(MS_ACCESS.length(), sourceDefinition.length());
		} else {
			return sourceDefinition;
		}
	}
	
	public static String getFileName(String sourceDefinition) {
		String source = MsAccessSyncAdapterFactory.getDataSource(sourceDefinition);
		String[] elements = source.split(DEFAULT_SEPARATOR);
		//String tableName = elements[1];
		String fileName= elements[0];
		return fileName;
	}

	public static String getTableName(String sourceDefinition) {
		String source = MsAccessSyncAdapterFactory.getDataSource(sourceDefinition);
		String[] elements = source.split(DEFAULT_SEPARATOR);
		String tableName = elements[1];
		//String fileName= elements[0];
		return tableName;
	}

	// TODO (JMT) Adapter should be supports dynamic source definition changes
	public void changeSourceDefinition(String sourceAlias, String sourceDefinition, ISyncAdapter syncAdapter) {
		SplitAdapter splitAdapter = (SplitAdapter) syncAdapter;
		
		String mdbFileName = getFileName(sourceDefinition);
		String tableName = getTableName(sourceDefinition);
		String contentMappingFileName = this.baseDirectory + "/" + tableName + ".hbm.xml";
		String syncMappingFileName =  this.baseDirectory + "/" + tableName + "_sync.hbm.xml";
		String user = "";
		String password = "";

		String dbURL = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + mdbFileName.trim() + ";DriverID=22;READONLY=false}"; 
		
		IHibernateSessionFactoryBuilder builder = createHibernateSessionBuilder(dbURL, tableName, user, password, contentMappingFileName, syncMappingFileName);
		
		HibernateContentAdapter contentAdapter = (HibernateContentAdapter) splitAdapter.getContentAdapter();
		contentAdapter.initializeSessionFactory(builder, tableName);
		
		HibernateSyncRepository syncRepo = (HibernateSyncRepository) splitAdapter.getSyncRepository();
		syncRepo.initializeSessionFactory(builder);
	}
}
