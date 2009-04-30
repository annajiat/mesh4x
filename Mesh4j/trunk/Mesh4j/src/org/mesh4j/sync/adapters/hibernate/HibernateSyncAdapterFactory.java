package org.mesh4j.sync.adapters.hibernate;

import java.io.File;
import java.text.MessageFormat;
import java.util.Iterator;

import org.hibernate.Hibernate;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.SchemaSelection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.hbm2x.HibernateMappingExporter;
import org.hibernate.tool.hbmlint.detector.TableSelectorStrategy;
import org.hibernate.type.Type;
import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.adapters.ISyncAdapterFactory;
import org.mesh4j.sync.adapters.feed.rss.RssSyndicationFormat;
import org.mesh4j.sync.adapters.split.SplitAdapter;
import org.mesh4j.sync.id.generator.IdGenerator;
import org.mesh4j.sync.parsers.SyncInfoParser;
import org.mesh4j.sync.payload.schema.rdf.RDFSchema;
import org.mesh4j.sync.security.IIdentityProvider;
import org.mesh4j.sync.security.NullIdentityProvider;
import org.mesh4j.sync.utils.FileUtils;
import org.mesh4j.sync.validations.Guard;
import org.mesh4j.sync.validations.MeshException;

public class HibernateSyncAdapterFactory implements ISyncAdapterFactory{

	// BUSINESS METHODS
	
	// TODO (JMT) ISyncAdapterFactory methods read JDBC url connections
	@Override
	public boolean acceptsSource(String sourceId, String sourceDefinition) {
		return false;
	}

	@Override
	public ISyncAdapter createSyncAdapter(String sourceAlias, String sourceDefinition, IIdentityProvider identityProvider) throws Exception {
		return null;
	}

	@Override
	public String getSourceType() {
		return null;
	}
	
	// ADAPTER CREATION
	@SuppressWarnings("unchecked")
	public static ISyncAdapter createHibernateAdapter(String connectionURL, String user, String password, Class driverClass, Class dialectClass, String tableName, String syncTableName, String rdfURL, String baseDirectory) {
	
		HibernateSessionFactoryBuilder builder = createHibernateFactoryBuilder(connectionURL, user, password, driverClass, dialectClass, null);
		
		PersistentClass contentMapping = createMappings(builder, tableName, syncTableName, baseDirectory);
		
		RDFSchema schema = createRDFSchema(tableName, rdfURL, contentMapping);
		builder.addRDFSchema(schema);
		
		SyncInfoParser syncInfoParser = new SyncInfoParser(RssSyndicationFormat.INSTANCE, NullIdentityProvider.INSTANCE, IdGenerator.INSTANCE, syncTableName);
		
		HibernateSyncRepository syncRepository = new HibernateSyncRepository(builder, syncInfoParser);
		HibernateContentAdapter contentAdapter = new HibernateContentAdapter(builder, tableName);
		SplitAdapter splitAdapter = new SplitAdapter(syncRepository, contentAdapter, NullIdentityProvider.INSTANCE);
		return splitAdapter;
	}

	@SuppressWarnings("unchecked")
	private static RDFSchema createRDFSchema(String tableName, String rdfURL, PersistentClass mapping) {
		RDFSchema rdfSchema = new RDFSchema(tableName, rdfURL, tableName);
		
		Property property = mapping.getIdentifierProperty();
		addRDFProperty(rdfSchema, property);
		
		Iterator<Property> it = mapping.getPropertyIterator();
		while(it.hasNext()){
			property = it.next();
			addRDFProperty(rdfSchema, property);
		}
		return rdfSchema;
	}
	
