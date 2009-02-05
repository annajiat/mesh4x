package org.mesh4j.sync.message.channel.sms;

import java.util.ArrayList;
import java.util.List;

import org.mesh4j.sync.message.channel.sms.batch.SmsMessage;
import org.mesh4j.sync.message.channel.sms.batch.SmsMessageBatch;


public class MockSmsSender implements ISmsSender {

	private List<SmsMessageBatch> batches = new ArrayList<SmsMessageBatch>();
	private List<SmsMessage> messages = new ArrayList<SmsMessage>();
	private List<String> acks = new ArrayList<String>();
	
	@Override
	public SmsMessageBatch getOngoingBatch(String batchID) {
		for (SmsMessageBatch batch : batches) {
			if(batch.getId().equals(batchID)){
				return batch;
			}
		}
		return null;
	}

	@Override
	public List<SmsMessageBatch> getOngoingBatches() {
		return batches;
	}

	@Override
	public int getOngoingBatchesCount() {
		return batches.size();
	}

	@Override
	public void receiveACK(String batchId) {
		acks.add(batchId);
	}

	@Override
	public void send(SmsMessageBatch batch, boolean ackRequired) {
		this.batches.add(batch);
	}

	@Override
	public void send(List<SmsMessage> smsMessages, SmsEndpoint endpoint) {
		for (SmsMessage smsMessage : smsMessages) {
			send(smsMessage, endpoint);
		}
	}

	public void send(SmsMessage smsMessage, SmsEndpoint endpoint) {
		this.messages.add(smsMessage);
	}

	public List<SmsMessage> getMessages() {
		return messages;
	}
	
	public List<String> getACKs(){
		return acks;
	}

	@Override
	public void purgeBatches(String sessionId, int sessionVersion) {
		// nothing to do		
	}
	
	@Override
	public void startUp() {
		// nothing to do		
	}

	@Override
	public void shutdown() {
		// nothing to do		
	}

	@Override
	public List<SmsMessageBatch> getCompletedBatches(String sessionId,
			int version) {
		return null;
	}

	@Override
	public List<SmsMessageBatch> getOngoingBatches(String sessionId, int version) {
		return null;
	}

}
