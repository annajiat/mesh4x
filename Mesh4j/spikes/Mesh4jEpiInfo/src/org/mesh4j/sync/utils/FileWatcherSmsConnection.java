package org.mesh4j.sync.utils;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mesh4j.sync.id.generator.IdGenerator;
import org.mesh4j.sync.message.channel.sms.ISmsConnection;
import org.mesh4j.sync.message.channel.sms.ISmsReceiver;
import org.mesh4j.sync.message.channel.sms.SmsEndpoint;
import org.mesh4j.sync.message.channel.sms.connection.ISmsConnectionInboundOutboundNotification;
import org.mesh4j.sync.message.encoding.IMessageEncoding;

public class FileWatcherSmsConnection implements ISmsConnection {

	private final static Log LOGGER = LogFactory.getLog(FileWatcherSmsConnection.class);
	
	// MODEL VARIABLES
	private ISmsReceiver messageReceiver;
	private int maxMessageLenght = 140;
	private IMessageEncoding messageEncoding;
	private FileMessageRepository messageRepository;
	private ISmsConnectionInboundOutboundNotification[] smsAware = new ISmsConnectionInboundOutboundNotification[]{}; 
	private Timer timer;
	
	// BUSINESS METHODS
	
	public FileWatcherSmsConnection(String inDir, String outDir, IMessageEncoding encoding, int maxMessageLenght, ISmsConnectionInboundOutboundNotification[] smsAware) {
		super();
		this.messageEncoding = encoding;
		this.maxMessageLenght = maxMessageLenght;
		
		this.messageRepository = new FileMessageRepository(inDir, outDir);
	}

	@Override
	public int getMaxMessageLenght() {
		return this.maxMessageLenght;
	}

	@Override
	public IMessageEncoding getMessageEncoding() {
		return this.messageEncoding;
	}

	@Override
	public void send(List<String> messages, SmsEndpoint endpoint) {
		String smsNumber = endpoint.getEndpointId();
		for (String messageText : messages) {
			boolean ok = true;
			try{
				FileMessage msg = new FileMessage(IdGenerator.INSTANCE.newID(), smsNumber, messageText, new Date());
				this.messageRepository.addOutcommingMessage(msg);
			} catch (Throwable e) {
				ok = false;
				LOGGER.error(e.getMessage(), e);
			}

			try{
				if(ok){
					this.notifySendMessage(smsNumber, messageText);
				} else {
					this.notifySendMessageError(smsNumber, messageText);
				}
			} catch (Throwable e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

	}

	@Override
	public void setMessageReceiver(ISmsReceiver messageReceiver) {
		this.messageReceiver = messageReceiver;
	}

	@Override
	public void shutdown() {
		this.messageRepository.close();
		this.timer.cancel();
		this.timer.purge();
		this.timer = null;
	}

	@Override
	public void startUp() {
		this.messageRepository.open();
		
		int period = 30000;		
		TimerTask task = new TimerTask(){
			@Override
			public void run() {
				try{
					receiveMessages();
				} catch (RuntimeException e){
					LOGGER.error(e.getMessage(), e);
				}
			}			
		};		
		this.timer = new Timer();
		timer.schedule(task, 0, period);
	}

	protected void receiveMessages() {
		List<FileMessage> inMessages = this.messageRepository.getIncommingMessages();
		for (FileMessage fileMessage : inMessages) {
			boolean ok = true;
			
			try{
				this.messageReceiver.receiveSms(new SmsEndpoint(fileMessage.getNumber()), fileMessage.getText(), fileMessage.getDate());
				this.messageRepository.deleteIncommingMessage(fileMessage);
			} catch(Throwable e){
				ok = false;
				LOGGER.error(e.getMessage(), e);
			}
			
			try{
				if(ok){
					this.notifyReceiveMessage(fileMessage.getNumber(), fileMessage.getText(), fileMessage.getDate());
				} else {
					this.notifyReceiveMessageError(fileMessage.getNumber(), fileMessage.getText(), fileMessage.getDate());
				}
			} catch(Throwable e){
				LOGGER.error(e.getMessage(), e);
			}
		}		
	}

	// NOTIFICATIONS
	private void notifySendMessage(String endpointId, String message) {
		for (ISmsConnectionInboundOutboundNotification smsNot : this.smsAware) {
			smsNot.notifySendMessage(endpointId, message);
		}
	}

	private void notifySendMessageError(String endpointId, String message) {
		for (ISmsConnectionInboundOutboundNotification smsNot : this.smsAware) {
			smsNot.notifySendMessageError(endpointId, message);
		}
	}
	
	private void notifyReceiveMessage(String endpointId, String message, Date date) {
		for (ISmsConnectionInboundOutboundNotification smsNot : this.smsAware) {
			smsNot.notifyReceiveMessage(endpointId, message, date);
		}
	}

	private void notifyReceiveMessageError(String endpointId, String message, Date date) {
		for (ISmsConnectionInboundOutboundNotification smsNot : this.smsAware) {
			smsNot.notifyReceiveMessageError(endpointId, message, date);
		}
	}

}
