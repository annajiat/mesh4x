package org.mesh4j.sync.message.core;

import org.mesh4j.sync.message.IMessage;
import org.mesh4j.sync.message.ISyncSession;

public interface IBeginSyncMessageProcessor extends IMessageProcessor {

	IMessage createMessage(ISyncSession syncSession);

	String getSourceId(String data);

	boolean getFullProtocol(String data);

	boolean getSendChanges(String data);

	boolean getReceiveChanges(String data);

}
