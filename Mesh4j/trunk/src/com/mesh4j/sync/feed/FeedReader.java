package com.mesh4j.sync.feed;

import static com.mesh4j.sync.feed.SyndicationFormat.ATTRIBUTE_PAYLOAD;
import static com.mesh4j.sync.feed.SyndicationFormat.SX_ATTRIBUTE_HISTORY_BY;
import static com.mesh4j.sync.feed.SyndicationFormat.SX_ATTRIBUTE_HISTORY_SEQUENCE;
import static com.mesh4j.sync.feed.SyndicationFormat.SX_ATTRIBUTE_HISTORY_WHEN;
import static com.mesh4j.sync.feed.SyndicationFormat.SX_ATTRIBUTE_ITEM_DESCRIPTION;
import static com.mesh4j.sync.feed.SyndicationFormat.SX_ATTRIBUTE_ITEM_TITLE;
import static com.mesh4j.sync.feed.SyndicationFormat.SX_ATTRIBUTE_SYNC_DELETED;
import static com.mesh4j.sync.feed.SyndicationFormat.SX_ATTRIBUTE_SYNC_ID;
import static com.mesh4j.sync.feed.SyndicationFormat.SX_ATTRIBUTE_SYNC_NO_CONFLICTS;
import static com.mesh4j.sync.feed.SyndicationFormat.SX_ELEMENT_CONFLICTS;
import static com.mesh4j.sync.feed.SyndicationFormat.SX_ELEMENT_HISTORY;
import static com.mesh4j.sync.feed.SyndicationFormat.SX_ELEMENT_SYNC;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import com.mesh4j.sync.model.Item;
import com.mesh4j.sync.model.Sync;
import com.mesh4j.sync.security.Security;
import com.mesh4j.sync.utils.IdGenerator;

public class FeedReader {
	
	// MODEL VARIABLES
	SyndicationFormat syndicationFormat;
	
	// BUSINESS METHODS

	public FeedReader(SyndicationFormat syndicationFormat){
		super();
		this.syndicationFormat = syndicationFormat;
	}
	
	public Feed read(URL url) throws DocumentException{
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(url);
		return read(document);
	}
	
	public Feed read(Reader reader) throws DocumentException{
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(reader);
		return read(document);
	}
	
	public Feed read(InputStream inputStream) throws DocumentException{
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(inputStream);
		return read(document);
	}

	public Feed read(InputSource inputSource) throws DocumentException{
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(inputSource);
		return read(document);
	}
		
	public Feed read(File file) throws DocumentException{
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(file);
		return read(document);
	}

	public Feed read(Document document) {
		Feed feed = new Feed();
		Element payload = DocumentHelper.createElement(ATTRIBUTE_PAYLOAD);
		
		Element root = document.getRootElement();
		List<Element> elements = getRootElements(root);
		for (Element element : elements) {
			if(isFeedItem(element)){
				Item item = readItem(element);
				feed.addItem(item);
			} else {
				payload.add(element.detach());
			}
		}

		feed.setPayload(payload);
		return feed;
	}

	@SuppressWarnings("unchecked")
	public Item readItem(Element itemElement) {
		Element payload = DocumentHelper.createElement(ATTRIBUTE_PAYLOAD);
		
		Sync sync = null;
		
		List<Element> elements = itemElement.elements();
		for (Element element : elements) {
			if(SX_ELEMENT_SYNC.equals(element.getName())){
				sync = readSync(element);
		} else {
				payload.add(element.detach());
			}
		}
		
		if(sync == null){
			sync = new Sync(makeNewSyncID(), Security.getAuthenticatedUser(), new Date(), false); 
		}
		
		String title = itemElement.elementText(SX_ATTRIBUTE_ITEM_TITLE);
		String description = itemElement.elementText(SX_ATTRIBUTE_ITEM_DESCRIPTION);
		ItemXMLContent modelItem = new ItemXMLContent(sync.getId(), title, description, payload);
		return new Item(modelItem, sync);
	}

	protected String makeNewSyncID() {
		return IdGenerator.newID();
	}

	@SuppressWarnings("unchecked")
	public Sync readSync(Element syncElement) {
		String syncID = syncElement.attributeValue(SX_ATTRIBUTE_SYNC_ID);
		//int updates = Integer.valueOf(syncElement.attributeValue(SX_ATTRIBUTE_SYNC_UPDATES));
		boolean deleted = Boolean.parseBoolean(syncElement.attributeValue(SX_ATTRIBUTE_SYNC_DELETED));
		boolean noConflicts = Boolean.parseBoolean(syncElement.attributeValue(SX_ATTRIBUTE_SYNC_NO_CONFLICTS));
		//syncElement.asXML()
		
		Sync sync = new Sync(syncID);
		sync.setDeleted(deleted);
		if(noConflicts){
			sync.markWithoutConflicts();
		} else {
			sync.markWithConflicts();
		}
		
		List<Element> elements = syncElement.elements();
		for (Element historyElement : elements) {
			if(SX_ELEMENT_HISTORY.equals(historyElement.getName())){
				int sequence = Integer.valueOf(historyElement.attributeValue(SX_ATTRIBUTE_HISTORY_SEQUENCE));
				Date when = this.parseDate(historyElement.attributeValue(SX_ATTRIBUTE_HISTORY_WHEN));
				String by = historyElement.attributeValue(SX_ATTRIBUTE_HISTORY_BY);
				sync.update(by, when, sequence);
			} 
		}
		
		Element conflicts = syncElement.element(SX_ELEMENT_CONFLICTS);
		if(conflicts != null){
			List<Element> conflicItems = conflicts.elements();
			for (Element itemElement : conflicItems) {
				Item item = readItem(itemElement);
				sync.addConflict(item);
			}
		}
		return sync;
	}
	
	protected boolean isFeedItem(Element element){
		return this.syndicationFormat.isFeedItem(element);
	}
	protected List<Element> getRootElements(Element root){
		return this.syndicationFormat.getRootElements(root);
	}
	
	protected Date parseDate(String dateAsString){
		return this.syndicationFormat.parseDate(dateAsString);
	}
}