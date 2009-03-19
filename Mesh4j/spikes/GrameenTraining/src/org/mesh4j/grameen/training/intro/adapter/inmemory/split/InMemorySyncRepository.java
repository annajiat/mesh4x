package org.mesh4j.grameen.training.intro.adapter.inmemory.split;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.mesh4j.sync.ISyncAware;
import org.mesh4j.sync.adapters.SyncInfo;
import org.mesh4j.sync.adapters.split.ISyncRepository;
import org.mesh4j.sync.id.generator.IdGenerator;
import org.mesh4j.sync.model.IContent;

public class InMemorySyncRepository implements ISyncRepository , ISyncAware{

	private Storage storage = new Storage();
	private String repositoryType = "";
	
	
	
	public InMemorySyncRepository(Storage storage,String repositoryType){
		this.storage = storage;
		this.repositoryType = repositoryType;
	}
	public InMemorySyncRepository(){
	}
	
	@Override
	public SyncInfo get(String id) {
		return (SyncInfo)this.storage.getRow(id);
	}

	@Override
	public List<SyncInfo> getAll(String entityName) {
		List<SyncInfo> allContents = new LinkedList<SyncInfo>();
		Collection<Object> list = this.storage.getStorage().values();
		for(Object cont : list){
			allContents.add((SyncInfo)cont);
		}
		return allContents;
	}

	@Override
	public String newSyncID(IContent content) {
		return IdGenerator.INSTANCE.newID();
	}

	@Override
	public void save(SyncInfo syncInfo) {
		//we will decide if it is new row or old one.
		if(this.storage.getRow(syncInfo.getId()) == null){
			storage.addRow(syncInfo);
		}else{
			storage.updateRow(syncInfo);
		}
		
	}

	public String getType(){
		return this.repositoryType;
	}
	
	@Override
	public void beginSync() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endSync() {
		// TODO Auto-generated method stub
		
	}

}
