package org.mesh4j.sync.message.channel.sms.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.mesh4j.sync.message.IMessage;
import org.mesh4j.sync.message.IMessageReceiver;
import org.mesh4j.sync.message.InOutStatistics;
import org.mesh4j.sync.message.channel.sms.ISmsChannel;
import org.mesh4j.sync.message.channel.sms.ISmsConnection;
import org.mesh4j.sync.message.channel.sms.ISmsReceiverAndBatchManager;
import org.mesh4j.sync.message.channel.sms.ISmsSender;
import org.mesh4j.sync.message.channel.sms.SmsEndpoint;
import org.mesh4j.sync.message.channel.sms.batch.MessageBatchFactory;
import org.mesh4j.sync.message.channel.sms.batch.SmsMessage;
import org.mesh4j.sync.message.channel.sms.batch.SmsMessageBatch;
import org.mesh4j.sync.message.core.Message;
import org.mesh4j.sync.message.encoding.IMessageEncoding;
import org.mesh4j.sync.validations.Guard;


public class SmsChannel implements ISmsChannel {
	
	// MODEL VARIABLES
	private ISmsSender sender;
	private ISmsReceiverAndBatchManager receiver;
	private MessageBatchFactory batchFactory;
	private IMessageEncoding messageEncoding;	
	private IMessageReceiver messageReceiver;
	private ISmsConnection smsConnection;

	// METHODs
	public SmsChannel(ISmsConnection smsConnection, ISmsSender sender, ISmsReceiverAndBatchManager receiver, IMessageEncoding messageEncoding, int maxMessageLenght) {

		Guard.argumentNotNull(sender, "sender");
		Guard.argumentNotNull(receiver, "receiver");
		Guard.argumentNotNull(messageEncoding, "messageEncoding");
		Guard.argumentNotNull(smsConnection, "smsConnection");
		
		int max = maxMessageLenght - MessageFormatter.getBatchHeaderLenght();
		if(max < 0){
			Guard.throwsArgumentException("maxMessageLenght");	
		}
		
		this.smsConnection = smsConnection;
		this.messageEncoding = messageEncoding;
		this.batchFactory = new MessageBatchFactory(max);
		
		this.sender = sender;
		this.receiver = receiver;
		receiver.setBatchReceiver(this);
	}

	@Override
	public void receive(SmsMessageBatch batch){
		Guard.argumentNotNull(batch, "batch");
		
		IMessage message = createMessage(batch);
		if(this.isRetry(message)){
			this.resendSmsMessages(message);
		}else{
			this.messageReceiver.receiveMessage(message);
		}
	}
	
	@Override
	public void registerMessageReceiver(IMessageReceiver messageReceiver) {
		this.messageReceiver = messageReceiver;
	}

	@Override
	public void send(IMessage message) {
		Guard.argumentNotNull(message, "message");
		
		SmsMessageBatch batch = createBatch(message);		
		this.send(batch, message.isAckRequired());
	}

	public SmsMessageBatch createBatch(IMessage message) {
		String msg = MessageFormatter.createMessage(message.getMessageType(), message.getSessionVersion(), message.getData());

		String encodedData = this.messageEncoding.encode(msg);		
		String header = message.getProtocol();
		String ackBatchId = (message.getOrigin() == null || message.getOrigin().length() == 0) ? "00000" : message.getOrigin();
		SmsMessageBatch batch = this.batchFactory.createMessageBatch(message.getSessionId(), (SmsEndpoint)message.getEndpoint(), header, ackBatchId, encodedData);
		return batch;
	}
	
	private Message createMessage(SmsMessageBatch batch) {
		batch.reconstitutePayload();

		String encodedData = batch.getPayload();
		String decodedData = this.messageEncoding.decode(encodedData);
		
		Message message = new Message(
			batch.getProtocolHeader(),
			MessageFormatter.getMessageType(decodedData),
			batch.getSessionId(),
			MessageFormatter.getSessionVersion(decodedData),
			MessageFormatter.getData(decodedData),
			batch.getEndpoint()
		);
		message.setOrigin(batch.getId());
		return message;
	}

	@Override
	public void receiveACK(String batchId) {
		if(batchId != null && batchId.length() != 0){
			this.sender.receiveACK(batchId);
		}
	}

