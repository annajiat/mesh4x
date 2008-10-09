package org.mesh4j.sync.message.protocol;

import java.util.Date;
import java.util.List;

import org.mesh4j.sync.message.IMessage;
import org.mesh4j.sync.message.IMessageSyncProtocol;
import org.mesh4j.sync.message.ISyncSession;
import org.mesh4j.sync.message.core.IMessageProcessor;
import org.mesh4j.sync.message.core.Message;
import org.mesh4j.sync.utils.DateHelper;
import org.mesh4j.sync.validations.Guard;


public class ACKEndSyncMessageProcessor implements IMessageProcessor {

	public static final String MESSAGE_TYPE = "9";
	
	// MODEL VARIABLES
	private IMessageSyncProtocol messageSyncProtocol;
	
	// BUSINESS METHODS
	public ACKEndSyncMessageProcessor() {
		super();
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}
	
	public IMessage createMessage(ISyncSession syncSession, Date sinceDate){
		Guard.argumentNotNull(syncSession, "syncSession");
		Guard.argumentNotNull(sinceDate, "sinceDate");
		
		Message message = new Message(
				IProtocolConstants.PROTOCOL,
				getMessageType(),
				syncSession.getSessionId(),
				syncSession.getVersion(),
				DateHelper.formatDateTime(sinceDate),
				syncSession.getTarget());
		message.setAckIsRequired(false);
		return message;
	}

	@Override
	public List<IMessage> process(ISyncSession syncSession, IMessage message) {
		
		if(syncSession.isOpen() && syncSession.getVersion() == message.getSessionVersion() && this.getMessageType().equals(message.getMessageType())){
			Date sinceDate = DateHelper.parseDateTime(message.getData());
			if(sinceDate != null){
				this.messageSyncProtocol.endSync(syncSession, sinceDate);
			}
		}
		return IMessageSyncProtocol.NO_RESPONSE;
	}

	public void setMessageSyncProtocol(IMessageSyncProtocol messageSyncProtocol) {
		this.messageSyncProtocol = messageSyncProtocol;
	}

}
