package org.mesh4j.sync.message.channel.sms;

import java.util.List;

import org.mesh4j.sync.message.IChannel;
import org.mesh4j.sync.message.channel.sms.batch.SmsMessageBatch;


public interface ISmsChannel extends IChannel, ISmsBatchReceiver{

	List<SmsMessageBatch> getIncommingBatches();

	void sendAskForRetry(SmsMessageBatch incommingBatch);

	void send(SmsMessageBatch batch, boolean ackIsRequired);

	List<SmsMessageBatch> getOutcommingBatches();

	void resend(SmsMessageBatch outcommingBatch);

}
