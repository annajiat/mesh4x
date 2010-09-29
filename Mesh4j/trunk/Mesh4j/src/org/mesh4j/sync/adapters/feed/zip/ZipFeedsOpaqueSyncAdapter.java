package org.mesh4j.sync.adapters.feed.zip;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dom4j.Element;
import org.mesh4j.sync.IFilter;
import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.adapters.composite.IIdentifiableSyncAdapter;
import org.mesh4j.sync.model.Item;
import org.mesh4j.sync.payload.schema.ISchema;
import org.mesh4j.sync.payload.schema.rdf.RDFSchema;
import org.mesh4j.sync.validations.Guard;

public class ZipFeedsOpaqueSyncAdapter implements ISyncAdapter {

	// MODEL VARIABLES
	private ZipFeedsSyncAdapter zipFeedsSyncAdapter;
	
	// BUSINESS METHODS
	public ZipFeedsOpaqueSyncAdapter(ZipFeedsSyncAdapter zipFeedsSyncAdapter) {
		Guard.argumentNotNull(zipFeedsSyncAdapter, "zipFeedsSyncAdapter");
		this.zipFeedsSyncAdapter = zipFeedsSyncAdapter; 
	}
	
	@Override
	public void add(Item item) {
		String datasetName = getDataSetName(item);
		IIdentifiableSyncAdapter syncAdapter = this.zipFeedsSyncAdapter.addNewSyncAdapter(datasetName);
		if(syncAdapter != null){
			zipFeedsSyncAdapter.add(item);
		}
	}

	@Override
	public void update(Item item) {
		String datasetName = getDataSetName(item);
		IIdentifiableSyncAdapter syncAdapter = this.zipFeedsSyncAdapter.addNewSyncAdapter(datasetName);
		if(syncAdapter != null){
			syncAdapter.add(item);
			//zipFeedsSyncAdapter.update(item);
		}
	}

	@Override
	public void update(Item item, boolean resolveConflicts) {
		String datasetName = getDataSetName(item);
		IIdentifiableSyncAdapter syncAdapter = this.zipFeedsSyncAdapter.addNewSyncAdapter(datasetName);
		if(syncAdapter != null){
			syncAdapter.add(item);			
			//zipFeedsSyncAdapter.update(item, resolveConflicts);
		}
	}

	public String getDataSetName(Item item) {
		String datasetName = item.getContent().getPayload().getName();
		if(RDFSchema.isRDF(item.getContent().getPayload()) || datasetName.trim().toLowerCase().startsWith("payload")){
			datasetName = ((Element)item.getContent().getPayload().elements().get(0)).getName();
		}
		return datasetName;
	}
	
	@Override
	public void delete(String id) {
		// nothing to do
	}

	@Override
	public Item get(String id) {
		return null;
	}

	@Override
	public List<Item> getAll() {
		return new ArrayList<Item>();
	}

	@Override
	public List<Item> getAll(IFilter<Item> filter) {
		return new ArrayList<Item>();
	}

	@Override
	public List<Item> getAllSince(Date since) {
		return new ArrayList<Item>();
	}

	@Override
	public List<Item> getAllSince(Date since, IFilter<Item> filter) {
		return new ArrayList<Item>();
	}

	@Override
	public List<Item> getConflicts() {
		return new ArrayList<Item>();
	}

	@Override
	public String getFriendlyName() {
		return "zip opaque adapter";
	}

	public ZipFeedsSyncAdapter getZipAdapter() {
		return this.zipFeedsSyncAdapter;
	}

	@Override
	public ISchema getSchema() {
		return zipFeedsSyncAdapter.getSchema();
	}
}
