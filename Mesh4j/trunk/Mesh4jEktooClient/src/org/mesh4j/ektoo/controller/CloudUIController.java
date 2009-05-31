package org.mesh4j.ektoo.controller;

import org.mesh4j.ektoo.ISyncAdapterBuilder;
import org.mesh4j.ektoo.SyncAdapterBuilder;
import org.mesh4j.ektoo.model.CloudModel;
import org.mesh4j.ektoo.properties.PropertiesProvider;
import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.payload.schema.rdf.IRDFSchema;
import org.mesh4j.sync.validations.Guard;

/**
 * @author Bhuiyan Mohammad Iklash
 * 
 */
public class CloudUIController extends AbstractUIController
{
	
	public static final String MESH_NAME_PROPERTY = "MeshName";
	public static final String DATASET_NAME_PROPERTY = "DatasetName";

	// MODEL VARIABLES
	private ISyncAdapterBuilder adapterBuilder;

	// BUSINESS METHODS
	public CloudUIController(PropertiesProvider propertiesProvider) {
		Guard.argumentNotNull(propertiesProvider, "propertiesProvider");
		this.adapterBuilder = new SyncAdapterBuilder(propertiesProvider);
	}

	public void changeMeshName(String meshName) {
		setModelProperty(MESH_NAME_PROPERTY, meshName);
	}

	public void changeDatasetName(String datasetName) {
		setModelProperty(DATASET_NAME_PROPERTY, datasetName);
	}

	@Override
	public ISyncAdapter createAdapter() {
		CloudModel model = (CloudModel) this.getModel();
		if (model == null){
			return null;
		}

		String meshName = model.getMeshName();
		if (meshName == null || meshName.trim().length() == 0){
			return null;
		}

		String datasetName = model.getDatasetName();
		if (datasetName == null || datasetName.trim().length() == 0){
			return null;
		}

		return adapterBuilder.createHttpSyncAdapter(meshName, datasetName);
	}

	@Override
	public IRDFSchema fetchSchema(ISyncAdapter adapter) {
		// TODO create schema
		return null;
	}

	@Override
	public ISyncAdapter createAdapter(IRDFSchema schema) {
		// TODO create adapter
		return null;
	}


}
