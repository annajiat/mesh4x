package com.mesh4j.sync.parsers;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.junit.Assert;
import org.junit.Test;

public class XMLViewTests {

	@Test
	public void shouldNormalizeReturnsNullBecauseElementIsNull(){
		XMLView view = new XMLView();
		Assert.assertNull(view.normalize(null));
	}
	
	@Test
	public void shouldNormalizeReturnsSameElementBecauseNoViewForElement(){
		XMLView view = new XMLView();
		
		Element element = DocumentHelper.createElement("Foo");
		Assert.assertEquals(element, view.normalize(element));
	}
	
	@Test
	public void shouldNormalize() throws DocumentException{
		XMLViewElement elementView = new XMLViewElement("Document");
		
		QName attr1 = DocumentHelper.createQName("fooAttribute", DocumentHelper.createNamespace("foo", "http:\\foo.org"));
		elementView.addAttribute(attr1);
		
		String attr2 = "myAttribute";
		elementView.addAttribute(attr2);
		
		QName ele1 = DocumentHelper.createQName("BAR", DocumentHelper.createNamespace("bar", "http:\\bar.org"));
		elementView.addElement(ele1);
		
		String ele2 = "BarFoo";
		elementView.addElement(ele2);
		
		XMLView view = new XMLView(elementView);
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		"<Document xmlns:foo=\"http:\\foo.org\" xmlns:bar=\"http:\\bar.org\" xmlns:fooBar=\"http:\\foobar.org\" myAttribute=\"2\" foo:fooAttribute=\"1\">"+
		"<name>dummy</name>"+
	   	"<ExtendedData>"+
		"<MESH xmlns:sx=\"http://feedsync.org/2007/feedsync\" version=\"2096103467\">"+
      	"<sx:sync id=\"1\" updates=\"3\" deleted=\"false\" noconflicts=\"false\">"+
      	"<sx:history sequence=\"3\" when=\"2005-05-21T11:43:33Z\" by=\"JEO2000\"/>"+
      	"<sx:history sequence=\"2\" when=\"2005-05-21T10:43:33Z\" by=\"REO1750\"/>"+
      	"<sx:history sequence=\"1\" when=\"2005-05-21T09:43:33Z\" by=\"REO1750\"/>"+
     	"</sx:sync>"+
		"</MESH>"+
      	"</ExtendedData>"+
		"<Placemark foo:id=\"1\">"+
		"<name>B</name>"+
		"</Placemark>"+
		"<bar:BAR>"+
		"<name>B</name>"+
		"</bar:BAR>"+
		"<BarFoo>"+
		"<name>C</name>"+
		"</BarFoo>"+
		"</Document>";
		
		Element element = DocumentHelper.parseText(xml).getRootElement();
		Element normalizedElement = view.normalize(element);
		
		Assert.assertNotNull(normalizedElement);
		Assert.assertFalse(element == normalizedElement);
		
		Assert.assertNotNull(normalizedElement.attributeValue(attr1));
		Assert.assertEquals("1", normalizedElement.attributeValue(attr1));
		
		Assert.assertNotNull(normalizedElement.attributeValue(attr2));
		Assert.assertEquals("2", normalizedElement.attributeValue(attr2));
		
		Assert.assertNotNull(normalizedElement.element(ele1));
		Assert.assertNotNull(normalizedElement.element(ele1).element("name"));
		Assert.assertEquals("B", normalizedElement.element(ele1).element("name").getText());
		
		Assert.assertNotNull(normalizedElement.element(ele2));
		Assert.assertNotNull(normalizedElement.element(ele2).element("name"));
		Assert.assertEquals("C", normalizedElement.element(ele2).element("name").getText());
	}
	
	@Test
	public void shouldUpdateNotEffectBecauseElementIsNull(){
		XMLView view = new XMLView();
		
		Element element = DocumentHelper.createElement("Foo");
		view.update(null, element);
	}
	
	@Test
	public void shouldUpdateNotEffectBecauseNewElementIsNull(){
		XMLView view = new XMLView();
		
		Element element = DocumentHelper.createElement("Foo");
		view.update(element, null);

		Assert.assertFalse(element.attributeIterator().hasNext());
		Assert.assertFalse(element.elementIterator().hasNext());

	}
	
	@Test
	public void shouldUpdateNotEffectBecauseElementHasInvalidType(){
		XMLView view = new XMLView();
		
		Element element = DocumentHelper.createElement("Foo");
		Element elementSource = DocumentHelper.createElement("Foolder");
		view.update(element, elementSource);
		
		Assert.assertFalse(element.attributeIterator().hasNext());
		Assert.assertFalse(element.elementIterator().hasNext());

	}
		
	@Test
	public void shouldUpdateNoEffectBecauseNotConfigurationWasDefined() throws DocumentException{
		XMLView view = new XMLView();
		
		String xml = "<Document xmlns:foo=\"http://foo.org\" myAttribute=\"2\" foo:fooAttribute=\"1\"><name>dummy</name><ExtendedData><MESH xmlns:sx=\"http://feedsync.org/2007/feedsync\" version=\"2096103467\"><sx:sync id=\"1\" updates=\"3\" deleted=\"false\" noconflicts=\"false\"><sx:history sequence=\"3\" when=\"2005-05-21T11:43:33Z\" by=\"JEO2000\"/><sx:history sequence=\"2\" when=\"2005-05-21T10:43:33Z\" by=\"REO1750\"/><sx:history sequence=\"1\" when=\"2005-05-21T09:43:33Z\" by=\"REO1750\"/></sx:sync></MESH></ExtendedData><Placemark foo:id=\"1\"><name>B</name></Placemark><bar:BAR xmlns:bar=\"http://bar.org\"><name>B</name></bar:BAR><BarFoo><name>C</name></BarFoo></Document>";
		Element elementSource = DocumentHelper.parseText(xml).getRootElement();

		Element element = DocumentHelper.createElement("Document");

		view.update(element, elementSource);		

		Assert.assertFalse(element.attributeIterator().hasNext());
		Assert.assertFalse(element.elementIterator().hasNext());
	}