	// TODO (JMT) RDF: improve Hibernate type to RDF type mappings
	private static void addRDFProperty(RDFSchema rdfSchema, Property property) {
		String propertyName = property.getName();
		Type type = property.getType();
		
		if(Hibernate.STRING.equals(type)){
			rdfSchema.addStringProperty(propertyName, propertyName, "en");
		}
		
		if(Hibernate.BOOLEAN.equals(type)){
			rdfSchema.addBooleanProperty(propertyName, propertyName, "en");
		}
		
		if(Hibernate.DATE.equals(type) || Hibernate.TIMESTAMP.equals(type)){
			rdfSchema.addDateTimeProperty(propertyName, propertyName, "en");
		}

		if(Hibernate.LONG.equals(type)){
			rdfSchema.addLongProperty(propertyName, propertyName, "en");
		}
		
		if(Hibernate.INTEGER.equals(type)){
			rdfSchema.addIntegerProperty(propertyName, propertyName, "en");
		}

		if(Hibernate.DOUBLE.equals(type)){
			rdfSchema.addDoubleProperty(propertyName, propertyName, "en");
		}

		if(Hibernate.BIG_DECIMAL.equals(type)){
			rdfSchema.addDecimalProperty(propertyName, propertyName, "en");
		}
	}
	
	public static PersistentClass createMappings(HibernateSessionFactoryBuilder builder, String tableName, String syncTableName, String baseDirectory) {
		autodiscoveryMappings(builder, tableName, syncTableName, baseDirectory);

		File contentMapping = new File(baseDirectory + tableName+".hbm.xml");
		if(!contentMapping.exists()){
			Guard.throwsException("INVALID_TABLE_NAME");
		}
		
		boolean mustCreateTables = false;
		File syncFileMapping = new File(baseDirectory + syncTableName+".hbm.xml");
		if(!syncFileMapping.exists()){
			try{
				byte[] templateBytes = FileUtils.read(HibernateSyncAdapterFactory.class.getResource("syncMappingTemplate.xml").getFile());
				String template = new String(templateBytes, "UTF-8");		
				String xml = MessageFormat.format(template, syncTableName);
				FileUtils.write(syncFileMapping.getCanonicalPath(), xml.getBytes());
				mustCreateTables = true;
			} catch (Exception e) {
				throw new MeshException(e);
			}
		}
		
		builder.addMapping(syncFileMapping);
		builder.addMapping(contentMapping);
		
		
		Configuration cfg = builder.buildConfiguration();
		if(mustCreateTables){			
			SchemaUpdate schemaExport = new SchemaUpdate(cfg);
			schemaExport.execute(true, true);
		}
		
		PersistentClass mapping = cfg.getClassMapping(syncTableName);
		if(mapping == null){
			Guard.throwsException("INVALID_TABLE_NAME");
		}
		
		mapping = cfg.getClassMapping(tableName);
		if(contentMapping == null){  // TODO (JMT) create tables automatically if absent from RDF schema
			Guard.throwsException("INVALID_TABLE_NAME");
		}
		
		return mapping;
		
	}

	private static void autodiscoveryMappings(
			HibernateSessionFactoryBuilder builder, String tableName,
			String syncTableName, String baseDirectory) {
		JDBCMetaDataConfiguration cfg = new JDBCMetaDataConfiguration();
		builder.initializeConfiguration(cfg);		
		
		TableSelectorStrategy reverseEngineeringStrategy = new TableSelectorStrategy(new DefaultReverseEngineeringStrategy());
		reverseEngineeringStrategy.addSchemaSelection(new SchemaSelection(null, null, tableName));
		reverseEngineeringStrategy.addSchemaSelection(new SchemaSelection(null, null, syncTableName));
		
		cfg.setReverseEngineeringStrategy(reverseEngineeringStrategy);
		cfg.readFromJDBC();		
		cfg.buildMappings();
		
		HibernateMappingExporter exporter = new HibernateDOMMappingExporter(cfg, new File(baseDirectory));
		exporter.start();
	}

	@SuppressWarnings("unchecked")
	public static HibernateSessionFactoryBuilder createHibernateFactoryBuilder(String connectionURL, String user, String password, Class driverClass, Class dialectClass, File propertyFile) {
		
		HibernateSessionFactoryBuilder builder = new HibernateSessionFactoryBuilder();
		builder.setProperty("hibernate.dialect", dialectClass.getName());
		builder.setProperty("hibernate.connection.driver_class", driverClass.getName());
		builder.setProperty("hibernate.connection.url", connectionURL);
		builder.setProperty("hibernate.connection.username", user);
		builder.setProperty("hibernate.connection.password", password);
		builder.setProperty("hibernate.show_sql", "true");
		builder.setProperty("hibernate.format_sql", "true");

		if(propertyFile != null){
			builder.setPropertiesFile(propertyFile);
		}
		return builder;
	}

}