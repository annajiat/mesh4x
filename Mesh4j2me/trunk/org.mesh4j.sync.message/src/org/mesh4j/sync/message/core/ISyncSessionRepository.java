package org.mesh4j.sync.message.core;

import org.mesh4j.sync.message.IEndpoint;
import org.mesh4j.sync.message.IMessageSyncAdapter;
import org.mesh4j.sync.message.ISyncSession;

public interface ISyncSessionRepository {

	ISyncSession createSession(String sessionID, int version, String sourceId, IEndpoint endpoint, boolean fullProtocol);

	ISyncSession getSession(String sessionId);
	
	ISyncSession getSession(String sourceId, String endpointId);
	
	void registerSourceIfAbsent(IMessageSyncAdapter adapter);

	IMessageSyncAdapter getSource(String sourceId);

	void deleteAll();

	void save(ISyncSession syncSession);

}
