package org.mesh4j.sync.payload.schema.rdf;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Assert;
import org.junit.Test;
import org.mesh4j.sync.payload.schema.ISchema;
import org.mesh4j.sync.payload.schema.ISchemaTypeFormat;
import org.mesh4j.sync.utils.DateHelper;
import org.mesh4j.sync.utils.XMLHelper;
import org.mesh4j.sync.validations.MeshException;


public class RDFSchemaTests {

	@Test(expected=IllegalArgumentException.class)
	public void shouldCreateSchemaFromReaderFailsIfReaderIsNull(){
		new RDFSchema(null);
	}
	
	@Test(expected=MeshException.class)
	public void shouldCreateSchemaFromReaderFailsIfXMLIsNotValidXML(){
		Reader reader = new StringReader("abc");
		new RDFSchema(reader);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldCreateSchemaFromReaderFailsIfXMLIsInvalidRDF(){
		Reader reader = new StringReader("<foo>bar</foo>");
		new RDFSchema(reader);
	}
	
	@Test
	public void shouldCreateSchemaFromReader() throws FileNotFoundException{
		Reader reader = new FileReader(this.getClass().getResource("oswego.owl").getFile());
		RDFSchema schema = new RDFSchema(reader);
		Assert.assertNotNull(schema);
		
		Assert.assertEquals(6, schema.getPropertyCount());
		
		Assert.assertEquals(IRDFSchema.XLS_STRING, schema.getPropertyType("Code"));
		Assert.assertEquals("Code", schema.getPropertyLabel("Code", IRDFSchema.DEFAULT_LANGUAGE));

		Assert.assertEquals(IRDFSchema.XLS_STRING, schema.getPropertyType("Name"));
		Assert.assertEquals("Name", schema.getPropertyLabel("Name", IRDFSchema.DEFAULT_LANGUAGE));

		Assert.assertEquals(IRDFSchema.XLS_DATETIME, schema.getPropertyType("DateOnset"));
		Assert.assertEquals("DateOnset", schema.getPropertyLabel("DateOnset", IRDFSchema.DEFAULT_LANGUAGE));

		Assert.assertEquals(IRDFSchema.XLS_BOOLEAN, schema.getPropertyType("ILL"));
		Assert.assertEquals("ILL", schema.getPropertyLabel("ILL", IRDFSchema.DEFAULT_LANGUAGE));
		
		Assert.assertEquals(IRDFSchema.XLS_DOUBLE, schema.getPropertyType("AGE"));
		Assert.assertEquals("AGE", schema.getPropertyLabel("AGE", IRDFSchema.DEFAULT_LANGUAGE));

		Assert.assertEquals(IRDFSchema.XLS_INTEGER, schema.getPropertyType("RecStatus"));
		Assert.assertEquals("RecStatus", schema.getPropertyLabel("RecStatus", IRDFSchema.DEFAULT_LANGUAGE));

		Assert.assertEquals("Oswego", schema.getOntologyNameSpace());
		Assert.assertEquals("http://localhost:8080/mesh4x/feeds/Epiinfo/Oswego#", schema.getOntologyBaseClassUri());
		Assert.assertEquals("Oswego", schema.getOntologyClassName());
	}
		
	@Test
	public void shouldCreateSchemaFailsIfAnyParameterIsEmptyOrNull(){
		try{
			new RDFSchema(null, "http://localhost:8080/mesh4x#className", "className");
			Assert.fail();
		} catch(IllegalArgumentException e){
			// ok test
		}
		try{
			new RDFSchema("", "http://localhost:8080/mesh4x#className", "className");
			Assert.fail();
		} catch(IllegalArgumentException e){
			// ok test
		}
		try{
			new RDFSchema("ns", null, "className");
			Assert.fail();
		} catch(IllegalArgumentException e){
			// ok test
		}
		try{
			new RDFSchema("ns", "", "className");
			Assert.fail();
		} catch(IllegalArgumentException e){
			// ok test
		}
		try{
			new RDFSchema("ns", "http://localhost:8080/mesh4x#className", null);
			Assert.fail();
		} catch(IllegalArgumentException e){
			// ok test
		}
		try{
			new RDFSchema("ns", "http://localhost:8080/mesh4x#className", "");
			Assert.fail();
		} catch(IllegalArgumentException e){
			// ok test
		}
		
	}
	
	@Test
	public void shouldCreateSchema(){
		RDFSchema schema = new RDFSchema("Oswego", "http://localhost:8080/mesh4x/feeds/Epiinfo/Oswego#", "Oswego");
		Assert.assertNotNull(schema);
		Assert.assertEquals(0, schema.getPropertyCount());
		Assert.assertEquals("Oswego", schema.getOntologyNameSpace());
		Assert.assertEquals("http://localhost:8080/mesh4x/feeds/Epiinfo/Oswego#", schema.getOntologyBaseClassUri());
		Assert.assertEquals("Oswego", schema.getOntologyClassName());
	}

	@Test
	public void shouldGetBaseRDFURL(){
		RDFSchema schema = new RDFSchema("Oswego", "http://localhost:8080/mesh4x/feeds/Epiinfo/Oswego#", "Oswego");
		Assert.assertEquals("http://localhost:8080/mesh4x/feeds/Epiinfo", schema.getOntologyBaseRDFUrl());
	}
	
	@Test
	public void shouldGetPropertyTypeReturnsNullIfPropertyDoesNotExists() throws FileNotFoundException{
		Reader reader = new FileReader(this.getClass().getResource("oswego.owl").getFile());
		RDFSchema schema = new RDFSchema(reader);;
		Assert.assertNotNull(schema);
		Assert.assertNull(schema.getPropertyType("undefined"));
	}
	
	@Test
	public void shouldGetPropertyType() throws FileNotFoundException{
		Reader reader = new FileReader(this.getClass().getResource("oswego.owl").getFile());
		RDFSchema schema = new RDFSchema(reader);
		Assert.assertNotNull(schema);
		Assert.assertEquals(IRDFSchema.XLS_STRING, schema.getPropertyType("Name"));
	}
	
	@Test
	public void shouldGetPropertyLabelReturnsNullIfPropertyDoesNotExists() throws FileNotFoundException{
		Reader reader = new FileReader(this.getClass().getResource("oswego.owl").getFile());
		RDFSchema schema = new RDFSchema(reader);
		Assert.assertNotNull(schema);
		Assert.assertNull(schema.getPropertyLabel("undefined", IRDFSchema.DEFAULT_LANGUAGE));
	}
	
	@Test
	public void shouldGetPropertyLabelReturnsNullIfLangDoesNotExists() throws FileNotFoundException{
		Reader reader = new FileReader(this.getClass().getResource("oswego.owl").getFile());
		RDFSchema schema = new RDFSchema(reader);
		Assert.assertNotNull(schema);
		Assert.assertNull(schema.getPropertyLabel("Name", "sp"));
	}
	
	@Test
	public void shouldGetPropertyLabel() throws FileNotFoundException{
		Reader reader = new FileReader(this.getClass().getResource("oswego.owl").getFile());
		RDFSchema schema = new RDFSchema(reader);
		Assert.assertNotNull(schema);
		Assert.assertEquals("Name", schema.getPropertyLabel("Name", IRDFSchema.DEFAULT_LANGUAGE));
		Assert.assertEquals("Name", schema.getPropertyLabel("Name"));
	}

	@Test
	public void shouldCannonicaliseValueReturnsNullIfPropertyDoesNotExists() throws FileNotFoundException{
		Reader reader = new FileReader(this.getClass().getResource("oswego.owl").getFile());
		RDFSchema schema = new RDFSchema(reader);
		Assert.assertNotNull(schema);
		Assert.assertNull(schema.cannonicaliseValue("undefined", "123"));
	}
	
	@Test
	public void shouldCannonicaliseValue() throws FileNotFoundException{
		Reader reader = new FileReader(this.getClass().getResource("oswego.owl").getFile());
		RDFSchema schema = new RDFSchema(reader);
		Assert.assertNotNull(schema);
		Assert.assertEquals(new Long(Integer.MIN_VALUE -1), schema.cannonicaliseValue("RecStatus", Integer.MIN_VALUE -1));
	}
	
	@Test
	public void shouldGetPropertyNameReturnsNullIfSchemaHasNotProperties(){
		RDFSchema schema = new RDFSchema("Oswego", "http://localhost:8080/mesh4x/feeds/Epiinfo/Oswego#", "Oswego");
		Assert.assertNotNull(schema);
		Assert.assertEquals(0, schema.getPropertyCount());
		Assert.assertNull(schema.getPropertyName(3));
	}
	
	@Test
	public void shouldGetPropertyNameReturnsNullIfIndexIsOutOfRange(){
		RDFSchema schema = new RDFSchema("Oswego", "http://localhost:8080/mesh4x/feeds/Epiinfo/Oswego#", "Oswego");
		Assert.assertNotNull(schema);
		schema.addStringProperty("name", "name", IRDFSchema.DEFAULT_LANGUAGE);
		Assert.assertEquals(1, schema.getPropertyCount());
		
		Assert.assertNull(schema.getPropertyName(1));
	}
	
	@Test
	public void shouldGetPropertyName(){
		RDFSchema schema = new RDFSchema("Oswego", "http://localhost:8080/mesh4x/feeds/Epiinfo/Oswego#", "Oswego");
		Assert.assertNotNull(schema);
		schema.addStringProperty("name", "name", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addStringProperty("code", "code", IRDFSchema.DEFAULT_LANGUAGE);
		Assert.assertEquals(2, schema.getPropertyCount());
		
		Assert.assertEquals("name", schema.getPropertyName(1));
		Assert.assertEquals("code", schema.getPropertyName(0));
	}
	
	@Test
	public void shouldGetInstanceFromRDFXMLReturnsNullIfElementHasNotRDFNode(){
		Element element = DocumentHelper.createElement("foo");
		RDFSchema schema = new RDFSchema("Oswego", "http://localhost:8080/mesh4x/feeds/Epiinfo/Oswego#", "Oswego");
		Assert.assertNull(schema.getInstanceFromXML(element));
	}
	
	@Test
	public void shouldGetInstanceFromRDFXMLRootElement(){
		Element element = DocumentHelper.createElement(IRDFSchema.QNAME_RDF);
		RDFSchema schema = new RDFSchema("Oswego", "http://localhost:8080/mesh4x/feeds/Epiinfo/Oswego#", "Oswego");
		Element rdfElement = schema.getInstanceFromXML(element);
		Assert.assertEquals(element.asXML(), rdfElement.asXML());
	}
	
	@Test
	public void shouldGetInstanceFromRDFXMLSubElement(){
		Element element = DocumentHelper.createElement("foo");
		Element elementSub = element.addElement(IRDFSchema.QNAME_RDF);
		RDFSchema schema = new RDFSchema("Oswego", "http://localhost:8080/mesh4x/feeds/Epiinfo/Oswego#", "Oswego");
		Element rdfElement = schema.getInstanceFromXML(element);
		Assert.assertEquals(elementSub.asXML(), rdfElement.asXML());
	}
	
	@Test
	public void shouldAsInstancePlainXMLReturnsNullIfElementHasNotRDFNode(){
		Element element = DocumentHelper.createElement("foo");
		RDFSchema schema = new RDFSchema("Oswego", "http://localhost:8080/mesh4x/feeds/Epiinfo/Oswego#", "Oswego");
		Assert.assertNull(schema.asInstancePlainXML(element, ISchema.EMPTY_FORMATS));
	}
	
	@Test
	public void shouldAsInstancePlainXMLRootElement(){
        String rdfXml = "<rdf:RDF xmlns:example=\"http://mesh4x/example#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">"+
		"<example:example rdf:about=\"uri:urn:1\">"+
		"<example:string rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">abc</example:string>"+
		"<example:integer rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">2147483647</example:integer>"+
		"<example:boolean rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</example:boolean>"+
		"<example:datetime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2009-06-01T05:31:01.001Z</example:datetime>"+
		"<example:double rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">1.7976931348623157E308</example:double>"+
		"<example:long rdf:datatype=\"http://www.w3.org/2001/XMLSchema#long\">9223372036854775807</example:long>"+
		"<example:decimal rdf:datatype=\"http://www.w3.org/2001/XMLSchema#decimal\">10</example:decimal>"+
		"</example:example>"+
		"</rdf:RDF>";
        
        Element element = XMLHelper.parseElement(rdfXml);
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
        
		Element plainElement = schema.asInstancePlainXML(element, ISchema.EMPTY_FORMATS);
		Assert.assertEquals("<example>" +
				"<boolean>true</boolean>" +
				"<datetime>2009-06-01T05:31:01.001Z</datetime>" +
				"<decimal>10</decimal>" +
				"<double>1.7976931348623157E308</double>" +
				"<integer>2147483647</integer>" +
				"<long>9223372036854775807</long>" +
				"<string>abc</string>" +				
				"</example>", plainElement.asXML());
	}
	
	@Test
	public void shouldAsInstancePlainXMLSubElement(){
		Element element = DocumentHelper.createElement("foo");
		String rdfXml = "<rdf:RDF xmlns:example=\"http://mesh4x/example#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">"+
			"<example:example rdf:about=\"uri:urn:1\">"+
			"<example:string rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">abc</example:string>"+
			"<example:integer rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">2147483647</example:integer>"+
			"<example:boolean rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</example:boolean>"+
			"<example:datetime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2009-06-01T05:31:01.001Z</example:datetime>"+
			"<example:double rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">1.7976931348623157E308</example:double>"+
			"<example:long rdf:datatype=\"http://www.w3.org/2001/XMLSchema#long\">9223372036854775807</example:long>"+
			"<example:decimal rdf:datatype=\"http://www.w3.org/2001/XMLSchema#decimal\">10</example:decimal>"+
			"</example:example>"+
			"</rdf:RDF>";
	        
	    Element elementSub = XMLHelper.parseElement(rdfXml);
	    element.add(elementSub);
	    
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
	        
		Element plainElement = schema.asInstancePlainXML(element, ISchema.EMPTY_FORMATS);
		Assert.assertEquals(
			"<example>" +
			"<boolean>true</boolean>" +
			"<datetime>2009-06-01T05:31:01.001Z</datetime>" +
			"<decimal>10</decimal>" +
			"<double>1.7976931348623157E308</double>" +
			"<integer>2147483647</integer>" +
			"<long>9223372036854775807</long>" +
			"<string>abc</string>" +
			"</example>", 
			plainElement.asXML());

	}
	

	@Test
	public void shouldGetInstanceFromPlainXML(){
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
	       
		String xml = "<example><decimal>10.8</decimal><long>9223372036854775807</long><double>1.7976931348623157E308</double><datetime>2009-06-01T05:31:01.001Z</datetime><boolean>true</boolean><integer>2147483647</integer><string>abc</string></example>";
		Element element = XMLHelper.parseElement(xml);
		Element rdfElement = schema.getInstanceFromPlainXML("1", element, ISchema.EMPTY_FORMATS);
		
		String rdfXml = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:example=\"http://mesh4x/example#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">"+
		"<example:example rdf:about=\"uri:urn:1\">"+
		"<example:string rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">abc</example:string>"+
		"<example:integer rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">2147483647</example:integer>"+
		"<example:boolean rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</example:boolean>"+
		"<example:datetime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2009-06-01T05:31:01.001Z</example:datetime>"+
		"<example:double rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">1.7976931348623157E308</example:double>"+
		"<example:long rdf:datatype=\"http://www.w3.org/2001/XMLSchema#long\">9223372036854775807</example:long>"+
		"<example:decimal rdf:datatype=\"http://www.w3.org/2001/XMLSchema#decimal\">10.8</example:decimal>"+
		"</example:example>"+
		"</rdf:RDF>";
		Element rdfExpected = XMLHelper.parseElement(rdfXml);
		
		Assert.assertEquals(XMLHelper.canonicalizeXML(rdfExpected), XMLHelper.canonicalizeXML(rdfElement));
	}
	
	@Test
	public void shouldGetInstanceFromProperties(){
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		HashMap<String, Object> propertyValues = new HashMap<String, Object>();
		propertyValues.put("decimal", 10d);
		propertyValues.put("long", 9223372036854775807l);
		propertyValues.put("double", 1.7976931348623157E308d);
		propertyValues.put("datetime", DateHelper.parseW3CDateTime("2009-06-01T05:31:01.001Z"));
		propertyValues.put("boolean", true);
		propertyValues.put("integer", 2147483647);
		propertyValues.put("string", "abc");
		
		Element rdfElement = XMLHelper.parseElement(schema.createNewInstanceFromProperties("1", propertyValues).asRDFXML());
		
		String rdfXml = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:example=\"http://mesh4x/example#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">"+
		"<example:example rdf:about=\"uri:urn:1\">"+
		"<example:string rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">abc</example:string>"+
		"<example:integer rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">2147483647</example:integer>"+
		"<example:boolean rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</example:boolean>"+
		"<example:datetime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2009-06-01T05:31:01.001Z</example:datetime>"+
		"<example:double rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">1.7976931348623157E308</example:double>"+
		"<example:long rdf:datatype=\"http://www.w3.org/2001/XMLSchema#long\">9223372036854775807</example:long>"+
		"<example:decimal rdf:datatype=\"http://www.w3.org/2001/XMLSchema#decimal\">10</example:decimal>"+
		"</example:example>"+
		"</rdf:RDF>";
		Element rdfExpected = XMLHelper.parseElement(rdfXml);
		
		Assert.assertEquals(XMLHelper.canonicalizeXML(rdfExpected), XMLHelper.canonicalizeXML(rdfElement));
	}
	
	@Test
	public void shouldGetPropertiesAsMap(){
		
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		String rdfXml = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:example=\"http://mesh4x/example#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">"+
		"<example:example rdf:about=\"uri:urn:1\">"+
		"<example:string rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">abc</example:string>"+
		"<example:integer rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">2147483647</example:integer>"+
		"<example:boolean rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</example:boolean>"+
		"<example:datetime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2009-06-01T05:31:01.001Z</example:datetime>"+
		"<example:double rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">1.7976931348623157E308</example:double>"+
		"<example:long rdf:datatype=\"http://www.w3.org/2001/XMLSchema#long\">9223372036854775807</example:long>"+
		"<example:decimal rdf:datatype=\"http://www.w3.org/2001/XMLSchema#decimal\">10</example:decimal>"+
		"</example:example>"+
		"</rdf:RDF>";
		
		Element rdfElement = XMLHelper.parseElement(rdfXml);
		
		Map<String, String> context = schema.getPropertiesAsLexicalFormMap(rdfElement);
		
		Assert.assertEquals("10", context.get("decimal"));
		Assert.assertEquals("9223372036854775807", context.get("long"));
		Assert.assertEquals("1.7976931348623157E308", context.get("double"));
		Assert.assertEquals("2009-06-01T05:31:01.001Z", context.get("datetime"));
		Assert.assertEquals("true", context.get("boolean"));
		Assert.assertEquals("2147483647", context.get("integer"));
		Assert.assertEquals("abc", context.get("string"));
	}

	@Test
	public void shouldIsCompatibleReturnsTrueIfParameterSchemaIsSameInstance(){
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		Assert.assertTrue(schema.isCompatible(schema));
	}
	
	@Test
	public void shouldIsCompatibleReturnsFalseIfParameterSchemaIsNull(){
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		Assert.assertFalse(schema.isCompatible(null));
	}

	@Test
	public void shouldIsCompatibleReturnsFalseIfParameterSchemaIsNotRDFSchemaInstance(){
		ISchema mockSchema = new ISchema(){
			@Override public Element asInstancePlainXML(Element element, Map<String, ISchemaTypeFormat> typeFormats) {return null;}
			@Override public String asXML() {return null;}
			@Override public Element getInstanceFromPlainXML(String id, Element element, Map<String, ISchemaTypeFormat> typeFormats) {return null;}
			@Override public Element getInstanceFromXML(Element element) {return null;}
			@Override public Map<String, String> getPropertiesAsLexicalFormMap(Element element) {return null;}
			@Override public Map<String, Object> getPropertiesAsMap(Element element) {return null;}
			@Override public boolean isCompatible(ISchema schema) {return false;}
			@Override public String getName() {return null;}
			@Override public Element asInstanceXML(Element element, HashMap<String, ISchemaTypeFormat> typeFormats) {return null;}			
		};
		
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		Assert.assertFalse(schema.isCompatible(mockSchema));
	}
	
	@Test
	public void shouldIsCompatibleReturnsTrueIfParameterSchemaHasSameRDFXml(){
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		RDFSchema otherSchema = new RDFSchema("example", "http://mesh4x/example#", "example");
		otherSchema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		Assert.assertEquals(schema.asXML(), otherSchema.asXML());
		Assert.assertTrue(schema.isCompatible(otherSchema));
	}
	
	@Test
	public void shouldIsCompatibleReturnsTrueIfParameterSchemaHasSameProperties(){
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		RDFSchema otherSchema = new RDFSchema("example", "http://mesh4x/example#", "example");
		otherSchema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		
		Assert.assertEquals(schema.asXML(), otherSchema.asXML());
		Assert.assertTrue(schema.isCompatible(otherSchema));
	}
	
	@Test
	public void shouldIsCompatibleReturnsFalseIfParameterSchemaHasNotSamePropertiesCount(){
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		RDFSchema otherSchema = new RDFSchema("example", "http://mesh4x/example#", "example");
		otherSchema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		
		Assert.assertFalse(schema.isCompatible(otherSchema));
	}
	
	@Test
	public void shouldIsCompatibleReturnsFalseIfParameterSchemaHasNotSamePropertyNames(){
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		RDFSchema otherSchema = new RDFSchema("example", "http://mesh4x/example#", "example");
		otherSchema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDoubleProperty("double2", "double", IRDFSchema.DEFAULT_LANGUAGE);
		
		Assert.assertFalse(schema.isCompatible(otherSchema));
	}
	
	@Test
	public void shouldIsCompatibleReturnsFalseIfParameterSchemaHasNotSamePropertyTypes(){
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		RDFSchema otherSchema = new RDFSchema("example", "http://mesh4x/example#", "example");
		otherSchema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addStringProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		
		Assert.assertFalse(schema.isCompatible(otherSchema));
	}
	
	@Test
	public void shouldIsCompatibleReturnsTrueIfParameterSchemaHasCompatiblePropertyTypes(){
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		RDFSchema otherSchema = new RDFSchema("example", "http://mesh4x/example#", "example");
		otherSchema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDoubleProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addLongProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addDecimalProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		otherSchema.addIntegerProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		Assert.assertFalse(schema.asXML().equals(otherSchema.asXML()));
		Assert.assertTrue(schema.isCompatible(otherSchema));
	}
	
	@Test
	public void shouldMarkIdentifiablesPropertyNames(){
		RDFSchema schema = new RDFSchema("example", "http://mesh4x/example#", "example");
		schema.addStringProperty("id1", "id1", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addStringProperty("id2", "id2", IRDFSchema.DEFAULT_LANGUAGE);
		schema.addStringProperty("name", "name", IRDFSchema.DEFAULT_LANGUAGE);

		ArrayList<String> pks = new ArrayList<String>();
		pks.add("id1");
		pks.add("id2");
		schema.setIdentifiablePropertyNames(pks);
		schema.setVersionPropertyName("datetime");
	
		List<String> pksLoaded = schema.getIdentifiablePropertyNames();
		Assert.assertEquals("id1", pksLoaded.get(0));
		Assert.assertEquals("id2", pksLoaded.get(1));
		
		
		String rdfXml = "<rdf:RDF xmlns:example=\"http://mesh4x/example#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n"+
		"  <owl:Class rdf:about=\"http://mesh4x/example#mesh4xMetadata\"></owl:Class>\n"+
		"  <owl:Class rdf:about=\"http://mesh4x/example#example\"></owl:Class>\n"+
		"  <owl:DatatypeProperty rdf:about=\"http://mesh4x/example#name\">\n"+
		"    <rdfs:label xml:lang=\"en\">name</rdfs:label>\n"+
		"    <rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#string\"></rdfs:range>\n"+
		"    <rdfs:domain rdf:resource=\"http://mesh4x/example#example\"></rdfs:domain>\n"+
		"  </owl:DatatypeProperty>\n"+
		"  <owl:DatatypeProperty rdf:about=\"http://mesh4x/example/mesh4xMetadata#versionProperty\">\n"+
		"    <rdfs:comment xml:lang=\"en\">datetime</rdfs:comment>\n"+
		"    <rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#string\"></rdfs:range>\n"+
		"    <rdfs:domain rdf:resource=\"http://mesh4x/example#mesh4xMetadata\"></rdfs:domain>\n"+
		"  </owl:DatatypeProperty>\n"+
		"  <owl:DatatypeProperty rdf:about=\"http://mesh4x/example#id1\">\n"+
		"    <rdfs:label xml:lang=\"en\">id1</rdfs:label>\n"+
		"    <rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#string\"></rdfs:range>\n"+
		"    <rdfs:domain rdf:resource=\"http://mesh4x/example#example\"></rdfs:domain>\n"+
		"  </owl:DatatypeProperty>\n"+
		"  <owl:DatatypeProperty rdf:about=\"http://mesh4x/example#id2\">\n"+
		"    <rdfs:label xml:lang=\"en\">id2</rdfs:label>\n"+
		"    <rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#string\"></rdfs:range>\n"+
		"    <rdfs:domain rdf:resource=\"http://mesh4x/example#example\"></rdfs:domain>\n"+
		"  </owl:DatatypeProperty>\n"+
		"  <owl:DatatypeProperty rdf:about=\"http://mesh4x/example/mesh4xMetadata#identifiableProperties\">\n"+
		"    <rdfs:comment xml:lang=\"en\">id1,id2</rdfs:comment>\n"+
		"    <rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#string\"></rdfs:range>\n"+
		"    <rdfs:domain rdf:resource=\"http://mesh4x/example#mesh4xMetadata\"></rdfs:domain>\n"+
		"  </owl:DatatypeProperty>\n"+
		"</rdf:RDF>";
		
		
		Assert.assertEquals(rdfXml, schema.asXML());
	
	}
	
	@Test
	public void shouldReadRDFSchemaWithIdentifiableProperties(){
		String serverUrl = "http://localhost:8080/mesh4x/feeds";
		String meshGroup = "meshGroup";
		String dataSetId = "dataSetId";

		String url = serverUrl+"/"+meshGroup+"/"+dataSetId;
		
		RDFSchema rdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		rdfSchema.addStringProperty("id1", "id1", IRDFSchema.DEFAULT_LANGUAGE);
		rdfSchema.addStringProperty("id2", "id2", IRDFSchema.DEFAULT_LANGUAGE);
		rdfSchema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		rdfSchema.addIntegerProperty("integer", "int", IRDFSchema.DEFAULT_LANGUAGE);
		rdfSchema.addBooleanProperty("boolean", "boolean", IRDFSchema.DEFAULT_LANGUAGE);
		rdfSchema.addDateTimeProperty("datetime", "datetime", IRDFSchema.DEFAULT_LANGUAGE);
		rdfSchema.addDoubleProperty("double", "double", IRDFSchema.DEFAULT_LANGUAGE);
		rdfSchema.addLongProperty("long", "long", IRDFSchema.DEFAULT_LANGUAGE);
		rdfSchema.addDecimalProperty("decimal", "decimal", IRDFSchema.DEFAULT_LANGUAGE);  
		
		ArrayList<String> pks = new ArrayList<String>();
		pks.add("id1");
		pks.add("id2");
		rdfSchema.setIdentifiablePropertyNames(pks);
		rdfSchema.setVersionPropertyName("datetime");
		RDFSchema schema = new RDFSchema(new StringReader(rdfSchema.asXML()));
		
		Assert.assertNotNull(schema);
		Assert.assertTrue(rdfSchema.isCompatible(schema));
		
		Assert.assertEquals("id1", ((RDFSchema)schema).getIdentifiablePropertyNames().get(0));
		Assert.assertEquals("id2", ((RDFSchema)schema).getIdentifiablePropertyNames().get(1));
	}

	
	@Test
	public void shouldRDFSchemaIsCompatibleWithIdentifiableProperties(){
		String serverUrl = "http://localhost:8080/mesh4x/feeds";
		String meshGroup = "meshGroup";
		String dataSetId = "dataSetId";

		String url = serverUrl+"/"+meshGroup+"/"+dataSetId;
		
		RDFSchema rdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		rdfSchema.addStringProperty("id1", "id1", IRDFSchema.DEFAULT_LANGUAGE);
		rdfSchema.addStringProperty("id2", "id2", IRDFSchema.DEFAULT_LANGUAGE);
		rdfSchema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		
		RDFSchema otherRdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		otherRdfSchema.addStringProperty("id1", "id1", IRDFSchema.DEFAULT_LANGUAGE);
		otherRdfSchema.addStringProperty("id2", "id2", IRDFSchema.DEFAULT_LANGUAGE);
		otherRdfSchema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
				
		Assert.assertTrue(rdfSchema.isCompatible(otherRdfSchema));
		
		ArrayList<String> pks = new ArrayList<String>();
		pks.add("id1");
		pks.add("id2");
		
		rdfSchema.setIdentifiablePropertyNames(pks);
		rdfSchema.setVersionPropertyName("datetime");
				
		Assert.assertEquals("id1", rdfSchema.getIdentifiablePropertyNames().get(0));
		Assert.assertEquals("id2", rdfSchema.getIdentifiablePropertyNames().get(1));
	
		otherRdfSchema.setIdentifiablePropertyNames(pks);
		otherRdfSchema.setVersionPropertyName("datetime");
				
		Assert.assertEquals("id1", otherRdfSchema.getIdentifiablePropertyNames().get(0));
		Assert.assertEquals("id2", otherRdfSchema.getIdentifiablePropertyNames().get(1));
		
		Assert.assertTrue(rdfSchema.isCompatible(otherRdfSchema));
	}
	
	@Test
	public void shouldRDFSchemaIsNotCompatibleWithIdentifiableProperties(){
		String serverUrl = "http://localhost:8080/mesh4x/feeds";
		String meshGroup = "meshGroup";
		String dataSetId = "dataSetId";

		String url = serverUrl+"/"+meshGroup+"/"+dataSetId;
		
		RDFSchema rdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		rdfSchema.addStringProperty("id1", "id1", IRDFSchema.DEFAULT_LANGUAGE);
		rdfSchema.addStringProperty("id2", "id2", IRDFSchema.DEFAULT_LANGUAGE);
		rdfSchema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
		
		RDFSchema otherRdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		otherRdfSchema.addStringProperty("id1", "id1", IRDFSchema.DEFAULT_LANGUAGE);
		otherRdfSchema.addStringProperty("id2", "id2", IRDFSchema.DEFAULT_LANGUAGE);
		otherRdfSchema.addStringProperty("string", "string", IRDFSchema.DEFAULT_LANGUAGE);
				
		Assert.assertTrue(rdfSchema.isCompatible(otherRdfSchema));
		
		ArrayList<String> pks = new ArrayList<String>();
		pks.add("id1");
		pks.add("id2");
		
		rdfSchema.setIdentifiablePropertyNames(pks);
		rdfSchema.setVersionPropertyName("datetime");
				
		Assert.assertEquals("id1", rdfSchema.getIdentifiablePropertyNames().get(0));
		Assert.assertEquals("id2", rdfSchema.getIdentifiablePropertyNames().get(1));
	
		
		ArrayList<String> pks2 = new ArrayList<String>();
		pks2.add("id2");
		pks2.add("id1");
		otherRdfSchema.setIdentifiablePropertyNames(pks2);
		otherRdfSchema.setVersionPropertyName("datetime");
				
		Assert.assertEquals("id2", otherRdfSchema.getIdentifiablePropertyNames().get(0));
		Assert.assertEquals("id1", otherRdfSchema.getIdentifiablePropertyNames().get(1));
		
		Assert.assertFalse(rdfSchema.isCompatible(otherRdfSchema));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldDontAcceptPropertyNameWithBlanks(){
		String serverUrl = "http://localhost:8080/mesh4x/feeds";
		String meshGroup = "meshGroup";
		String dataSetId = "dataSetId";

		String url = serverUrl+"/"+meshGroup+"/"+dataSetId;
		
		RDFSchema rdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		rdfSchema.addStringProperty("id     1   dwq", "id1", IRDFSchema.DEFAULT_LANGUAGE);		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldAddPropertyFailsWhenPropertyNameIsNull(){
		String serverUrl = "http://localhost:8080/mesh4x/feeds";
		String meshGroup = "meshGroup";
		String dataSetId = "dataSetId";

		String url = serverUrl+"/"+meshGroup+"/"+dataSetId;
		
		RDFSchema rdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		rdfSchema.addStringProperty(null, "id1", IRDFSchema.DEFAULT_LANGUAGE);		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldAddPropertyFailsWhenPropertyNameIsEmpty(){
		String serverUrl = "http://localhost:8080/mesh4x/feeds";
		String meshGroup = "meshGroup";
		String dataSetId = "dataSetId";

		String url = serverUrl+"/"+meshGroup+"/"+dataSetId;
		
		RDFSchema rdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		rdfSchema.addStringProperty("", "id1", IRDFSchema.DEFAULT_LANGUAGE);		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldAddPropertyFailsWhenPropertyNameHasOneBlank(){
		String serverUrl = "http://localhost:8080/mesh4x/feeds";
		String meshGroup = "meshGroup";
		String dataSetId = "dataSetId";

		String url = serverUrl+"/"+meshGroup+"/"+dataSetId;
		
		RDFSchema rdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		rdfSchema.addStringProperty("id 1", "id1", IRDFSchema.DEFAULT_LANGUAGE);		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldAddPropertyFailsWhenPropertyNameHasBlanks(){
		String serverUrl = "http://localhost:8080/mesh4x/feeds";
		String meshGroup = "meshGroup";
		String dataSetId = "dataSetId";

		String url = serverUrl+"/"+meshGroup+"/"+dataSetId;
		
		RDFSchema rdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		rdfSchema.addStringProperty("id 1", "id1", IRDFSchema.DEFAULT_LANGUAGE);		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldAddPropertyFailsWhenLabelIsNull(){
		String serverUrl = "http://localhost:8080/mesh4x/feeds";
		String meshGroup = "meshGroup";
		String dataSetId = "dataSetId";

		String url = serverUrl+"/"+meshGroup+"/"+dataSetId;
		
		RDFSchema rdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		rdfSchema.addStringProperty("id1", null, IRDFSchema.DEFAULT_LANGUAGE);		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldAddPropertyFailsWhenLabelIsEmpty(){
		String serverUrl = "http://localhost:8080/mesh4x/feeds";
		String meshGroup = "meshGroup";
		String dataSetId = "dataSetId";

		String url = serverUrl+"/"+meshGroup+"/"+dataSetId;
		
		RDFSchema rdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		rdfSchema.addStringProperty("id1", "", IRDFSchema.DEFAULT_LANGUAGE);		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldAddPropertyFailsWhenLanguageIsNull(){
		String serverUrl = "http://localhost:8080/mesh4x/feeds";
		String meshGroup = "meshGroup";
		String dataSetId = "dataSetId";

		String url = serverUrl+"/"+meshGroup+"/"+dataSetId;
		
		RDFSchema rdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		rdfSchema.addStringProperty("id1", "id1", null);		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldAddPropertyFailsWhenLanguageIsEmpty(){
		String serverUrl = "http://localhost:8080/mesh4x/feeds";
		String meshGroup = "meshGroup";
		String dataSetId = "dataSetId";

		String url = serverUrl+"/"+meshGroup+"/"+dataSetId;
		
		RDFSchema rdfSchema = new RDFSchema(dataSetId, url+"#", dataSetId);
		rdfSchema.addStringProperty("id1", "id1", "");		
	}
	
	@Test
	public void shouldNormalizePropertyName(){
		Assert.assertEquals("My_Name", RDFSchema.normalizePropertyName("My Name"));
		Assert.assertEquals("My_Second_Name", RDFSchema.normalizePropertyName("My Second Name"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNormalizePropertyNameFailsIfPropertyNameIsNull(){
		RDFSchema.normalizePropertyName(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNormalizePropertyNameFailsIfPropertyNameIsEmpty(){
		RDFSchema.normalizePropertyName("");
	}
}
