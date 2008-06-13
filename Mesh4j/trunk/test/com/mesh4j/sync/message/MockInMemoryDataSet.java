package com.mesh4j.sync.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mesh4j.sync.IFilter;
import com.mesh4j.sync.ISyncAdapter;
import com.mesh4j.sync.model.Item;

public class MockInMemoryDataSet implements IDataSet, ISyncAdapter{

	// MODEL VARIABLES
	private String dataSetId;
	private List<Item> items = new ArrayList<Item>();
	
	// METHODS
	public MockInMemoryDataSet(String dataSetId, List<Item> items) {
		this.dataSetId = dataSetId;
		this.items = items;
	}

	@Override
	public void add(Item item) {
		items.add(item);			
	}

	@Override
	public String getDataSetId() {
		return dataSetId;
	}

	@Override
	public List<Item> getItems() {
		return items;
	}

	@Override
	public void update(Item item) {
		Item itemToUpdate = get(item.getSyncId());
		if(itemToUpdate != null){
			this.items.remove(itemToUpdate);
			if(!item.isDeleted()){
				this.items.add(item);
			}
		}
	}

	@Override
	public Item get(String id) {
		for (Item localItem : this.items) {
			if(localItem.getSyncId().equals(id)){
				return localItem;
			}
		}
		return null;
	}

	@Override
	public void delete(String id) {
		Item item = get(id);
		if(item != null){
			this.items.remove(item);
		}
		
	}

	@Override
	public List<Item> getAll() {
		return items;
	}

	@Override
	public List<Item> getAll(IFilter<Item> filter) {
		return null;
	}

	@Override
	public List<Item> getAllSince(Date since) {
		return null;
	}

	@Override
	public List<Item> getAllSince(Date since, IFilter<Item> filter) {
		return null;
	}

	@Override
	public List<Item> getConflicts() {
		return null;
	}

	@Override
	public String getFriendlyName() {
		return null;
	}

	@Override
	public void update(Item item, boolean resolveConflicts) {
		
	}

}
