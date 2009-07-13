package org.mesh4j.sync.message;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.junit.Assert;
import org.junit.Test;
import org.mesh4j.sync.SyncEngine;
import org.mesh4j.sync.adapters.dom.DOMAdapter;
import org.mesh4j.sync.adapters.kml.KMLContent;
import org.mesh4j.sync.adapters.kml.KMLDOMLoaderFactory;
import org.mesh4j.sync.adapters.kml.KmlNames;
import org.mesh4j.sync.message.channel.sms.SmsChannelFactory;
import org.mesh4j.sync.message.channel.sms.SmsEndpoint;
import org.mesh4j.sync.message.channel.sms.core.SmsEndpointFactory;
import org.mesh4j.sync.message.core.ISyncSessionRepository;
import org.mesh4j.sync.message.core.MockSyncSessionRepository;
import org.mesh4j.sync.message.core.repository.ISourceIdMapper;
import org.mesh4j.sync.message.core.repository.MessageSyncAdapterFactory;
import org.mesh4j.sync.message.core.repository.SyncSessionFactory;
import org.mesh4j.sync.message.core.repository.file.FileSyncSessionRepository;
import org.mesh4j.sync.message.encoding.CompressBase91MessageEncoding;
import org.mesh4j.sync.message.encoding.IMessageEncoding;
import org.mesh4j.sync.message.protocol.IItemEncoding;
import org.mesh4j.sync.message.protocol.ItemEncodingFixedBlock;
import org.mesh4j.sync.message.protocol.MessageSyncProtocolFactory;
import org.mesh4j.sync.model.Item;
import org.mesh4j.sync.security.NullIdentityProvider;
import org.mesh4j.sync.test.utils.TestHelper;
import org.mesh4j.sync.utils.XMLHelper;


public class KmlMessageSyncEngineTests {
	
	//@Test
	public void shouldSyncKml() throws DocumentException, IOException{
		
		String fileNameA = this.getClass().getResource("kmlWithSyncInfo.kml").getFile();
		String fileNameB = this.getClass().getResource("kmlDummyForSync.kml").getFile(); 
		
		DOMAdapter kmlAdapterA = new DOMAdapter(KMLDOMLoaderFactory.createDOMLoader(fileNameA, NullIdentityProvider.INSTANCE));
		kmlAdapterA.beginSync();

		DOMAdapter kmlAdapterB = new DOMAdapter(KMLDOMLoaderFactory.createDOMLoader(fileNameB, NullIdentityProvider.INSTANCE));
		kmlAdapterB.beginSync();
		
		// Sync SMS
		String dataSetId = "12345";
		
		MockSmsConnection smsConnectionEndpointA = new MockSmsConnection("A", CompressBase91MessageEncoding.INSTANCE);
		//smsConnectionEndpointA.activateTrace();
		
		MockSmsConnection smsConnectionEndpointB = new MockSmsConnection("B", CompressBase91MessageEncoding.INSTANCE);
		//smsConnectionEndpointB.activateTrace();
		
		smsConnectionEndpointA.setEndPoint(smsConnectionEndpointB);
		smsConnectionEndpointB.setEndPoint(smsConnectionEndpointA);
		
		IChannel channelEndpointA = SmsChannelFactory.createChannel(smsConnectionEndpointA, 5000, 5000, MessageSyncProtocolFactory.getProtocolMessageFilter());
		IChannel channelEndpointB = SmsChannelFactory.createChannel(smsConnectionEndpointB, 5000, 5000, MessageSyncProtocolFactory.getProtocolMessageFilter());

		IMessageSyncAdapter endpointA = new MockInMemoryMessageSyncAdapter(dataSetId, kmlAdapterA.getAll());
		
		ISourceIdMapper sourceIdMapper = new ISourceIdMapper(){

			@Override
			public String getSourceDefinition(String sourceId) {
				//TestHelper.baseDirectoryForTest();
				return sourceId;
			}
			
			@Override
			public void removeSourceDefinition(String sourceId) {
				// nothing to do
			}
			
		};
		
		KMLDOMLoaderFactory kmlfactory = new KMLDOMLoaderFactory();
		MessageSyncAdapterFactory syncAdapterFactory = new MessageSyncAdapterFactory(sourceIdMapper, null, false, kmlfactory);
		SyncSessionFactory syncSessionFactoryA = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, syncAdapterFactory);
		syncSessionFactoryA.registerSource(endpointA);
	
