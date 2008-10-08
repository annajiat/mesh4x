package org.mesh4j.sync.adapters.split;

import java.util.List;

import org.mesh4j.sync.adapters.SyncInfo;
import org.mesh4j.sync.model.IContent;


public interface ISyncRepository {

	void save(SyncInfo syncInfo);

	SyncInfo get(String syncId);

	List<SyncInfo> getAll(String entityName);

	String newSyncID(IContent content);
}