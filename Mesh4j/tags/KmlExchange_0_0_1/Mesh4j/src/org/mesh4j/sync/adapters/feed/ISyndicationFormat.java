package org.mesh4j.sync.adapters.feed;

import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.mesh4j.sync.model.Item;

public interface ISyndicationFormat {
	
	String getName();

	String getContentType();
	
	boolean isFeedTitle(Element element);
	boolean isFeedDescription(Element element);
	boolean isFeedLink(Element element);
	boolean isAditionalFeedPayload(Element element);
	
	boolean isFeedItem(Element element);
	boolean isFeedItemTitle(Element element);
	boolean isFeedItemDescription(Element element);
	boolean isFeedItemLink(Element element);
	boolean isFeedItemAuthor(Element element);
	boolean isFeedItemPayload(Element element);
	boolean isAditionalFeedItemPayload(Element element);
	
	List<Element> getRootElements(Element root);

	Date parseDate(String dateAsString);
	String formatDate(Date date);

	Element addRootElement(Document document);

	Element addFeedItemElement(Element root, Item item);

	void addFeedInformation(Element payload, String title, String description, String link, Date lastUpdate);

	Element getFeedItemTitleElement(Element itemElement);
	Element addFeedItemTitleElement(Element itemElement, String title);

	Element getFeedItemDescriptionElement(Element itemElement);
	Element addFeedItemDescriptionElement(Element itemElement, String description);

	Element getFeedItemLinkElement(Element itemElement);
	Element addFeedItemLinkElement(Element itemElement, String absoluteLink);
	
	void addAuthorElement(Element itemElement, String author);

	Element getFeedItemPayloadElement(Element itemElement);
	void addFeedItemPayloadElement(Element itemElement, Element payload);

	String getFeedTitle(Element element);
	String getFeedDescription(Element element);
	String getFeedLink(Element element);
	
	String getFeedItemTitle(Element element);
	String getFeedItemDescription(Element element);
	String getFeedItemLink(Element element);
	
	// / <summary>
	// / Namespace of the FeedSync elements.
	// / </summary>
	public static final String NAMESPACE = "http://feedsync.org/2007/feedsync";

	// / <summary>
	// / Default prefix used for FeedSync elements.
	// / </summary>
	public static final String SX_PREFIX = "sx";

	public static final Namespace SX_NS = DocumentHelper.createNamespace(SX_PREFIX, NAMESPACE);

	public static final String SX_ELEMENT_AUTHOR = "author";
	public static final String SX_ELEMENT_NAME = "name";

	public static final QName SX_QNAME_SYNC = DocumentHelper.createQName("sync", SX_NS);
	public static final QName SX_QNAME_SHARING = DocumentHelper.createQName("sharing", SX_NS);
	public static final QName SX_QNAME_RELATED = DocumentHelper.createQName("related", SX_NS);
	public static final QName SX_QNAME_HISTORY = DocumentHelper.createQName("history", SX_NS);
	public static final QName SX_QNAME_CONFLICTS = DocumentHelper.createQName("conflicts", SX_NS);
	

	// sx:sharing
	public static final String SX_ATTRIBUTE_SHARING_SINCE = "since";
	public static final String SX_ATTRIBUTE_SHARING_UNTIL = "until";
	public static final String SX_ATTRIBUTE_SHARING_VERSION = "version";
	public static final String SX_ATTRIBUTE_SHARING_EXPIRES = "expires";
	// sx:related
	public static final String SX_ATTRIBUTE_RELATED_LINK = "link";
	public static final String SX_ATTRIBUTE_RELATED_TITLE = "title";
	public static final String SX_ATTRIBUTE_RELATED_TYPE = "type";
	// sx:sync
	public static final String SX_ATTRIBUTE_SYNC_ID = "id";
	public static final String SX_ATTRIBUTE_SYNC_UPDATES = "updates";
	public static final String SX_ATTRIBUTE_SYNC_DELETED = "deleted";
	public static final String SX_ATTRIBUTE_SYNC_NO_CONFLICTS = "noconflicts";
	
	public static final QName SX_QNAME_SYNC_ID = DocumentHelper.createQName(SX_ATTRIBUTE_SYNC_ID, SX_NS);
	
	// sx:history
	public static final String SX_ATTRIBUTE_HISTORY_SEQUENCE = "sequence";
	public static final String SX_ATTRIBUTE_HISTORY_WHEN = "when";
	public static final String SX_ATTRIBUTE_HISTORY_BY = "by";
	
	// internal
	public static final String ELEMENT_PAYLOAD = "payload";

}
