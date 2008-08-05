package org.mesh4j.sync.message;

import java.util.List;

import org.mesh4j.sync.model.Item;


public interface IMessageSyncAdapter{

	String getSourceId();

	List<Item> getAll();
	
	List<Item> synchronizeSnapshot(ISyncSession syncSession);
}