		//ISyncSessionRepository repoA = new MockSyncSessionRepository(syncSessionFactoryA);
		ISyncSessionRepository repoA = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), syncSessionFactoryA);
		
		IMessageSyncProtocol syncProtocolA = MessageSyncProtocolFactory.createSyncProtocol(getItemEncoding(), repoA, channelEndpointA);		
		MessageSyncEngine syncEngineEndPointA = new MessageSyncEngine(syncProtocolA, channelEndpointA);

		MockInMemoryMessageSyncAdapter endpointB = new MockInMemoryMessageSyncAdapter(dataSetId, kmlAdapterB.getAll());
		
		SyncSessionFactory syncSessionFactoryB = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, syncAdapterFactory);
		syncSessionFactoryB.registerSource(endpointB);
		
		//ISyncSessionRepository repoB = new MockSyncSessionRepository(syncSessionFactoryB);
		ISyncSessionRepository repoB = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), syncSessionFactoryB);
		
		IMessageSyncProtocol syncProtocolB = MessageSyncProtocolFactory.createSyncProtocol(getItemEncoding(), repoB, channelEndpointA);
		MessageSyncEngine syncEngineEndPointB = new MessageSyncEngine(syncProtocolB, channelEndpointB);
		Assert.assertNotNull(syncEngineEndPointB);
		
		Assert.assertEquals(611, endpointA.getAll().size());
		Assert.assertEquals(0, endpointB.getAll().size());
		
		syncEngineEndPointA.synchronize(endpointA, new SmsEndpoint("B"));
	
		System.out.println("batch A: " 
				+ smsConnectionEndpointA.getGeneratedMessagesSizeStatistics() 
				+ " messages: " + smsConnectionEndpointA.getGeneratedMessagesStatistics());
		
		System.out.println("batch B: " 
				+ smsConnectionEndpointB.getGeneratedMessagesSizeStatistics() 
				+ " messages: " + smsConnectionEndpointB.getGeneratedMessagesStatistics());
		
		ISyncSession syncSessionAB = syncSessionFactoryA.get(dataSetId, "B");
		ISyncSession syncSessionBA = syncSessionFactoryB.get(dataSetId, "A");
		Assert.assertFalse(syncSessionAB.isOpen());
		Assert.assertFalse(syncSessionBA.isOpen());

		Assert.assertEquals(611, syncSessionFactoryA.get(dataSetId, "B").getSnapshot().size());
		Assert.assertEquals(611, syncSessionFactoryB.get(dataSetId, "A").getSnapshot().size());
		Assert.assertFalse(syncSessionFactoryA.get(dataSetId, "B").isOpen());
		Assert.assertFalse(syncSessionFactoryB.get(dataSetId, "A").isOpen());

		// Sync KML file
		ISyncSession syncSession = syncSessionFactoryB.get(dataSetId, "A");
		endpointB = new MockInMemoryMessageSyncAdapter(dataSetId, syncSession.getSnapshot());

		File file = new File(TestHelper.fileName("kmlMessage.kml")); 
		XMLHelper.write(kmlAdapterB.getDOM().toDocument(), file);
		
		DOMAdapter kmlAdapter = new DOMAdapter(KMLDOMLoaderFactory.createDOMLoader(file.getAbsolutePath(), NullIdentityProvider.INSTANCE));
		
		SyncEngine syncEngine = new SyncEngine(kmlAdapter, endpointB);
		List<Item> conflicts = syncEngine.synchronize();
		Assert.assertNotNull(conflicts);
		Assert.assertEquals(0, conflicts.size());
		
		kmlAdapter.beginSync();
		int itemsSize = kmlAdapter.getAll().size();
		Assert.assertEquals(611, itemsSize);
		
		Document document = kmlAdapter.getDOM().toDocument();
		
		int xmlCano = XMLHelper.canonicalizeXML(document).length();
		System.out.println("canon: " + xmlCano + "  messages: " + ((xmlCano / 121) + ((xmlCano % 121) == 0 ? 0 : 1)));
		
		int xmlformat = XMLHelper.formatXML(document, OutputFormat.createCompactFormat()).length();
		System.out.println("format: " + xmlformat + "  messages: " + ((xmlformat / 121) + ((xmlformat % 121) == 0 ? 0 : 1)));
		
		System.out.println("items: " + itemsSize);
		
		System.out.println("batch A (zip+base64): " 
				+ smsConnectionEndpointA.getGeneratedMessagesSizeStatistics() 
				+ " messages: " + smsConnectionEndpointA.getGeneratedMessagesStatistics());
		
		System.out.println("batch B (zip+base64): " 
				+ smsConnectionEndpointB.getGeneratedMessagesSizeStatistics() 
				+ " messages: " + smsConnectionEndpointB.getGeneratedMessagesStatistics());
	}

	private IItemEncoding getItemEncoding() {
		//return new ItemEncoding(100);
		return new ItemEncodingFixedBlock(100);
	}
	
