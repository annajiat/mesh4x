package com.mesh4j.sync.message;

import java.util.ArrayList;
import java.util.Date;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Assert;
import org.junit.Test;

import com.mesh4j.sync.adapters.feed.XMLContent;
import com.mesh4j.sync.message.channel.sms.ISmsConnection;
import com.mesh4j.sync.message.channel.sms.SmsChannel;
import com.mesh4j.sync.message.dataset.DataSetManager;
import com.mesh4j.sync.message.dataset.InMemoryDataSet;
import com.mesh4j.sync.message.encoding.ZipBase64Encoding;
import com.mesh4j.sync.message.protocol.MessageSyncProtocolFactory;
import com.mesh4j.sync.model.IContent;
import com.mesh4j.sync.model.Item;
import com.mesh4j.sync.model.Sync;
import com.mesh4j.sync.utils.IdGenerator;

public class MessageSyncEngineTests {

	@Test
	public void shouldSync(){
		
		ArrayList<Item> itemsA = new ArrayList<Item>();
		itemsA.add(createNewItem());
		itemsA.add(createNewItem());
		itemsA.add(createNewItem());
		
		ArrayList<Item> itemsB = new ArrayList<Item>();
		itemsB.add(createNewItem());
		itemsB.add(createNewItem());
		itemsB.add(createNewItem());		
		
		String dataSetId = "12345";

		MockSmsConnection smsConnectionEndpointA = new MockSmsConnection();
		MockSmsConnection smsConnectionEndpointB = new MockSmsConnection();

		smsConnectionEndpointA.setEndPoint(smsConnectionEndpointB);
		smsConnectionEndpointB.setEndPoint(smsConnectionEndpointA);
		
		IChannel channelEndpointA = new SmsChannel(smsConnectionEndpointA, ZipBase64Encoding.INSTANCE);
		IChannel channelEndpointB = new SmsChannel(smsConnectionEndpointB, ZipBase64Encoding.INSTANCE);
		
		DataSetManager dataSetManagerEndPointA = new DataSetManager();
		IDataSet dataSetEndPointA = new InMemoryDataSet(dataSetId, itemsA);
		dataSetManagerEndPointA.addDataSet(dataSetEndPointA);		
		
		IMessageSyncProtocol syncProtocolEndPointA = MessageSyncProtocolFactory.createSyncProtocol(dataSetManagerEndPointA);		
		MessageSyncEngine syncEngineEndPointA = new MessageSyncEngine(dataSetManagerEndPointA, syncProtocolEndPointA, channelEndpointA);

		DataSetManager dataSetManagerEndPointB = new DataSetManager();
		IDataSet dataSetEndPointB = new InMemoryDataSet(dataSetId, itemsB);
		dataSetManagerEndPointB.addDataSet(dataSetEndPointB);
		
		IMessageSyncProtocol syncProtocolEndPointB = MessageSyncProtocolFactory.createSyncProtocol(dataSetManagerEndPointB);		
		MessageSyncEngine syncEngineEndPointB = new MessageSyncEngine(dataSetManagerEndPointB, syncProtocolEndPointB, channelEndpointB);
		Assert.assertNotNull(syncEngineEndPointB);
		
		Assert.assertEquals(3, dataSetEndPointA.getItems().size());
		Assert.assertEquals(3, dataSetEndPointB.getItems().size());
		
		syncEngineEndPointA.synchronize(dataSetId);
	
		Assert.assertEquals(6, dataSetEndPointA.getItems().size());
		Assert.assertEquals(3, dataSetEndPointB.getItems().size());
	}
	
	
	@SuppressWarnings("unused")
	private class MockSmsConnection implements ISmsConnection{

		private MockSmsConnection endpoint;
		private IMessageReceiver messageReceiver;
		
		public MockSmsConnection() {
		}
		
		public void setEndPoint(MockSmsConnection endpoint){
			this.endpoint = endpoint;
		}
		
		@Override
		public void registerMessageReceiver(IMessageReceiver messageReceiver) {
			this.messageReceiver = messageReceiver;			
		}

		@Override
		public void send(String message) {
			this.endpoint.receive(message);
		}
		
		public void receive(String message){
			this.messageReceiver.receiveMessage(message);
		}

		@Override
		public int getMaxMessageLenght() {
			return 30;
		}
	}
	
	private Item createNewItem() {
		String syncId = IdGenerator.newID();
		
		Element payload = DocumentHelper.createElement("foo");
		payload.addElement("bar");
		
		IContent content = new XMLContent(syncId, "title", "desc", payload);
		
		Sync sync = new Sync(syncId, "jmt", new Date(), false);
		sync.update("jjj", new Date());
		
		return new Item(content, sync);
	}
}
