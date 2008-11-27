package org.mesh4j.sync.ui.translator;

import java.util.Date;
import java.util.List;

import org.mesh4j.sync.adapters.msaccess.MsAccessSyncAdapterFactory;
import org.mesh4j.sync.message.IEndpoint;
import org.mesh4j.sync.message.IMessage;
import org.mesh4j.sync.message.protocol.ACKEndSyncMessageProcessor;
import org.mesh4j.sync.message.protocol.ACKMergeMessageProcessor;
import org.mesh4j.sync.message.protocol.BeginSyncMessageProcessor;
import org.mesh4j.sync.message.protocol.CancelSyncMessageProcessor;
import org.mesh4j.sync.message.protocol.EndSyncMessageProcessor;
import org.mesh4j.sync.message.protocol.GetForMergeMessageProcessor;
import org.mesh4j.sync.message.protocol.ItemEncoding;
import org.mesh4j.sync.message.protocol.LastVersionStatusMessageProcessor;
import org.mesh4j.sync.message.protocol.MergeMessageProcessor;
import org.mesh4j.sync.message.protocol.MergeWithACKMessageProcessor;
import org.mesh4j.sync.message.protocol.NoChangesMessageProcessor;
import org.mesh4j.sync.translator.MessageTranslator;


public class EpiInfoUITranslator {

	public static String getTitle() {
		return MessageTranslator.translate("EPIINFO_TITLE");
	}

	public static String getGroupCommunications() {
		return MessageTranslator.translate("EPIINFO_GROUP_COMMUNICATIONS");
	}

	public static String getToolTipPhoneNumber() {
		return MessageTranslator.translate("EPIINFO_TOOLTIP_PHONE_NUMBER");
	}

	public static String getLabelSMSDevice() {
		return MessageTranslator.translate("EPIINFO_LABEL_SMS_DEVICE");
	}

	public static String getLabelPhoneNumber() {
		return MessageTranslator.translate("EPIINFO_LABEL_PHONE_NUMBER");
	}

	public static String getLabelDataSource() {
		return MessageTranslator.translate("EPIINFO_LABEL_DATA_SOURCE");
	}

	public static String getLabelTable() {
		return MessageTranslator.translate("EPIINFO_LABEL_TABLE");
	}

	public static String getToolTipDataSource() {
		return MessageTranslator.translate("EPIINFO_TOOLTIP_DATA_SOURCE");
	}

	public static String getToolTipFileChooser() {
		return MessageTranslator.translate("EPIINFO_TOOLTIP_FILE_CHOOSER");
	}

	public static String getLabelFileChooser() {
		return MessageTranslator.translate("EPIINFO_LABEL_FILE_CHOOSER");
	}

	public static String getSynchronize() {
		return MessageTranslator.translate("EPIINFO_LABEL_SYNCHRONIZE");
	}

	public static String getCancel() {
		return MessageTranslator.translate("EPIINFO_LABEL_CANCEL");
	}

	public static String getMessageNotifySendMessage(String endpointId, String message) {
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_NOTIFY_SEND_MSG", endpointId, message);
	}

	public static String getMessageNotifyReceiveMessage(String endpointId, String message) {
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_NOTIFY_RECEIVE_MSG", endpointId, message);
	}

	public static String getMessageNotifyReceiveMessageError(String endpointId, String message) {
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_NOTIFY_RECEIVE_MSG", endpointId, message);
	}

	public static String getMessageNotifySendMessageError(String endpointId, String message) {
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_NOTIFY_RECEIVE_MSG", endpointId, message);
	}

	public static String getLabelMessageToSend() {
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_LABEL_MESSAGE_TO_SEND");
	}

	public static String getLabelSendMessage() {
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_LABEL_SEND");

	}

	public static String getMessageErrorBeginSync(String endpointId, String sourceId) {
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_ERROR_BEGIN_SYNC", endpointId, getSourceId(sourceId));
	}