// Statistics:
//	canon: 352708  messages: 3028
//	format: 339745  messages: 2905
//	items: 611
//	File size: 401 kb
//	batch A: 494699 messages: 3656
//	batch B: 19 messages: 1
//	
//	canon: 352749  messages: 2949
//	format: 339786  messages: 2826
//	items: 611
//	File size: 401 kb
//	batch A (zip+base64): 467913 messages: 3354
//	batch B (zip+base64): 20 messages: 1
//	
//	canon: 352722  messages: 2916
//	format: 339759  messages: 2808
//	items: 611
//	File size: 401 kb
//	batch A (zip+base64): 285756 messages: 2132
//	batch B (zip+base64): 20 messages: 1

	@Test
	public void shouldSyncKmlNoChanges() throws Exception{
		syncKml(false, false, CompressBase91MessageEncoding.INSTANCE);
	}
	
	@Test
	public void shouldSyncKmlPlacemarkEndpointAChanged() throws Exception{
		syncKml(true, false, CompressBase91MessageEncoding.INSTANCE);
	}

	@Test
	public void shouldSyncKmlPlacemarkEndpointBChanged() throws Exception{
		syncKml(false, true, CompressBase91MessageEncoding.INSTANCE);
	}
	
	@Test
	public void shouldSyncKmlPlacemarkConflicts() throws Exception{
		syncKml(true, true, CompressBase91MessageEncoding.INSTANCE);

	}
	
	private void syncKml(boolean updateA, boolean updateB, IMessageEncoding messageEncoding) throws InterruptedException{
		
		String fileName = this.getClass().getResource("kmlWithPlacemark.kml").getFile(); 
		DOMAdapter kmlAdapter = new DOMAdapter(KMLDOMLoaderFactory.createDOMLoader(fileName, NullIdentityProvider.INSTANCE));
		kmlAdapter.beginSync();
		
		// Sync SMS
		String dataSetId = "kmlWithPlacemark.kml";

		MockSmsConnection smsConnectionEndpointA = new MockSmsConnection("A", messageEncoding);
		MockSmsConnection smsConnectionEndpointB = new MockSmsConnection("B", messageEncoding);

		smsConnectionEndpointA.setEndPoint(smsConnectionEndpointB);
		smsConnectionEndpointB.setEndPoint(smsConnectionEndpointA);
		
		IChannel channelEndpointA = SmsChannelFactory.createChannel(smsConnectionEndpointA, 5000, 5000, MessageSyncProtocolFactory.getProtocolMessageFilter());
		IChannel channelEndpointB = SmsChannelFactory.createChannel(smsConnectionEndpointB, 5000, 5000, MessageSyncProtocolFactory.getProtocolMessageFilter());

		MockInMemoryMessageSyncAdapter endpointA = new MockInMemoryMessageSyncAdapter(dataSetId, kmlAdapter.getAll());
		
		ISourceIdMapper sourceIdMapper = new ISourceIdMapper(){

			@Override
			public String getSourceDefinition(String sourceId) {
				// ""
				return sourceId;
			}

			@Override
			public void removeSourceDefinition(String sourceId) {
				// nothing to do				
			}
			
		};

		
		KMLDOMLoaderFactory kmlFactory = new KMLDOMLoaderFactory();
		MessageSyncAdapterFactory syncAdapterFactory = new MessageSyncAdapterFactory(sourceIdMapper, null, false, kmlFactory);
		SyncSessionFactory syncSessionFactoryA = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, syncAdapterFactory);
		syncSessionFactoryA.registerSource(endpointA);
		
		IMessageSyncProtocol syncProtocolA = MessageSyncProtocolFactory.createSyncProtocol(getItemEncoding(), new MockSyncSessionRepository(syncSessionFactoryA), channelEndpointA);
		MessageSyncEngine syncEngineEndPointA = new MessageSyncEngine(syncProtocolA, channelEndpointA);

		MockInMemoryMessageSyncAdapter endpointB = new MockInMemoryMessageSyncAdapter(dataSetId, kmlAdapter.getAll());
		SyncSessionFactory syncSessionFactoryB = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, syncAdapterFactory);
		syncSessionFactoryB.registerSource(endpointB);
		
		IMessageSyncProtocol syncProtocolB = MessageSyncProtocolFactory.createSyncProtocol(getItemEncoding(), new MockSyncSessionRepository(syncSessionFactoryB), channelEndpointB);
		MessageSyncEngine syncEngineEndPointB = new MessageSyncEngine(syncProtocolB, channelEndpointB);
		Assert.assertNotNull(syncEngineEndPointB);
		
		Assert.assertEquals(2, endpointA.getAll().size());
		Assert.assertEquals(2, endpointB.getAll().size());

		syncEngineEndPointA.synchronize(endpointA, new SmsEndpoint("B"));
		
		ISyncSession syncSessionAB = syncSessionFactoryA.get(dataSetId, "B");
		ISyncSession syncSessionBA = syncSessionFactoryB.get(dataSetId, "A");
		Assert.assertFalse(syncSessionAB.isOpen());
		Assert.assertFalse(syncSessionBA.isOpen());

		
		// A Update item 
		Item itemA = endpointA.getAll().get(0);
		if(!KmlNames.KML_ELEMENT_PLACEMARK.equals(itemA.getContent().getPayload().getName())){
			itemA = endpointA.getAll().get(1);
		}
		
		String syncIDPlacemark = itemA.getSyncId();
		if(updateA){
			Thread.sleep(1000);
			Element placemarkA = itemA.getContent().getPayload();
			
			Element placemarkNameA = placemarkA.element("name");
			placemarkNameA.setText("JMT");
			
			KMLContent kmlContentA = (KMLContent)itemA.getContent();
			kmlContentA.refreshVersion();
			
			itemA.getSync().update("jmt", new Date(), false);
		}
		
		
		// B Update item
		if(updateB){
			Thread.sleep(1000);
			Item itemB = endpointB.getAll().get(0);
			if(!KmlNames.KML_ELEMENT_PLACEMARK.equals(itemB.getContent().getPayload().getName())){
				itemB = endpointB.getAll().get(1);
			}			
			
			Element placemarkB = itemB.getContent().getPayload();
			
			Element placemarkNameB = placemarkB.element("name");
			placemarkNameB.setText("MaR");
			
			KMLContent kmlContentB = (KMLContent)itemB.getContent();
			kmlContentB.refreshVersion();
			
			itemB.getSync().update("bia", new Date(), false);
		}
		
		// sync
		smsConnectionEndpointA.activateTrace();
		smsConnectionEndpointB.activateTrace();
		smsConnectionEndpointA.resetStatistics();		
		smsConnectionEndpointB.resetStatistics();
			
		Assert.assertEquals(2, endpointA.getAll().size());
		Assert.assertEquals(2, endpointB.getAll().size());
	
		syncEngineEndPointA.synchronize(endpointA, new SmsEndpoint("B"));
	
		System.out.println("batch A: " 
				+ smsConnectionEndpointA.getGeneratedMessagesSizeStatistics() 
				+ " messages: " + smsConnectionEndpointA.getGeneratedMessagesStatistics());
		
		System.out.println("batch B: " 
				+ smsConnectionEndpointB.getGeneratedMessagesSizeStatistics() 
				+ " messages: " + smsConnectionEndpointB.getGeneratedMessagesStatistics());
		
		syncSessionAB = syncSessionFactoryA.get(dataSetId, "B");
		syncSessionBA = syncSessionFactoryB.get(dataSetId, "A");

		Assert.assertFalse(syncSessionAB.isOpen());
		Assert.assertFalse(syncSessionBA.isOpen());

		List<Item> snapshotA = syncSessionAB.getSnapshot();
		List<Item> snapshotB = syncSessionAB.getSnapshot();
		
		Assert.assertEquals(2, snapshotA.size());
		Assert.assertEquals(2, snapshotB.size());
		
		Item itemA1 = snapshotA.get(0);
		Item itemA2 = snapshotA.get(1);
		Assert.assertFalse(itemA1.equals(itemA2));
		
		Item itemB1 = syncSessionBA.get(itemA1.getSyncId());
		Item itemB2 = syncSessionBA.get(itemA2.getSyncId());
		
		if(updateA && updateB){     // conflicts
			Assert.assertNotNull(itemB1);
			Assert.assertNotNull(itemB2);
			if(syncIDPlacemark.equals(itemA1.getSyncId())){
				Assert.assertFalse(itemA1.equals(itemB1));
				Assert.assertTrue(itemA2.equals(itemB2));
			} else {
				Assert.assertTrue(itemA1.equals(itemB1));
				Assert.assertFalse(itemA2.equals(itemB2));
			}
		} else {
			Assert.assertNotNull(itemB1);
			Assert.assertTrue(itemA1.equals(itemB1));				
			Assert.assertNotNull(itemB2);
			Assert.assertTrue(itemA2.equals(itemB2));
			
		}
	}
}
