package org.mesh4j.sync.message.protocol;

import java.util.Date;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Assert;
import org.junit.Test;
import org.mesh4j.sync.adapters.feed.XMLContent;
import org.mesh4j.sync.id.generator.IdGenerator;
import org.mesh4j.sync.message.ISyncSession;
import org.mesh4j.sync.model.IContent;
import org.mesh4j.sync.model.Item;
import org.mesh4j.sync.model.NullContent;
import org.mesh4j.sync.model.Sync;
import org.mesh4j.sync.test.utils.TestHelper;
import org.mesh4j.sync.utils.XMLHelper;


public class ItemEncodingFixedBlockTests {
	
	@Test
	public void shouldEncodeDeletedItemWithOutSinceDate(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0), true);
		
		IContent content = new NullContent(syncID);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(null);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[0]);
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("T");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1~");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2~");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeDeletedItemWithOutSinceDate(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0), true);
		
		IContent content = new NullContent(syncID);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(null);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("T");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1~");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2~");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		
		Item decodedItem = itemEncoding.decode(syncSession, sb.toString());
		Assert.assertNotNull(decodedItem);
		Assert.assertTrue(item.equals(decodedItem));
	}
	
	@Test
	public void shouldEncodeDeletedItemWithSinceDateLessThanFirstHistory(){
		
		Date sinceDate = TestHelper.makeDate(2008, 5, 1, 1, 1, 0, 0);
		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0), true);
		
		IContent content = new NullContent(syncID);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(sinceDate);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[0]);
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("T");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1~");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2~");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeDeletedItemWithSinceDateLessThanFirstHistory(){
		
		Date sinceDate = TestHelper.makeDate(2008, 5, 1, 1, 1, 0, 0);
		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0), true);
		
		IContent content = new NullContent(syncID);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(sinceDate);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("T");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1~");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2~");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
	
		Item decodedItem = itemEncoding.decode(syncSession, sb.toString());
		Assert.assertNotNull(decodedItem);
		Assert.assertTrue(item.equals(decodedItem));
	}
		
	@Test
	public void shouldEncodeDeletedItemWithSinceDateLessThanSecondHistory(){
		
		Date sinceDate = TestHelper.makeDate(2008, 6, 5, 1, 1, 0, 0);
		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0), true);
		
		IContent content = new NullContent(syncID);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(sinceDate);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[0]);
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("T");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2~");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeDeletedItemWithSinceDateLessThanSecondHistory(){
		
		Date sinceDate = TestHelper.makeDate(2008, 6, 5, 1, 1, 0, 0);
		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0), true);
		
		IContent content = new NullContent(syncID);
		Item item = new Item(content, sync);
		
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);

		Element payload = DocumentHelper.createElement("foo");
		IContent localContent = new XMLContent(syncID, "", "", payload);
		Item localItem = new Item(localContent, new Sync(syncID, "jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0), false));
		ISyncSession syncSession = new MockSyncSession(sinceDate, localItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("T");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2~");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		
		Item decodedItem = itemEncoding.decode(syncSession, sb.toString());
		Assert.assertNotNull(decodedItem);
		Assert.assertTrue(item.equals(decodedItem));
	}
	
	@Test
	public void shouldEncodeDeletedItemWithSinceDateLessThanLastHistory(){
		
		Date sinceDate = TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 600);
		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0), true);
		
		IContent content = new NullContent(syncID);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(sinceDate);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[0]);
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("T");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeDeletedItemWithSinceDateLessThanLastHistory(){
		
		Date sinceDate = TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 600);
		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0), true);
		
		IContent content = new NullContent(syncID);
		Item item = new Item(content, sync);
			
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);

		Element payload = DocumentHelper.createElement("foo");
		IContent localContent = new XMLContent(syncID, "", "", payload);
		Item localItem = new Item(
			localContent, 
			new Sync(syncID, "jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0), false)
				.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0)));
		ISyncSession syncSession = new MockSyncSession(sinceDate, localItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("T");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		
		Item decodedItem = itemEncoding.decode(syncSession, sb.toString());
		Assert.assertNotNull(decodedItem);
		Assert.assertTrue(item.equals(decodedItem));
	}
	
	@Test
	public void shouldEncodeItemWithOutSinceDate(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID)
			.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0))
			.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0))
			.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0));
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID,"","", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(null);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(5);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[0]);
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1~");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2~");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		sb.append("|00<foo>01</foo02>");
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeItemWithOutSinceDate(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID)
			.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0))
			.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0))
			.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0));
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID,"","", payload);
		Item item = new Item(content, sync);
		
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(5);

		ISyncSession syncSession = new MockSyncSession(null);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1~");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2~");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		sb.append("|00<foo>01</foo02>");
		
		Item decodedItem = itemEncoding.decode(syncSession, sb.toString());
		Assert.assertNotNull(decodedItem);
		Assert.assertTrue(item.equals(decodedItem));
	}
	
	@Test
	public void shouldEncodeItemWhenPayloadLenghtIsLessThanBlockSize(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID, "jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0), false);
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID,"","", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(null);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(500);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[0]);
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1");
		sb.append("|00<foo></foo>");
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeItemWhenPayloadLenghtIsLessThanBlockSize(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID, "jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0), false);
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID,"","", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(null);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(500);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1");
		sb.append("|00<foo></foo>");
		
		Item decodedItem = itemEncoding.decode(syncSession, sb.toString());
		Assert.assertNotNull(decodedItem);
		Assert.assertTrue(item.equals(decodedItem));
	}
	
	@Test
	public void shouldEncodeItemWhenPayloadLenghtIsEqualsToBlockSize(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID, "jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0), false);
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID,"","", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(null);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(11);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[0]);
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1");
		sb.append("|00<foo></foo>");
		
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeItemWhenPayloadLenghtIsEqualsToBlockSize(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID, "jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0), false);
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID,"","", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(null);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(11);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1");
		sb.append("|00<foo></foo>");
		
		Item decoItem = itemEncoding.decode(syncSession, sb.toString());
		Assert.assertNotNull(decoItem);
		Assert.assertTrue(item.equals(decoItem));
	}
	
	@Test
	public void shouldEncodeItemWhenPayloadLenghtIsGreaterThanBlockSize(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID, "jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0), false);
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID,"","", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(null);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(3);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[0]);
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1");
		sb.append("|00<fo01o><02/fo03o>");
		
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeItemWhenPayloadLenghtIsGreaterThanBlockSize(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID, "jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0), false);
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID,"","", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(null);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(3);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1");
		sb.append("|00<fo01o><02/fo03o>");
		
		Item decodeItem = itemEncoding.decode(syncSession, sb.toString());
		Assert.assertNotNull(decodeItem);
		Assert.assertTrue(item.equals(decodeItem));
	}
	
	@Test
	public void shouldEncodeItemWhenPayloadWithoutDiffs(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID, "jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0), false);
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID,"","", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(null);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[]{XMLHelper.canonicalizeXML(payload).hashCode()});
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1");
		sb.append("|");
		
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeItemWhenPayloadWithoutDiffs(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID,"","", payload);
		Item item = new Item(content, sync);
		
		Sync localSync = new Sync(syncID);
		localSync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));

		Item localItem = new Item(content, localSync);
		
		ISyncSession syncSession = new MockSyncSession(null, localItem);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2");
		sb.append("|");		
		
		Item decodedItem = itemEncoding.decode(syncSession, sb.toString());
		Assert.assertNotNull(decodedItem);
		
		Assert.assertTrue(item.equals(decodedItem));
	}
	
	@Test
	public void shouldEncodeItemWhenPayloadWithDiffs(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID, "jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0), false);
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID,"","", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(null);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(5);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[]{"<foo>".hashCode(), 33, 35});
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1");
		sb.append("|01</foo02>");
		
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeItemWhenPayloadWithDiffs(){		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID, "jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0), false);
		
		Element payload = DocumentHelper.createElement("foo");
		payload.addElement("bar");
		IContent content = new XMLContent(syncID,"","", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(null, item);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(5);
		Item decodedItem = itemEncoding.decode(syncSession, syncID+"F1214928060000jmt1|01<rab>02Examp03le</r04ab></05foo>");
		Assert.assertNotNull(decodedItem);
		Assert.assertFalse(item.equals(decodedItem));
		Assert.assertEquals("<foo><rab>Example</rab></foo>", decodedItem.getContent().getPayload().asXML());
	}
	
	@Test
	public void shouldEncodeItemWithSinceDateLessThanFirstHistory(){
		
		Date sinceDate = TestHelper.makeDate(2008, 5, 1, 1, 1, 0, 0);
		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0));
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID, "", "", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(sinceDate);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[0]);
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1~");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2~");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		sb.append("|00<foo></foo>");
		
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeItemWithSinceDateLessThanFirstHistory(){
		
		Date sinceDate = TestHelper.makeDate(2008, 5, 1, 1, 1, 0, 0);
		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0));
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID, "", "", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(sinceDate);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0).getTime());
		sb.append("jmt1~");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2~");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		sb.append("|00<foo></foo>");
		
		Item decodedItem = itemEncoding.decode(syncSession, sb.toString());
		Assert.assertNotNull(decodedItem);
		Assert.assertEquals(item, decodedItem);
	}
		
	@Test
	public void shouldEncodeItemWithSinceDateLessThanSecondHistory(){
		
		Date sinceDate = TestHelper.makeDate(2008, 6, 5, 1, 1, 0, 0);
		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0));
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID, "", "", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(sinceDate);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[0]);
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2~");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		sb.append("|00<foo></foo>");
		
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeItemWithSinceDateLessThanSecondHistory(){
		
		Date sinceDate = TestHelper.makeDate(2008, 6, 5, 1, 1, 0, 0);
		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0));
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID, "", "", payload);
		Item item = new Item(content, sync);

		Sync syncLocalItem = new Sync(syncID);
		syncLocalItem.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		Item localItem = new Item(content, syncLocalItem);
		
		ISyncSession syncSession = new MockSyncSession(sinceDate, localItem);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0).getTime());
		sb.append("jmt2~");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		sb.append("|00<foo></foo>");
		
		Item decodedItem = itemEncoding.decode(syncSession, sb.toString());
		Assert.assertNotNull(decodedItem);
		Assert.assertTrue(item.equals(decodedItem));
	}
	
	@Test
	public void shouldEncodeItemWithSinceDateLessThanLastHistory(){
		
		Date sinceDate = TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 600);
		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0));
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID, "", "", payload);
		Item item = new Item(content, sync);
		
		ISyncSession syncSession = new MockSyncSession(sinceDate);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		String encodedItem = itemEncoding.encode(syncSession, item, new int[0]);
		Assert.assertNotNull(encodedItem);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		sb.append("|00<foo></foo>");
		
		Assert.assertEquals(sb.toString(), encodedItem);
	}
	
	@Test
	public void shouldDecodeItemWithSinceDateLessThanLastHistory(){
		
		Date sinceDate = TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 600);
		
		String syncID = IdGenerator.INSTANCE.newID();
		Sync sync = new Sync(syncID);
		sync.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		sync.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		sync.update("jmt3", TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0));
		
		Element payload = DocumentHelper.createElement("foo");
		IContent content = new XMLContent(syncID, "", "", payload);
		Item item = new Item(content, sync);
		
		Sync syncLocalItem = new Sync(syncID);
		syncLocalItem.update("jmt1", TestHelper.makeDate(2008, 6, 1, 1, 1, 0, 0));
		syncLocalItem.update("jmt2", TestHelper.makeDate(2008, 6, 7, 1, 1, 0, 0));
		
		Item localItem = new Item(content, syncLocalItem);
		
		ISyncSession syncSession = new MockSyncSession(sinceDate, localItem);
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		
		StringBuffer sb = new StringBuffer();
		sb.append(syncID);
		sb.append("F");
		sb.append(TestHelper.makeDate(2008, 6, 10, 1, 1, 0, 0).getTime());
		sb.append("jmt3");
		sb.append("|00<foo></foo>");
		
		Item decodedItem = itemEncoding.decode(syncSession, sb.toString());
		Assert.assertNotNull(decodedItem);
		Assert.assertTrue(item.equals(decodedItem));
	}
	
	@Test
	public void shouldCalculateDiffBlockHashCodesReturnsEmptyArrayWhenTextIsNull(){
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		String xml = null;
		Assert.assertTrue(itemEncoding.calculateDiffBlockHashCodes(xml).length == 0);
	}

	@Test
	public void shouldCalculateDiffBlockHashCodesReturnsEmptyArrayWhenTextIsEmpty(){
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(100);
		Assert.assertTrue(itemEncoding.calculateDiffBlockHashCodes("").length == 0);
	}
	
	@Test
	public void shouldCalculateDiffBlockHashCodesReturnsHashCodeArrayWithTextHashCodeWhenTextLenghtIsLessThanDiffBlockSize(){
		int diffBlockSize = 5;
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(diffBlockSize);
		int[] diffHashs = itemEncoding.calculateDiffBlockHashCodes("abc");
		
		Assert.assertNotNull(diffHashs);
		Assert.assertEquals(1, diffHashs.length);
		Assert.assertEquals("abc".hashCode(), diffHashs[0]);
	}
	
	@Test
	public void shouldCalculateDiffBlockHashCodesReturnsHashCodeArrayWithTextHashCodeWhenTextLenghtIsEqualsToDiffBlockSize(){
		int diffBlockSize = 5;
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(diffBlockSize);
		int[] diffHashs = itemEncoding.calculateDiffBlockHashCodes("abcde");
		
		Assert.assertNotNull(diffHashs);
		Assert.assertEquals(1, diffHashs.length);
		Assert.assertEquals("abcde".hashCode(), diffHashs[0]);
	}

	@Test
	public void shouldCalculateDiffBlockHashCodesReturnsHashCodeArrayWhenTextLenghtIsGreaterThanDiffBlockSize(){
		int diffBlockSize = 3;
		ItemEncodingFixedBlock itemEncoding = new ItemEncodingFixedBlock(diffBlockSize);
		int[] diffHashs = itemEncoding.calculateDiffBlockHashCodes("abcde");
		
		Assert.assertNotNull(diffHashs);
		Assert.assertEquals(2, diffHashs.length);
		Assert.assertEquals("abc".hashCode(), diffHashs[0]);
		Assert.assertEquals("de".hashCode(), diffHashs[1]);
	}

}