	public static String getMessageCancelSync(String sessionId, String endpointId, String sourceId) {
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_CANCEL_SYNC", sessionId, endpointId, getSourceId(sourceId));
	}

	public static String getMessageCancelSyncErrorSessionNotOpen(IEndpoint endpoint, String sourceId) {
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_ERROR_CANCEL_SYNC_SESSION_NOT_OPEN", endpoint.getEndpointId(), getSourceId(sourceId));
	}
	
	public static String getMessageInvalidMessageProtocol(IMessage message) {
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_ERROR_INVALID_PROTOCOL", message.getProtocol(), translateMessageType(message) + " session: " + message.getSessionId() + " endpoint: " + message.getEndpoint().getEndpointId());
	}
	
	public static String getMessageErrorInvalidProtocolMessageOrder(IMessage message) {
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_ERROR_INVALID_PROTOCOL_MESSAGE_ORDER", translateMessageType(message), message.getSessionId(), message.getEndpoint().getEndpointId());	
	}

	public static String getMessageErrorSessionCreation(IMessage message, String sourceId) {
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_ERROR_SESSION_CREATION", translateMessageType(message), message.getSessionId(), message.getEndpoint().getEndpointId(), getSourceId(sourceId));
	}
	
	private static String translateMessageType(IMessage message){
		String messageType = message.getMessageType();
		if(BeginSyncMessageProcessor.MESSAGE_TYPE.equals(messageType)){
			return "Begin sync";
		}
		if(NoChangesMessageProcessor.MESSAGE_TYPE.equals(messageType)){
			return "No Changes";
		}
		if(LastVersionStatusMessageProcessor.MESSAGE_TYPE.equals(messageType)){
			return "Last Items Versions";
		}
		if(GetForMergeMessageProcessor.MESSAGE_TYPE.equals(messageType)){
			String syncId = GetForMergeMessageProcessor.getSyncID(message.getData());
			return "Get For Merge: " + syncId;
		}
		if(MergeMessageProcessor.MESSAGE_TYPE.equals(messageType)){
			String syncId = ItemEncoding.getSyncID(message.getData());
			return "Merge: " + syncId;
		}
		if(MergeWithACKMessageProcessor.MESSAGE_TYPE.equals(messageType)){
			String itemData = message.getData().substring(1, message.getData().length());
			String syncId = ItemEncoding.getSyncID(itemData);
			return "Merge with ACK: " + syncId;
		}
		if(EndSyncMessageProcessor.MESSAGE_TYPE.equals(messageType)){
			return "End sync";
		}
		if(ACKEndSyncMessageProcessor.MESSAGE_TYPE.equals(messageType)){
			return "ACK of End Sync";
		}
		if(ACKMergeMessageProcessor.MESSAGE_TYPE.equals(messageType)){
			String itemData = message.getData().substring(1, message.getData().length());
			String syncId = ItemEncoding.getSyncID(itemData);
			return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_TYPE_ACK_MERGE", syncId);
		}
		if(CancelSyncMessageProcessor.MESSAGE_TYPE.equals(messageType)){
			return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_TYPE_CANCEL", message.getSessionId());
		}
		return messageType;
	}

	public static String getMessageProcessed(IMessage message, List<IMessage> response) {
		StringBuffer sb = new StringBuffer();
		if(response.size() > 0){
			IMessage responseMessage;
			
			if(response.size() == 1){
				responseMessage = response.get(0);
				sb.append("\n   ");
				sb.append(MessageTranslator.translate("EPIINFO_SMS_MESSAGE_RESPONSE", 1, translateMessageType(responseMessage)));
			} else {
				for (int i = 0; i < response.size(); i++) {
					responseMessage = response.get(i);
					sb.append("\n   ");
					sb.append(MessageTranslator.translate("EPIINFO_SMS_MESSAGE_RESPONSE", i, translateMessageType(responseMessage)));
				}
			}
		}
		return MessageTranslator.translate("EPIINFO_SMS_MESSAGE_PROCESSED", 
				translateMessageType(message), 
				message.getOrigin(),
				message.getSessionId(),
				message.getEndpoint().getEndpointId()) + sb.toString();
	}

