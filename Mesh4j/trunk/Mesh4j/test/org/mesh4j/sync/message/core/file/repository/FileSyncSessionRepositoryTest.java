package org.mesh4j.sync.message.core.file.repository;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.dom4j.Element;
import org.junit.Test;
import org.mesh4j.sync.adapters.feed.ContentReader;
import org.mesh4j.sync.adapters.feed.Feed;
import org.mesh4j.sync.adapters.feed.FeedAdapter;
import org.mesh4j.sync.adapters.feed.FeedReader;
import org.mesh4j.sync.adapters.feed.rss.RssSyndicationFormat;
import org.mesh4j.sync.id.generator.IdGenerator;
import org.mesh4j.sync.message.IEndpoint;
import org.mesh4j.sync.message.IMessageSyncAdapter;
import org.mesh4j.sync.message.ISyncSession;
import org.mesh4j.sync.message.channel.sms.SmsEndpoint;
import org.mesh4j.sync.message.channel.sms.core.SmsEndpointFactory;
import org.mesh4j.sync.message.core.InMemoryMessageSyncAdapter;
import org.mesh4j.sync.message.core.MessageSyncAdapter;
import org.mesh4j.sync.message.core.SyncSession;
import org.mesh4j.sync.message.core.repository.ISourceIdMapper;
import org.mesh4j.sync.message.core.repository.ISyncSessionFactory;
import org.mesh4j.sync.message.core.repository.MessageSyncAdapterFactory;
import org.mesh4j.sync.message.core.repository.OpaqueFeedSyncAdapterFactory;
import org.mesh4j.sync.message.core.repository.SyncSessionFactory;
import org.mesh4j.sync.message.core.repository.file.FileSyncSessionRepository;
import org.mesh4j.sync.message.protocol.MockSyncSession;
import org.mesh4j.sync.model.Item;
import org.mesh4j.sync.model.NullContent;
import org.mesh4j.sync.model.Sync;
import org.mesh4j.sync.security.NullIdentityProvider;
import org.mesh4j.sync.test.utils.TestHelper;
import org.mesh4j.sync.utils.DateHelper;
import org.mesh4j.sync.validations.MeshException;


public class FileSyncSessionRepositoryTest {