	@Override
	public void sendAskForRetry(SmsMessageBatch batch) {
		
		Guard.argumentNotNull(batch, "batch");
		
		StringBuffer sb = new StringBuffer();
		sb.append(batch.getId());
		sb.append("|");
		
		for (int i = 0; i < batch.getExpectedMessageCount(); i++) {
			SmsMessage msg = batch.getMessage(i);
			if(msg == null){
				sb.append(i);
				sb.append("|");
			}else{
				msg.setLastModificationDate(new Date());
			}
		}
		Message message = new Message("R", "R", batch.getSessionId(), 0, sb.toString(), batch.getEndpoint());
		message.setAckIsRequired(false);
		
		this.send(message);
	}

	public boolean isRetry(IMessage message){
		return message != null  && "R".equals(message.getProtocol()) && "R".equals(message.getMessageType());
	}

	private void resendSmsMessages(IMessage message) {
		StringTokenizer st = new StringTokenizer(message.getData(), "|");
		String batchID = st.nextToken();
		
		SmsMessageBatch batch = this.sender.getOngoingBatch(batchID);
		if(batch != null){
			ArrayList<SmsMessage> messagesToResend = new ArrayList<SmsMessage>();
			while(st.hasMoreTokens()){
				int seq = Integer.valueOf(st.nextToken());
				SmsMessage smsMessage = batch.getMessage(seq);
				messagesToResend.add(smsMessage);
			}
			this.sender.send(messagesToResend, batch.getEndpoint());
		}
	}

	@Override
	public List<SmsMessageBatch> getIncommingBatches() {
		return this.receiver.getOngoingBatches();
	}

	@Override
	public List<SmsMessageBatch> getOutcommingBatches() {
		return this.sender.getOngoingBatches();
	}

	@Override
	public void resend(SmsMessageBatch outcommingBatch) {
		this.sender.send(outcommingBatch, false);
	}

	@Override
	public void send(SmsMessageBatch batch, boolean ackIsRequired) {
		this.sender.send(batch, ackIsRequired);		
	}
	
	public void send(SmsMessage message, SmsEndpoint endpoint){
		this.sender.send(message, endpoint);
	}
	
	public void send(List<SmsMessage> messages, SmsEndpoint endpoint){
		this.sender.send(messages, endpoint);
	}
	
	@Override
	public void purgeMessages(String sessionId, int sessionVersion) {
		this.sender.purgeBatches(sessionId, sessionVersion);
		this.receiver.purgeBatches(sessionId, sessionVersion);
	}
	
	@Override
	public void startUp() {
		this.sender.startUp();
	}
	
	@Override
	public void shutdown() {
		this.sender.shutdown();
	}

	@Override
	public ISmsConnection getSmsConnection() {
		return this.smsConnection;
	}
	
	public IMessageReceiver getMessageReceiver(){
		return this.messageReceiver;
	}

	@Override
	public InOutStatistics getInOutStatistics(String sessionId, int version) {
		
		int out = 0;
		int outPendingAcks = 0;
		int in = 0;
		int inPendingToArrive = 0;
		
		List<SmsMessageBatch> outPendindBatches = this.sender.getOngoingBatches(sessionId, version);
		List<SmsMessageBatch> outBatches = this.sender.getCompletedBatches(sessionId, version);
		List<SmsMessageBatch> inPendingBatches = this.receiver.getOngoingBatches(sessionId, version);
		List<SmsMessageBatch> inBatches = this.receiver.getCompletedBatches(sessionId, version);
		
		for (SmsMessageBatch smsMessageBatch : outPendindBatches) {
			out = out + smsMessageBatch.getExpectedMessageCount();
			outPendingAcks = outPendingAcks + smsMessageBatch.getExpectedMessageCount();
		}

		
		for (SmsMessageBatch smsMessageBatch : outBatches) {
			out = out + smsMessageBatch.getExpectedMessageCount();
		}
		
		
		for (SmsMessageBatch smsMessageBatch : inPendingBatches) {
			if(smsMessageBatch.isComplete()){
				in = in + smsMessageBatch.getExpectedMessageCount();
			} else {
				in = in + smsMessageBatch.getMessagesCount();
				inPendingToArrive = inPendingToArrive + (smsMessageBatch.getExpectedMessageCount() - smsMessageBatch.getMessagesCount());
			}
		}
		
		for (SmsMessageBatch smsMessageBatch : inBatches) {
			if(smsMessageBatch.isComplete()){
				in = in + smsMessageBatch.getExpectedMessageCount();
			} else {
				in = in + smsMessageBatch.getMessagesCount();
				inPendingToArrive = inPendingToArrive + (smsMessageBatch.getExpectedMessageCount() - smsMessageBatch.getMessagesCount());
			}
		}
		
		InOutStatistics sta = new InOutStatistics(in, inPendingToArrive, out, outPendingAcks);
		return sta;
	}
}