	public static String getLabelCancelSyn() {
		return MessageTranslator.translate("EPIINFO_SMS_LABEL_CANCEL_SYNCHRONIZE");
	}

	public static String getLabelStart() {
		return MessageTranslator.translate("EPIINFO_SYNC_LABEL_START");
	}

	public static String getLabelFailed() {
		return MessageTranslator.translate("EPIINFO_SYNC_LABEL_FAILED");
	}

	public static String getLabelSuccess() {
		return MessageTranslator.translate("EPIINFO_SYNC_LABEL_SUCCESS");
	}

	public static String getLabelSyncEndWithConflicts(int conflicts) {
		return MessageTranslator.translate("EPIINFO_SYNC_COMPLETED_WITH_CONFLICTS", conflicts);
	}

	public static String getLabelCleanConsole() {
		return MessageTranslator.translate("EPIINFO_SMS_LABEL_CLEAN_CONSOLE");
	}

	public static String getStatusBeginSync(String target, String dataSource, String tableName, Date date) {
		return MessageTranslator.translate("EPIINFO_STATUS_BEGIN_SYNC", target, dataSource, tableName, date);
	}

	public static String getStatusEndSync(String target, String dataSource, String tableName, Date date) {
		return MessageTranslator.translate("EPIINFO_STATUS_END_SYNC", target, dataSource, tableName, date);
	}

	public static String getStatusEndSyncWithConflicts(String target, String dataSource, String tableName, Date date, int conflicts) {
		return MessageTranslator.translate("EPIINFO_STATUS_END_SYNC_WITH_CONFLICTS", target, dataSource, tableName, date, conflicts);
	}
	
	public static String getStatusCancelSync(IEndpoint target, String dataSource, String tableName, Date date) {
		return MessageTranslator.translate("EPIINFO_STATUS_CANCEL_SYNC", target.getEndpointId(), dataSource, tableName, date);
	}

	public static String getSourceId(String sourceId) {
		if(MsAccessSyncAdapterFactory.isMsAccess(sourceId)){
			return MsAccessSyncAdapterFactory.getFileName(sourceId) + "@" + MsAccessSyncAdapterFactory.getTableName(sourceId);
		} else {
			return sourceId;
		}
	}

	public static String getLabelDataSourceFileExtensions() {
		return MessageTranslator.translate("EPIINFO_LABEL_DATA_SOURCE_FILE_EXTENSIONS");
	}

	public static String getLabelDemo() {
		return MessageTranslator.translate("EPIINFO_LABEL_DEMO");
	}

	public static String getLabelModemDiscovery() {
		return MessageTranslator.translate("EPIINFO_LABEL_MODEM_DISCOVERY");
	}

	public static String getLabelURL() {
		return MessageTranslator.translate("EPIINFO_LABEL_URL");
	}

	public static String getLabelChannelSMS() {
		return MessageTranslator.translate("EPIINFO_LABEL_CHANNEL_SMS");
	}

	public static String getLabelChannelWEB() {
		return MessageTranslator.translate("EPIINFO_LABEL_CHANNEL_WEB");
	}

	public static String getLabelAddDataSource() {
		return MessageTranslator.translate("EPIINFO_LABEL_ADD_DATA_SOURCE");
	}

	public static String getLabelDeviceConnectionFailed(String device) {
		return MessageTranslator.translate("EPIINFO_LABEL_DEVICE_CONNECTION_FAILED", device);
	}

	public static String getLabelSaveDefaults() {
		return MessageTranslator.translate("EPIINFO_LABEL_SAVE_DEFAULTS");
	}
}