	@Test(expected=IllegalArgumentException.class)
	public void shouldCreateRepositoryFailsIfDirIsNull(){
		new FileSyncSessionRepository(null, new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), null, false)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldCreateRepositoryFailsIfDirIsEmpty(){
		new FileSyncSessionRepository("", new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), null, false)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldCreateRepositoryFailsIfSessionFactoryIsNull(){
		new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), null);
	}
	
	// FLUSH
	@Test
	public void shouldGetFlushFile() throws IOException{
		OpaqueFeedSyncAdapterFactory feedAdapterFactory = new OpaqueFeedSyncAdapterFactory(TestHelper.baseDirectoryForTest());
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedAdapterFactory, false)));
		File file = repo.getCurrentSessionFile("myFile");
		
		File fileExpected = new File(TestHelper.baseDirectoryForTest() + "myFile_current.xml");
		Assert.assertEquals(fileExpected.getCanonicalPath(), file.getCanonicalPath());
	}

	@Test
	public void shouldFlushOpenSession() throws Exception{

		OpaqueFeedSyncAdapterFactory feedAdapterFactory = new OpaqueFeedSyncAdapterFactory(TestHelper.baseDirectoryForTest());
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedAdapterFactory, false)));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("true", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_OPEN));
	}

	private FeedReader makeFeedReader() {
		return new FeedReader(RssSyndicationFormat.INSTANCE, NullIdentityProvider.INSTANCE, IdGenerator.INSTANCE, ContentReader.INSTANCE);
	}
	
	@Test(expected=MeshException.class)
	public void shouldFlushCloseSessionFails() throws Exception{

		OpaqueFeedSyncAdapterFactory feedAdapterFactory = new OpaqueFeedSyncAdapterFactory(TestHelper.baseDirectoryForTest());
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedAdapterFactory, false)));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("false", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_OPEN));
	
	}

	@Test
	public void shouldFlushFullProtocolSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		syncSession.setFullProtocol(true);
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("true", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_FULL));	
	}

	@Test
	public void shouldFlushLightProtocolSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setFullProtocol(false);
		syncSession.setOpen();
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("false", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_FULL));
	}
	
	@Test
	public void shouldFlushLastSyncDateSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		Date date = TestHelper.makeDate(2008, 1, 1, 1, 1, 1, 1);
		MockSyncSession syncSession = new MockSyncSession(date, null, sessionId);
		syncSession.setOpen();
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals(DateHelper.formatW3CDateTime(date), syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_LAST_SYNC_DATE));
	}
	
	@Test
	public void shouldFlushNonLastSyncDateSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_LAST_SYNC_DATE));
	}
	
	@Test
	public void shouldFlushNonEmptySession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		String syncID = IdGenerator.INSTANCE.newID();
		Item item = new Item(new NullContent(syncID), new Sync(syncID, "jmt", new Date(), true));
		MockSyncSession syncSession = new MockSyncSession(null, item, sessionId);
		syncSession.setOpen();
		Assert.assertFalse(syncSession.getCurrentSnapshot().isEmpty());
		
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Assert.assertNotNull(feed.getItems());
		Assert.assertEquals(1, feed.getItems().size());
		Assert.assertTrue(item.equals(feed.getItems().get(0)));	
	}
	
	@Test
	public void shouldFlushEmptySession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		Assert.assertTrue(syncSession.getCurrentSnapshot().isEmpty());
		
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Assert.assertNotNull(feed.getItems());
		Assert.assertEquals(0, feed.getItems().size());
	}

	@Test
	public void shouldFlushACKSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		String syncID = IdGenerator.INSTANCE.newID();
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		syncSession.waitForAck(syncID);
		
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals(1, syncSessionElement.elements(FileSyncSessionRepository.ELEMENT_ACK).size());
		Assert.assertEquals(syncID, ((Element)syncSessionElement.elements(FileSyncSessionRepository.ELEMENT_ACK).get(0)).getText());
	}
	
	@Test
	public void shouldFlushNonACKSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals(0, syncSessionElement.elements(FileSyncSessionRepository.ELEMENT_ACK).size());
	}
	
	@Test
	public void shouldFlushConflictSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		String syncID = IdGenerator.INSTANCE.newID();
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		syncSession.addConflict(syncID);
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals(1, syncSessionElement.elements(FileSyncSessionRepository.ELEMENT_CONFLICT).size());
		Assert.assertEquals(syncID, ((Element)syncSessionElement.elements(FileSyncSessionRepository.ELEMENT_CONFLICT).get(0)).getText());
	}
	
	@Test
	public void shouldFlushNonConflictSession() throws Exception{
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals(0, syncSessionElement.elements(FileSyncSessionRepository.ELEMENT_CONFLICT).size());
	}
	
	@Test
	public void shouldFlushSendChangesAttrSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		syncSession.setShouldSendChanges(true);
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("true", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_SHOULD_SEND_CHANGES));	
	}

	@Test
	public void shouldFlushNoSendChangesAttrSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		syncSession.setShouldSendChanges(false);
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("false", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_SHOULD_SEND_CHANGES));	
	}
	
	@Test
	public void shouldFlushReceiveChangesAttrSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		syncSession.setShouldReceiveChanges(true);
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("true", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_SHOULD_RECEIVE_CHANGES));	
	}

	@Test
	public void shouldFlushNoReceiveChangesAttrSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		syncSession.setShouldReceiveChanges(false);
		repo.flush(syncSession);
				
		file = repo.getCurrentSessionFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("false", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_SHOULD_RECEIVE_CHANGES));	
	}
	
	// SNAPSHOT
	@Test
	public void shouldGetSnapshotFile() throws IOException{
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		File file = repo.getSnapshotFile("myFile");

		File fileExpected = new File(TestHelper.baseDirectoryForTest() + "myFile_snapshot.xml");
		Assert.assertEquals(fileExpected.getCanonicalPath(), file.getCanonicalPath());
	}
	
	@Test(expected=MeshException.class)
	public void shouldSnapshotOpenSessionFails() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		syncSession.setOpen();
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("true", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_OPEN));
	}
	
	@Test
	public void shouldSnapshotCloseSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("false", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_OPEN));
	
	}

	@Test
	public void shouldSnapshotFullProtocolSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.setFullProtocol(true);
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("true", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_FULL));	
	}
	
	@Test
	public void shouldSnapshotLightProtocolSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.setFullProtocol(false);
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("false", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_FULL));
	}
	
	@Test
	public void shouldSnapshotLastSyncDateSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		Date date = TestHelper.makeDate(2008, 1, 1, 1, 1, 1, 1);
		MockSyncSession syncSession = new MockSyncSession(date, null, sessionId);
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals(DateHelper.formatW3CDateTime(date), syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_LAST_SYNC_DATE));
	}
	
	@Test(expected=MeshException.class)
	public void shouldSnapshotNonLastSyncDateSessionFails() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(null, null, sessionId);
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_LAST_SYNC_DATE));
	}
	
	@Test
	public void shouldSnapshotNonEmptySession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		String syncID = IdGenerator.INSTANCE.newID();
		Item item = new Item(new NullContent(syncID), new Sync(syncID, "jmt", new Date(), true));
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.addToSnapshot(item);
		
		Assert.assertFalse(syncSession.getSnapshot().isEmpty());
		
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Assert.assertNotNull(feed.getItems());
		Assert.assertEquals(1, feed.getItems().size());
		Assert.assertTrue(item.equals(feed.getItems().get(0)));	
	}
	
	@Test
	public void shouldSnapshotEmptySession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		Assert.assertTrue(syncSession.getCurrentSnapshot().isEmpty());
		
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Assert.assertNotNull(feed.getItems());
		Assert.assertEquals(0, feed.getItems().size());
	}

	@Test
	public void shouldSnapshotACKSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		String syncID = IdGenerator.INSTANCE.newID();
		Item item = new Item(new NullContent(syncID), new Sync(syncID, "jmt", new Date(), true));
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.addToSnapshot(item);
		syncSession.waitForAck(syncID);
		
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals(0, syncSessionElement.elements(FileSyncSessionRepository.ELEMENT_ACK).size());
	}
	
	@Test
	public void shouldSnapshotNonACKSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		String syncID = IdGenerator.INSTANCE.newID();
		Item item = new Item(new NullContent(syncID), new Sync(syncID, "jmt", new Date(), true));
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.addToSnapshot(item);

		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals(0, syncSessionElement.elements(FileSyncSessionRepository.ELEMENT_ACK).size());
	}
	
	@Test
	public void shouldSnapshotConflictSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		String syncID = IdGenerator.INSTANCE.newID();
		Item item = new Item(new NullContent(syncID), new Sync(syncID, "jmt", new Date(), true));
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.addToSnapshot(item);
		syncSession.addConflict(syncID);
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals(1, syncSessionElement.elements(FileSyncSessionRepository.ELEMENT_CONFLICT).size());
		Assert.assertEquals(syncID, ((Element)syncSessionElement.elements(FileSyncSessionRepository.ELEMENT_CONFLICT).get(0)).getText());
	}
	
	@Test
	public void shouldSnapshotNonConflictSession() throws Exception{
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		String syncID = IdGenerator.INSTANCE.newID();
		Item item = new Item(new NullContent(syncID), new Sync(syncID, "jmt", new Date(), true));
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.addToSnapshot(item);
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals(0, syncSessionElement.elements(FileSyncSessionRepository.ELEMENT_CONFLICT).size());
	}
	
	@Test
	public void shouldSnapshotDeleteCurrentFile(){
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());

		File fileCurrent = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(fileCurrent.exists());

		String syncID = IdGenerator.INSTANCE.newID();
		Item item = new Item(new NullContent(syncID), new Sync(syncID, "jmt", new Date(), true));
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.addToSnapshot(item);
		syncSession.setOpen();
		repo.flush(syncSession);
		
		Assert.assertFalse(file.exists());
		Assert.assertTrue(fileCurrent.exists());
		
		syncSession.setClose();
		repo.snapshot(syncSession);
		
		Assert.assertTrue(file.exists());
		Assert.assertFalse(fileCurrent.exists());
		
		repo.snapshot(syncSession);
		
		Assert.assertTrue(file.exists());
		Assert.assertFalse(fileCurrent.exists());
	}
	
	@Test
	public void shouldSnapshotAddSendChangesAttrSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.setShouldSendChanges(true);
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("true", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_SHOULD_SEND_CHANGES));	
	}
	
	@Test
	public void shouldSnapshotAddNoSendChangesAttrSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.setShouldSendChanges(false);
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("false", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_SHOULD_SEND_CHANGES));	
	}
	
	@Test
	public void shouldSnapshotAddReceiveChangesAttrSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.setShouldReceiveChanges(true);
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("true", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_SHOULD_RECEIVE_CHANGES));	
	}
	
	@Test
	public void shouldSnapshotAddNoReceiveChangesAttrSession() throws Exception{

		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getSnapshotFile(sessionId);
		Assert.assertFalse(file.exists());
		
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.setShouldReceiveChanges(false);
		repo.snapshot(syncSession);
				
		file = repo.getSnapshotFile(sessionId);
		Assert.assertTrue(file.exists());
		
		FeedReader feedReader = makeFeedReader();
		Feed feed = feedReader.read(file);

		Element syncSessionElement = feed.getPayload().element(FileSyncSessionRepository.ELEMENT_SYNC_SESSION);
		Assert.assertNotNull(syncSessionElement);
		Assert.assertEquals("false", syncSessionElement.attributeValue(FileSyncSessionRepository.ATTRIBUTE_SHOULD_RECEIVE_CHANGES));	
	}
	
	// DELETE CURRENT SESSION
	@Test
	public void shouldDeleteCurrentSessionFile(){
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.setOpen();
		repo.flush(syncSession);
		Assert.assertTrue(file.exists());
		
		repo.deleteCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
	}

	@Test
	public void shouldDeleteCurrentSessionFileNoEffectWhenFileDoesNotExist(){
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
				
		repo.deleteCurrentSessionFile(sessionId);
		Assert.assertFalse(file.exists());
	}
	
	@Test
	public void shouldCancelCurrentSession(){
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = IdGenerator.INSTANCE.newID();
		
		File file = repo.getCurrentSessionFile(sessionId);
		MockSyncSession syncSession = new MockSyncSession(new Date(), null, sessionId);
		syncSession.setOpen();
		repo.flush(syncSession);
		Assert.assertTrue(file.exists());
		
		repo.cancel(syncSession);
		Assert.assertTrue(file.exists());
		
		Assert.assertFalse(repo.readSession(syncSession.getSessionId()).isCancelled());
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldCancelFailsWhenSessionIsNull(){
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		repo.cancel(null);
	}
	
	// SESSION READ
	
	@Test
	public void shouldReadSnapshot(){
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		String sessionId = "t3";
		String syncID = IdGenerator.INSTANCE.newID();
		
		Item item = new Item(new NullContent(syncID), new Sync(syncID, "jmt", new Date(), true));
		MockSyncSession syncSession = new MockSyncSession(new Date(), item, sessionId);
		syncSession.addToSnapshot(item);
		
		repo.snapshot(syncSession);
		
		List<Item> items = repo.readSnapshot(syncSession.getSessionId());
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		Assert.assertTrue(item.equals(items.get(0)));		
	}
	
	@Test(expected=MeshException.class)
	public void shouldReadSessionFailsWhenSnapshotAndCurrentSessionDoesNotExist(){
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory());
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), sessionFactory);
				
		String sessionId = "123";
		repo.readSession(sessionId);
	}
	
	@Test(expected=MeshException.class)
	public void shouldReadSessionSnapshotFailsWhenSessionIdAndFileNameAreNotEquals(){
		String sessionId = "exampleSnapshotWithErrorInSessionID";
		File file = new File(this.getClass().getResource(sessionId + "_snapshot.xml").getFile());
		
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));

		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		repo.readSession(sessionId);
	}
	
	@Test(expected=MeshException.class)
	public void shouldReadSessionCurrentFailsWhenSessionIdAndFileNameAreNotEquals(){
		String sessionId = "exampleCurrentWithErrorInSessionID";
		File file = new File(this.getClass().getResource(sessionId + "_current.xml").getFile());
		
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		repo.readSession(sessionId);
	}
	
	@Test(expected=MeshException.class)
	public void shouldReadSessionSnapshotAndCurrentFailsWhenSessionIdAndFileNameAreNotEquals(){
		String sessionId = "exampleSnapshotAndCurrentWithErrorInSessionID";
		File file = new File(this.getClass().getResource(sessionId + "_snapshot.xml").getFile());
		
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		repo.readSession(sessionId);
	}
	
	@Test
	public void shouldReadCloseSession(){
		String sessionId = "example1";
		File file = new File(this.getClass().getResource(sessionId + "_snapshot.xml").getFile());
		
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertEquals(1, syncSessionLoaded.getSnapshot().size());
		Assert.assertEquals(0, syncSessionLoaded.getCurrentSnapshot().size());
	}
	
	@Test
	public void shouldReadOpenSession(){
		String sessionId = "example2";
		File file = new File(this.getClass().getResource(sessionId + "_current.xml").getFile());

		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertEquals(0, syncSessionLoaded.getSnapshot().size());
		Assert.assertEquals(1, syncSessionLoaded.getCurrentSnapshot().size());
	}
	
	@Test
	public void shouldReadOpenSessionWithPreviousSnapshot(){
		String sessionId = "example3";
		File file = new File(this.getClass().getResource(sessionId + "_snapshot.xml").getFile());

		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));

		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertEquals(1, syncSessionLoaded.getSnapshot().size());
		Assert.assertEquals(1, syncSessionLoaded.getCurrentSnapshot().size());
	}
	

	@Test
	public void shouldReadFullProtocolSession() throws Exception{
		String sessionId = "example4";
		File file = new File(this.getClass().getResource(sessionId + "_snapshot.xml").getFile());

		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertTrue(syncSessionLoaded.isFullProtocol());
	}
	
	@Test
	public void shouldReadLightProtocolSession() throws Exception{
		String sessionId = "example5";
		File file = new File(this.getClass().getResource(sessionId + "_snapshot.xml").getFile());
		
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertFalse(syncSessionLoaded.isFullProtocol());
	}
	
	@Test
	public void shouldReadLastSyncDateSession() throws Exception{
		String sessionId = "example4";
		File file = new File(this.getClass().getResource(sessionId + "_snapshot.xml").getFile());
		
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertEquals(DateHelper.parseW3CDateTime("2008-06-30T18:31:23Z"), syncSessionLoaded.getLastSyncDate());
	}
	
	@Test
	public void shouldReadNonLastSyncDateSessionFails() throws Exception{
		String sessionId = "example6";
		File file = new File(this.getClass().getResource(sessionId + "_current.xml").getFile());
		
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertNull(syncSessionLoaded.getLastSyncDate());
	}
	
	@Test
	public void shouldReadACKSession() throws Exception{
		String sessionId = "example7";
		File file = new File(this.getClass().getResource(sessionId + "_current.xml").getFile());
		
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));

		Assert.assertFalse(syncSessionLoaded.isCompleteSync());
		Assert.assertEquals(1, syncSessionLoaded.getAllPendingACKs().size());
		Assert.assertEquals("607d3605-f8e2-4a9a-8ba6-806dd15ac3e6", syncSessionLoaded.getAllPendingACKs().get(0));
	}
	
	@Test
	public void shouldReadNonACKSession() throws Exception{
		String sessionId = "example8";
		File file = new File(this.getClass().getResource(sessionId + "_current.xml").getFile());
		
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertTrue(syncSessionLoaded.isCompleteSync());
		Assert.assertEquals(0, syncSessionLoaded.getAllPendingACKs().size());
	}
	
	@Test
	public void shouldReadConflictSession() throws Exception{
		String sessionId = "example9";
		File file = new File(this.getClass().getResource(sessionId + "_current.xml").getFile());
		
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
		
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertEquals(1, syncSessionLoaded.getConflictsSyncIDs().size());
		Assert.assertTrue(syncSessionLoaded.hasConflict("8c3f341f-51ab-42ac-8a19-e2cffb0530ad"));
	}
	
	@Test
	public void shouldReadNonConflictSession() throws Exception{
		String sessionId = "example8";
		File file = new File(this.getClass().getResource(sessionId + "_current.xml").getFile());
		
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertEquals(0, syncSessionLoaded.getConflictsSyncIDs().size());
	}
	
	@Test
	public void shouldReadSendChangesAttrSession() throws Exception{
		String sessionId = "example4";
		File file = new File(this.getClass().getResource(sessionId + "_snapshot.xml").getFile());

		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertTrue(syncSessionLoaded.shouldSendChanges());
	}
	
	@Test
	public void shouldReadNoSendChangesAttrSession() throws Exception{
		String sessionId = "example6";
		File file = new File(this.getClass().getResource(sessionId + "_current.xml").getFile());

		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertFalse(syncSessionLoaded.shouldSendChanges());
	}
	
	@Test
	public void shouldReadReceiveChangesArrtSession() throws Exception{
		String sessionId = "example5";
		File file = new File(this.getClass().getResource(sessionId + "_snapshot.xml").getFile());

		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertTrue(syncSessionLoaded.shouldReceiveChanges());
	}
	
	@Test
	public void shouldReadNoReceiveChangesAttrSession() throws Exception{
		String sessionId = "example7";
		File file = new File(this.getClass().getResource(sessionId + "_current.xml").getFile());

		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(file.getParent()+"\\");
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		sessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		Assert.assertNotNull(syncSessionLoaded);		
		Assert.assertSame(syncSessionLoaded, sessionFactory.get(sessionId));
		
		Assert.assertFalse(syncSessionLoaded.shouldReceiveChanges());
	}

	// CREATE SESSION
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldCreateSessionFailsIfSourceIDIsNull(){
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		repo.createSession(null, 0, "1234", new SmsEndpoint("sms:1"), false, true, true);
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldCreateSessionFailsIfEndPointIsNull(){
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory()));
		repo.createSession("123",0, null, new SmsEndpoint("sms:1"), false, true, true);
	}
	
	@Test
	public void shouldCreateSession(){
		String sourceID = "1234";
		IEndpoint endpoint = new SmsEndpoint("sms:1");

		SyncSessionFactory sessionFac = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory());
		sessionFac.registerSource(new InMemoryMessageSyncAdapter(sourceID));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), sessionFac);
				
		ISyncSession syncSession = repo.createSession("111", 1, sourceID, endpoint, false, true, true);
		
		Assert.assertNotNull(syncSession);
		Assert.assertEquals("111", syncSession.getSessionId());
		Assert.assertEquals(1, syncSession.getVersion());
		Assert.assertFalse(syncSession.isOpen());
		Assert.assertNotNull(syncSession.getAll());
		Assert.assertEquals(0, syncSession.getAll().size());
		Assert.assertNotNull(syncSession.getSnapshot());
		Assert.assertEquals(0, syncSession.getSnapshot().size());
		Assert.assertNull(syncSession.getLastSyncDate());
		Assert.assertEquals(sourceID, syncSession.getSourceId());
		Assert.assertEquals(endpoint, syncSession.getTarget());

	}
	
	@Test
	public void shouldGetSession(){
		String sourceID = "1234";
		IEndpoint endpoint = new SmsEndpoint("sms:1");

		SyncSessionFactory sessionFac = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory());
		sessionFac.registerSource(new InMemoryMessageSyncAdapter(sourceID));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), sessionFac);
				
		ISyncSession syncSession = repo.createSession("111", 3, sourceID, endpoint, false, true, true);
		
		Assert.assertNotNull(syncSession);
		Assert.assertSame(syncSession, repo.getSession("111"));
	}
	
	@Test
	public void shouldCreateSessionBySourceIDAndEndpoint(){
		String sourceID = "1234";
		IEndpoint endpoint = new SmsEndpoint("sms:1");

		SyncSessionFactory sessionFac = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, createMessageSyncAdapterFactory());
		sessionFac.registerSource(new InMemoryMessageSyncAdapter(sourceID));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(TestHelper.baseDirectoryForTest(), sessionFac);
				
		ISyncSession syncSession = repo.createSession("111", 0, sourceID, endpoint, false, true, true);
		
		Assert.assertNotNull(syncSession);
		Assert.assertSame(syncSession, repo.getSession(sourceID, endpoint.getEndpointId()));
	}
	
	@Test
	public void shouldReadAllSessions(){
		File file = new File(this.getClass().getResource("example2_current.xml").getFile());
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(TestHelper.baseDirectoryForTest());
		SyncSessionFactory syncSessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		syncSessionFactory.registerSource(new InMemoryMessageSyncAdapter("123"));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", syncSessionFactory);
		
		List<ISyncSession> all = repo.readAllSessions();
		Assert.assertNotNull(all);
		Assert.assertEquals(9, all.size());
		Assert.assertEquals(9, syncSessionFactory.getAll().size());
		
	}
	
	@Test
	public void shouldReadAllSessionsReturnsSessionsWithFeedAdapterWhenNoSourceIsRegistered(){
		File file = new File(this.getClass().getResource("example2_current.xml").getFile());
		
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(TestHelper.baseDirectoryForTest());
		SyncSessionFactory syncSessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
				
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", syncSessionFactory);
		
		List<ISyncSession> all = repo.readAllSessions();
		Assert.assertNotNull(all);
		Assert.assertEquals(9, all.size());
		Assert.assertEquals(9, syncSessionFactory.getAll().size());
		
	}
	
	@Test
	public void shouldSessionUseFeedAdapterWhenSourceIsNotRegistered(){
		String sessionId = "example2";
		File file = new File(this.getClass().getResource(sessionId + "_current.xml").getFile());

		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(TestHelper.baseDirectoryForTest());
		ISyncSessionFactory sessionFactory = new SyncSessionFactory(SmsEndpointFactory.INSTANCE, new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false));
		
		FileSyncSessionRepository repo = new FileSyncSessionRepository(file.getParent()+"\\", sessionFactory);
				
		ISyncSession syncSessionLoaded = repo.readSession(sessionId);
		IMessageSyncAdapter adapter = ((SyncSession)syncSessionLoaded).getSyncAdapter();
		Assert.assertEquals(MessageSyncAdapter.class.getName(), adapter.getClass().getName());
		Assert.assertEquals(FeedAdapter.class.getName(), ((MessageSyncAdapter)adapter).getSyncAdapter().getClass().getName());

	}
	
	private MessageSyncAdapterFactory createMessageSyncAdapterFactory() {
		OpaqueFeedSyncAdapterFactory feedFactory =  new OpaqueFeedSyncAdapterFactory(TestHelper.baseDirectoryForTest());
		return new MessageSyncAdapterFactory(getNullSourceIdMapper(), feedFactory, false);
	}

	private ISourceIdMapper getNullSourceIdMapper() {
		ISourceIdMapper sourceIdMapper = new ISourceIdMapper(){
			@Override
			public String getSourceDefinition(String sourceId) {
				return sourceId;
			}
			
			@Override
			public void removeSourceDefinition(String sourceId) {
				// nothing to do
			}
		};
		return sourceIdMapper;
	}
}
