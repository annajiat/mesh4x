package org.mesh4j.ektoo.controller;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mesh4j.ektoo.ISyncAdapterBuilder;
import org.mesh4j.ektoo.IUIController;
import org.mesh4j.ektoo.SyncAdapterBuilder;
import org.mesh4j.ektoo.model.MsAccessModel;
import org.mesh4j.ektoo.properties.PropertiesProvider;
import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.payload.schema.rdf.IRDFSchema;
import org.mesh4j.sync.validations.Guard;

/**
 * @author Bhuiyan Mohammad Iklash
 * 
 */
public class MsAccessUIController extends AbstractController implements
		IUIController {
	
	private static final Log LOGGER = LogFactory.getLog(MsAccessUIController.class);
	private static final String DATABASE_NAME_PROPERTY = "DatabaseName";
	private static final String TABLE_NAME_PROPERTY = "TableName";

	// MODEL VARIABLEs
	private ISyncAdapterBuilder adapterBuilder;

	// BUSINESS METHODS
	public MsAccessUIController(PropertiesProvider propertiesProvider) {
		Guard.argumentNotNull(propertiesProvider, "propertiesProvider");
		this.adapterBuilder = new SyncAdapterBuilder(propertiesProvider);
	}

	public void changeDatabaseName(String databaseName) {
		LOGGER.debug("MsAccessUIController>changeDatabaseName");
		setModelProperty(DATABASE_NAME_PROPERTY, databaseName);
	}

	public void changeTableName(String tableName) {
		LOGGER.debug("MsAccessUIController>changeTableName");
		setModelProperty(TABLE_NAME_PROPERTY, tableName);
	}

	@Override
	public ISyncAdapter createAdapter() {
		MsAccessModel model = (MsAccessModel) this.getModel();
		if (model == null){
			return null;
		}

		String databaseName = model.getDatabaseName();
		if (databaseName == null || databaseName.trim().length() == 0){
			return null;
		}

		String tableName = model.getTabletName();
		if (tableName == null || tableName.trim().length() == 0){
			return null;
		}

		return adapterBuilder.createMsAccessAdapter(databaseName, tableName);
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO property Change
	}

	@Override
	public IRDFSchema createSchema() {
		// TODO create Schema
		return null;
	}

	@Override
	public ISyncAdapter createAdapter(IRDFSchema schema) {
		// TODO create Adapter
		return null;
	}
}