	@Test
	public void shoudUpdateNoEffectBecauseNoElementsMatches() throws DocumentException{
		
		XMLViewElement elementView = new XMLViewElement("Document");
		elementView.addAttribute(DocumentHelper.createQName("fooAttribute", DocumentHelper.createNamespace("foo", "http:\\foo.org")));
		elementView.addAttribute("myAttribute");
		elementView.addElement(DocumentHelper.createQName("barAttribute", DocumentHelper.createNamespace("bar", "http:\\bar.org")));
		elementView.addElement("Bar");
		elementView.addElement("FooBar", "http:\\foobar.org");
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		"<kml xmlns=\"http://earth.google.com/kml/2.2\">"+
		"<Document xmlns:mesh4x=\"http://mesh4x.org/kml\">"+
		"<name>dummy</name>"+
	   	"<ExtendedData>"+
		"<mesh4x:sync xmlns:sx=\"http://feedsync.org/2007/feedsync\" version=\"2096103467\">"+
      	"<sx:sync id=\"1\" updates=\"3\" deleted=\"false\" noconflicts=\"false\">"+
      	"<sx:history sequence=\"3\" when=\"2005-05-21T11:43:33Z\" by=\"JEO2000\"/>"+
      	"<sx:history sequence=\"2\" when=\"2005-05-21T10:43:33Z\" by=\"REO1750\"/>"+
      	"<sx:history sequence=\"1\" when=\"2005-05-21T09:43:33Z\" by=\"REO1750\"/>"+
     	"</sx:sync>"+
		"</mesh4x:sync>"+
      	"</ExtendedData>"+
		"<Placemark mesh4x:id=\"1\">"+
		"<name>B</name>"+
		"</Placemark>"+
		"</Document>"+
		"</kml>";
		
		Element element = DocumentHelper.createElement("Document");
		Element elementSource = DocumentHelper.parseText(xml).getRootElement().element("Document");
		
		XMLView view = new XMLView(elementView);
		view.update(element, elementSource);
		
		Assert.assertFalse(element.attributeIterator().hasNext());
		Assert.assertFalse(element.elementIterator().hasNext());
	}
	
	@Test
	public void shoudUpdate() throws DocumentException{
		XMLViewElement elementView = new XMLViewElement("Document");
		
		QName attr1 = DocumentHelper.createQName("fooAttribute", DocumentHelper.createNamespace("foo", "http:\\foo.org"));
		elementView.addAttribute(attr1);
		
		String attr2 = "myAttribute";
		elementView.addAttribute(attr2);
		
		QName ele1 = DocumentHelper.createQName("BAR", DocumentHelper.createNamespace("bar", "http:\\bar.org"));
		elementView.addElement(ele1);
		
		String ele2 = "BarFoo";
		elementView.addElement(ele2);
		
		//view.addElement("FooBar", "http:\\foobar.org");
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		"<Document xmlns:foo=\"http:\\foo.org\" xmlns:bar=\"http:\\bar.org\" xmlns:fooBar=\"http:\\foobar.org\" myAttribute=\"2\" foo:fooAttribute=\"1\">"+
		"<name>dummy</name>"+
	   	"<ExtendedData>"+
		"<MESH xmlns:sx=\"http://feedsync.org/2007/feedsync\" version=\"2096103467\">"+
      	"<sx:sync id=\"1\" updates=\"3\" deleted=\"false\" noconflicts=\"false\">"+
      	"<sx:history sequence=\"3\" when=\"2005-05-21T11:43:33Z\" by=\"JEO2000\"/>"+
      	"<sx:history sequence=\"2\" when=\"2005-05-21T10:43:33Z\" by=\"REO1750\"/>"+
      	"<sx:history sequence=\"1\" when=\"2005-05-21T09:43:33Z\" by=\"REO1750\"/>"+
     	"</sx:sync>"+
		"</MESH>"+
      	"</ExtendedData>"+
		"<Placemark foo:id=\"1\">"+
		"<name>B</name>"+
		"</Placemark>"+
		"<bar:BAR>"+
		"<name>B</name>"+
		"</bar:BAR>"+
		"<BarFoo>"+
		"<name>C</name>"+
		"</BarFoo>"+
		"</Document>";
		
		Element element = DocumentHelper.createElement("Document");
		Element elementSource = DocumentHelper.parseText(xml).getRootElement();
		
		XMLView view = new XMLView(elementView);
		view.update(element, elementSource);
		
		Assert.assertNotNull(element.attributeValue(attr1));
		Assert.assertEquals("1", element.attributeValue(attr1));
		
		Assert.assertNotNull(element.attributeValue(attr2));
		Assert.assertEquals("2", element.attributeValue(attr2));
		
		Assert.assertNotNull(element.element(ele1));
		Assert.assertNotNull(element.element(ele1).element("name"));
		Assert.assertEquals("B", element.element(ele1).element("name").getText());
		
		Assert.assertNotNull(element.element(ele2));
		Assert.assertNotNull(element.element(ele2).element("name"));
		Assert.assertEquals("C", element.element(ele2).element("name").getText());

	}
}
