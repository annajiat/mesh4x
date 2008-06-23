package com.mesh4j.sync.message.protocol;

import java.util.ArrayList;
import java.util.List;

import com.mesh4j.sync.message.IMessage;
import com.mesh4j.sync.message.IMessageSyncProtocol;
import com.mesh4j.sync.message.ISyncSession;
import com.mesh4j.sync.message.MessageSyncEngine;
import com.mesh4j.sync.message.core.IMessageProcessor;
import com.mesh4j.sync.message.core.Message;
import com.mesh4j.sync.model.Item;
import com.mesh4j.sync.utils.XMLHelper;

public class MergeWithACKMessageProcessor implements IMessageProcessor {

	// MODEL VARIABLES
	private ACKMergeMessageProcessor ackMessage;
	private IItemEncoding itemEncoding;
	
	// METHODS
	public MergeWithACKMessageProcessor(IItemEncoding itemEncoding, ACKMergeMessageProcessor ackMessage) {
		super();
		this.ackMessage = ackMessage;
		this.itemEncoding = itemEncoding;
	}

	@Override
	public String getMessageType() {
		return "6";
	}
	
	public IMessage createMessage(ISyncSession syncSession, Item item) {
		syncSession.waitForAck(item.getSyncId());
		
		int[] diffHashCodes = this.getLastSyncDiffsHashCodes(syncSession, item);
		String data = this.itemEncoding.encode(syncSession, item, diffHashCodes);
		
		return new Message(
				IProtocolConstants.PROTOCOL,
				getMessageType(),
				syncSession.getSessionId(),
				data,
				syncSession.getTarget());
	}
	
	private int[] getLastSyncDiffsHashCodes(ISyncSession syncSession, Item actualItem) {
		if(!actualItem.isDeleted()){
			List<Item> items = syncSession.getSnapshot();
			for (Item item : items) {
				if(item.getSyncId().equals(actualItem.getSyncId())){
					String xml = XMLHelper.canonicalizeXML(item.getContent().getPayload());
					return this.itemEncoding.calculateDiffBlockHashCodes(xml);  
				}
			}
		}
		return new int[0];
	}

	@Override
	public List<IMessage> process(ISyncSession syncSession, IMessage message) {
		if(syncSession.isOpen() && this.getMessageType().equals(message.getMessageType())){
			Item incomingItem = this.itemEncoding.decode(syncSession, message.getData());
			MessageSyncEngine.merge(syncSession, incomingItem);
			
			ArrayList<IMessage> response = new ArrayList<IMessage>();
			response.add(this.ackMessage.createMessage(
					syncSession, 
					incomingItem.getSyncId(),
					syncSession.hasConflict(incomingItem.getSyncId())));
			return response;
		}
		return IMessageSyncProtocol.NO_RESPONSE;
	}
}
