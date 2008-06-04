package com.mesh4j.sync.adapters.kml;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Assert;
import org.junit.Test;

import com.mesh4j.sync.adapters.dom.DOMLoader;
import com.mesh4j.sync.adapters.dom.IMeshDOM;
import com.mesh4j.sync.adapters.dom.MeshNames;
import com.mesh4j.sync.security.NullIdentityProvider;
import com.mesh4j.sync.test.utils.TestHelper;
import com.mesh4j.sync.utils.IdGenerator;
import com.mesh4j.sync.utils.XMLHelper;
import com.mesh4j.sync.validations.MeshException;

public class KMLDOMLoaderTests {

	@Test(expected=IllegalArgumentException.class)
	public void shouldNotAccetpNullFileName(){
		new KMLDOMLoader(null, NullIdentityProvider.INSTANCE, DOMLoaderFactory.createKMLView());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotAccetpEmptyFileName(){
		new KMLDOMLoader("", NullIdentityProvider.INSTANCE, DOMLoaderFactory.createKMLView());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotAccetpInvalidExtension(){
		new KMLDOMLoader("a.kmz", NullIdentityProvider.INSTANCE, DOMLoaderFactory.createKMLView());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotAccetpNullIdentityProvider(){
		new KMLDOMLoader("a.kml", null, DOMLoaderFactory.createKMLView());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotAccetpNullXMLView(){
		new KMLDOMLoader("a.kml", NullIdentityProvider.INSTANCE, null);
	}
	
	@Test(expected=MeshException.class)
	public void shouldReadThrowsExceptionBecauseFileHasInvalidContent(){
		String fileName = this.getClass().getResource("templateWithInvalidXML.kml").getFile();
		 
		KMLDOMLoader loader = new KMLDOMLoader(fileName, NullIdentityProvider.INSTANCE, DOMLoaderFactory.createKMLView());
		loader.read();
	}
	
	@Test
	public void shouldReturnFriendlyName(){
		String fileName = this.getClass().getResource("templateWithInvalidXML.kml").getFile();
		 
		KMLDOMLoader loader = new KMLDOMLoader(fileName, NullIdentityProvider.INSTANCE, DOMLoaderFactory.createKMLView());
		
		String name = loader.getFriendlyName();
		Assert.assertNotNull(name);
		Assert.assertTrue(name.trim().length() > 0);
	}
	
	@Test
	public void shouldReadDoNotCreateFile(){
		String fileName = TestHelper.fileName(IdGenerator.newID()+".kml");
		 
		KMLDOMLoader loader = new KMLDOMLoader(fileName, NullIdentityProvider.INSTANCE, DOMLoaderFactory.createKMLView());
		loader.read();
		Assert.assertNotNull(loader.getDOM());
		
		File file = new File(fileName);
		Assert.assertFalse(file.exists());
	}
	
	@Test
	public void shouldRead() throws DocumentException{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		"<kml xmlns=\"http://earth.google.com/kml/2.2\">"+
		"<Document>"+
		"<name>dummy</name>"+
	   	"<ExtendedData xmlns:mesh4x=\"http://mesh4x.org/kml\" >"+
		"<mesh4x:sync xmlns:sx=\"http://feedsync.org/2007/feedsync\" version=\"1547376435\">"+
      	"<sx:sync id=\"1\" updates=\"3\" deleted=\"false\" noconflicts=\"false\">"+
      	"<sx:history sequence=\"3\" when=\"2005-05-21T11:43:33Z\" by=\"JEO2000\"/>"+
      	"<sx:history sequence=\"2\" when=\"2005-05-21T10:43:33Z\" by=\"REO1750\"/>"+
      	"<sx:history sequence=\"1\" when=\"2005-05-21T09:43:33Z\" by=\"REO1750\"/>"+
     	"</sx:sync>"+
		"</mesh4x:sync>"+
		"<mesh4x:sync xmlns:sx=\"http://feedsync.org/2007/feedsync\" version=\"825956491\">"+
      	"<sx:sync id=\"10\" updates=\"1\" deleted=\"false\" noconflicts=\"false\">"+
      	"<sx:history sequence=\"1\" when=\"2005-05-21T09:43:33Z\" by=\"REO1750\"/>"+
     	"</sx:sync>"+
		"</mesh4x:sync>"+
		"<mesh4x:hierarchy xml:id=\"10\" mesh4x:childId=\"1\"/>"+
      	"</ExtendedData>"+
		"<Placemark xml:id=\"1\">"+
		"<name>B</name>"+
		"</Placemark>"+
		"</Document>"+
		"</kml>";
		
		File file = TestHelper.makeNewXMLFile(xml, ".kml");
		Assert.assertTrue(file.exists());
		
		KMLDOMLoader loader = new KMLDOMLoader(file.getAbsolutePath(), NullIdentityProvider.INSTANCE, DOMLoaderFactory.createKMLView());
		loader.read();
		
		Assert.assertNotNull(loader.getDOM());
		
		Document doc = DocumentHelper.parseText(xml);
		doc.normalize();
		Assert.assertEquals(doc.asXML(), loader.getDOM().asXML());
	}

	@Test
	public void shouldReadDocWithExternalChanges() throws DocumentException{
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		"<kml xmlns=\"http://earth.google.com/kml/2.2\">"+
		"<Document>"+
		"<name>dummy</name>"+
	   	"<ExtendedData xmlns:mesh4x=\"http://mesh4x.org/kml\">"+
		"<mesh4x:sync xmlns:sx=\"http://feedsync.org/2007/feedsync\" version=\"1\">"+
      	"<sx:sync id=\"1\" updates=\"3\" deleted=\"false\" noconflicts=\"false\">"+
      	"<sx:history sequence=\"3\" when=\"2005-05-21T11:43:33Z\" by=\"JEO2000\"/>"+
      	"<sx:history sequence=\"2\" when=\"2005-05-21T10:43:33Z\" by=\"REO1750\"/>"+
      	"<sx:history sequence=\"1\" when=\"2005-05-21T09:43:33Z\" by=\"REO1750\"/>"+
     	"</sx:sync>"+
		"</mesh4x:sync>"+
      	"</ExtendedData>"+
		"<Placemark xml:id=\"1\">"+
		"<name>B</name>"+
		"</Placemark>"+
		"</Document>"+
		"</kml>";
		
		File file = TestHelper.makeNewXMLFile(xml, ".kml");
		Assert.assertTrue(file.exists());
		
		KMLDOMLoader loader = new KMLDOMLoader(file.getAbsolutePath(), NullIdentityProvider.INSTANCE, DOMLoaderFactory.createKMLView());
		loader.read();
		
		Assert.assertNotNull(loader.getDOM());
		
		Document doc = DocumentHelper.parseText(xml);
		doc.normalize();
		Assert.assertFalse(doc.asXML().equals(loader.getDOM().asXML()));
	}
	
	@Test
	public void shouldWriteCreateFile() throws DocumentException{

		String fileName = TestHelper.fileName(IdGenerator.newID()+".kml");
		File file = new File(fileName);
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://earth.google.com/kml/2.2\"><Document><name>"
					+file.getName()+"</name><ExtendedData xmlns:mesh4x=\"http://mesh4x.org/kml\"></ExtendedData></Document></kml>";
				 
		KMLDOMLoader loader = new KMLDOMLoader(fileName, NullIdentityProvider.INSTANCE, DOMLoaderFactory.createKMLView());
		loader.read();
		Assert.assertNotNull(loader.getDOM());
		
		Assert.assertFalse(file.exists());
		
		loader.write();
		Assert.assertTrue(file.exists());
		
		Assert.assertNotNull(loader.getDOM());
		
		Document doc = DocumentHelper.parseText(xml);
		doc.normalize();
		Assert.assertEquals(doc.asXML(), loader.getDOM().asXML());		
	}
	
	@Test
	public void shouldWrite() throws DocumentException{
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		"<kml xmlns=\"http://earth.google.com/kml/2.2\">"+
		"<Document>"+
		"<name>dummy</name>"+
	   	"<ExtendedData xmlns:mesh4x=\"http://mesh4x.org/kml\">"+
		"<mesh4x:sync xmlns:sx=\"http://feedsync.org/2007/feedsync\" version=\"1\">"+
      	"<sx:sync id=\"1\" updates=\"3\" deleted=\"false\" noconflicts=\"false\">"+
      	"<sx:history sequence=\"3\" when=\"2005-05-21T11:43:33Z\" by=\"JEO2000\"/>"+
      	"<sx:history sequence=\"2\" when=\"2005-05-21T10:43:33Z\" by=\"REO1750\"/>"+
      	"<sx:history sequence=\"1\" when=\"2005-05-21T09:43:33Z\" by=\"REO1750\"/>"+
     	"</sx:sync>"+
		"</mesh4x:sync>"+
      	"</ExtendedData>"+
		"<Placemark xml:id=\"1\">"+
		"<name>B</name>"+
		"</Placemark>"+
		"</Document>"+
		"</kml>";
		
		File file = TestHelper.makeNewXMLFile(xml, ".kml");
		Assert.assertTrue(file.exists());
		
		KMLDOMLoader loader = new KMLDOMLoader(file.getAbsolutePath(), NullIdentityProvider.INSTANCE, DOMLoaderFactory.createKMLView());
		loader.read();
		
		Assert.assertNotNull(loader.getDOM());
		
		Document doc = DocumentHelper.parseText(xml);
		doc.normalize();
		Assert.assertFalse(doc.asXML().equals(loader.getDOM().asXML()));
		
		doc = XMLHelper.readDocument(file);
		doc.normalize();
		Assert.assertFalse(doc.asXML().equals(loader.getDOM().asXML()));
		
		loader.getDOM().normalize();
		loader.write();
		
		doc = XMLHelper.readDocument(file);
		doc.normalize();
		
		Assert.assertEquals(doc.asXML(), loader.getDOM().asXML());

	}
	
	private static final String kmlAsXML = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
		"<kml xmlns=\"http://earth.google.com/kml/2.2\">"+
		"<Document>"+
		"	<name>dummy</name>"+
		"</Document>"+
		"</kml>";
	
	//prepateSyncRepository(Element)
	@Test
	public void shouldInitializeSyncRepository() throws DocumentException{
		
		Document kmlDocument = DocumentHelper.parseText(kmlAsXML);
		
		Element documentElement = kmlDocument.getRootElement().element(KmlNames.KML_ELEMENT_DOCUMENT);
		Assert.assertNotNull(documentElement);
		Element extendedData = documentElement.element(KmlNames.KML_ELEMENT_EXTENDED_DATA);
		Assert.assertNull(extendedData);
		
		new MockLoader(kmlDocument).read();
		
		
		extendedData = documentElement.element(KmlNames.KML_ELEMENT_EXTENDED_DATA);
		Assert.assertNotNull(extendedData);
		Assert.assertNotNull(extendedData.getNamespaceForPrefix(MeshNames.MESH_PREFIX));
	}
	
	@SuppressWarnings("unused")
	private class MockLoader extends DOMLoader{

		private Document document;
		
		public MockLoader(Document doc){
			super("a.kmj", NullIdentityProvider.INSTANCE, DOMLoaderFactory.createKMLView());
			this.document = doc;
		}
				
		@Override
		protected IMeshDOM createDocument(String name) {
			return new KMLDOM(document, getIdentityProvider(), getXMLView());
		}

		@Override
		protected void flush() {
			
		}

		@Override
		protected IMeshDOM load() {
			return new KMLDOM(document, getIdentityProvider(), getXMLView());
		}

		@Override
		public String getFriendlyName() {
			return "Mock";
		}	
	}


}
