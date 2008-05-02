package com.mesh4j.sync.feed.atom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.dom4j.io.XMLWriter;
import org.junit.Assert;
import org.junit.Test;

import com.mesh4j.sync.feed.Feed;
import com.mesh4j.sync.feed.FeedReader;
import com.mesh4j.sync.feed.FeedWriter;

public class AtomFeedWriterTests {

	@Test
	public void shouldWriteRssFeed() throws DocumentException, IOException{
		
		File file = new File(this.getClass().getResource("atom.xml").getFile());
		Assert.assertTrue(file.exists());
		
		FeedReader reader = new FeedReader(AtomSyndicationFormat.INSTANCE);
		Feed feed = reader.read(file);
		
		XMLWriter xmlWriter = new XMLWriter(new FileWriter("c:\\atom1.xml"));
		
		FeedWriter writer = new FeedWriter(AtomSyndicationFormat.INSTANCE);
		writer.write(xmlWriter, feed);
		
		File file2 =  new File("c:\\atom1.xml");
		Assert.assertTrue(file2.exists());
		
		Feed feed2 = reader.read(file2);
		Assert.assertNotNull(feed2);
		// TODO (JMT) test
		
	}
	
}
