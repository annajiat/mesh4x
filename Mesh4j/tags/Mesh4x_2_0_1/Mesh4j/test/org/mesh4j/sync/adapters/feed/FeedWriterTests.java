package org.mesh4j.sync.adapters.feed;

import java.util.Date;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Assert;
import org.junit.Test;
import org.mesh4j.sync.adapters.feed.rss.RssSyndicationFormat;
import org.mesh4j.sync.id.generator.IdGenerator;
import org.mesh4j.sync.model.Item;
import org.mesh4j.sync.model.Sync;
import org.mesh4j.sync.security.NullIdentityProvider;
import org.mesh4j.sync.test.utils.TestHelper;
import org.mesh4j.sync.utils.DateHelper;
import org.mesh4j.sync.utils.XMLHelper;


public class FeedWriterTests {

	@Test
	public void shouldWriteContent() throws Exception{
		
		String syncID = IdGenerator.INSTANCE.newID();
		
		Element element = DocumentHelper.createElement("payload");
		Element fooElement = element.addElement("foo");
		fooElement.addElement("bar");
		
		Date date = TestHelper.makeDate(2008, 1, 1, 1, 1, 1, 1);
		XMLContent content = new XMLContent(syncID, "myTitle", "myDesc", element);
		Sync sync = new Sync(syncID, "jmt", date, false);
		Item item = new Item(content, sync);
		
		Element root = DocumentHelper.createElement("items");
		FeedWriter writer = makeFeedWriter();
		writer.write(root, root, item);
		
		Assert.assertEquals(
			XMLHelper.canonicalizeXML("<items><item><guid isPermaLink=\"false\">urn:uuid:"+syncID+"</guid><pubDate>"+DateHelper.formatRFC822(date)+"</pubDate><title>myTitle</title><description>myDesc</description><content:encoded xmlns:content=\"http://purl.org/rss/1.0/modules/content/\">&lt;foo&gt;&lt;bar&gt;&lt;/bar&gt;&lt;/foo&gt;</content:encoded><author>jmt@mesh4x.example</author><sx:sync xmlns:sx=\"http://feedsync.org/2007/feedsync\" deleted=\"false\" id=\""+syncID+"\" noconflicts=\"false\" updates=\"1\"><sx:history by=\"jmt\" sequence=\"1\" when=\""+ DateHelper.formatW3CDateTime(date)+"\"></sx:history></sx:sync></item></items>"), 
			XMLHelper.canonicalizeXML(root.asXML()));

	}

	private FeedWriter makeFeedWriter() {
		return new FeedWriter(RssSyndicationFormat.INSTANCE, NullIdentityProvider.INSTANCE, ContentWriter.INSTANCE);
	}
	
	@Test
	public void shouldWriteContentUpdateTitleAndDescription() throws Exception{
		
		String syncID = IdGenerator.INSTANCE.newID();
		
		Element element = DocumentHelper.createElement("payload");
		Element fooElement = element.addElement("foo");
		fooElement.addElement("bar");
		
//		Element elementTitle = element.addElement(ISyndicationFormat.SX_ELEMENT_ITEM_TITLE);
//		elementTitle.setText("abc");
//		
//		Element elementDescription = element.addElement(ISyndicationFormat.SX_ELEMENT_ITEM_DESCRIPTION);
//		elementDescription.setText("abc");
		
		Date date = TestHelper.makeDate(2008, 1, 1, 1, 1, 1, 1);
		XMLContent content = new XMLContent(syncID, "myTitle", "myDesc", element);
		Sync sync = new Sync(syncID, "jmt", date, false);
		Item item = new Item(content, sync);
		
		Element root = DocumentHelper.createElement("items");
		FeedWriter writer = makeFeedWriter();
		writer.write(root, root, item);
		
		Assert.assertEquals(
			XMLHelper.canonicalizeXML("<items><item><guid isPermaLink=\"false\">urn:uuid:"+syncID+"</guid><pubDate>"+DateHelper.formatRFC822(date)+"</pubDate><title>myTitle</title><description>myDesc</description><content:encoded xmlns:content=\"http://purl.org/rss/1.0/modules/content/\">&lt;foo&gt;&lt;bar&gt;&lt;/bar&gt;&lt;/foo&gt;</content:encoded><author>jmt@mesh4x.example</author><sx:sync xmlns:sx=\"http://feedsync.org/2007/feedsync\" deleted=\"false\" id=\""+syncID+"\" noconflicts=\"false\" updates=\"1\"><sx:history by=\"jmt\" sequence=\"1\" when=\""+DateHelper.formatW3CDateTime(date)+"\"></sx:history></sx:sync></item></items>"), 
			XMLHelper.canonicalizeXML(root.asXML()));
	}
	
	@Test
	public void shouldWriteContentDeleteTitleAndDescription() throws Exception{
		
		String syncID = IdGenerator.INSTANCE.newID();
		
		Element element = DocumentHelper.createElement("payload");
		Element fooElement = element.addElement("foo");
		fooElement.addElement("bar");
		
		Date date = TestHelper.makeDate(2008, 1, 1, 1, 1, 1, 1);
		XMLContent content = new XMLContent(syncID, null, null, element);
		Sync sync = new Sync(syncID, "jmt", date, false);
		Item item = new Item(content, sync);
		
		Element root = DocumentHelper.createElement("items");
		FeedWriter writer = makeFeedWriter();
		writer.write(root, root, item);
		
		Assert.assertEquals(
			XMLHelper.canonicalizeXML("<items><item><guid isPermaLink=\"false\">urn:uuid:"+syncID+"</guid><pubDate>"+DateHelper.formatRFC822(date)+"</pubDate><title>"+syncID+"</title><description>Id: "+content.getId()+" Version: "+ content.getVersion() +"</description><content:encoded xmlns:content=\"http://purl.org/rss/1.0/modules/content/\">&lt;foo&gt;&lt;bar&gt;&lt;/bar&gt;&lt;/foo&gt;</content:encoded><author>jmt@mesh4x.example</author><sx:sync xmlns:sx=\"http://feedsync.org/2007/feedsync\" deleted=\"false\" id=\""+syncID+"\" noconflicts=\"false\" updates=\"1\"><sx:history by=\"jmt\" sequence=\"1\" when=\""+DateHelper.formatW3CDateTime(date)+"\"></sx:history></sx:sync></item></items>"), 
			XMLHelper.canonicalizeXML(root.asXML()));
		
		
	}
}
