package org.mesh4j.sync.adapters.hibernate.mapping;

import org.dom4j.Element;
import org.junit.Assert;
import org.junit.Test;
import org.mesh4j.sync.payload.schema.rdf.IRDFSchema;
import org.mesh4j.sync.payload.schema.rdf.RDFSchema;
import org.mesh4j.sync.utils.XMLHelper;

public class HibernateToRDFMappingTests {

	private final RDFSchema RDF_SCHEMA;
	
	{
		RDF_SCHEMA = new RDFSchema("User", "http://localhost:8080/mesh4x/User#", "User");
		RDF_SCHEMA.addStringProperty("id", "id", IRDFSchema.DEFAULT_LANGUAGE);
		RDF_SCHEMA.addStringProperty("name", "name", IRDFSchema.DEFAULT_LANGUAGE);
		RDF_SCHEMA.addStringProperty("pass", "pass", IRDFSchema.DEFAULT_LANGUAGE);
		
		RDF_SCHEMA.setIdentifiablePropertyName("id");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldCreateFailsIfSchemaIsNull(){
		new HibernateToRDFMapping(null);
	}
	
	@Test
	public void shouldCreateMapping(){
		HibernateToRDFMapping mapping = new HibernateToRDFMapping(RDF_SCHEMA);
		
		Assert.assertEquals("User", mapping.getType());
		Assert.assertEquals(1, mapping.getSchema().getIdentifiablePropertyNames().size());
		Assert.assertEquals("id", mapping.getSchema().getIdentifiablePropertyNames().get(0));
		Assert.assertEquals(RDF_SCHEMA, mapping.getSchema());
	}
	
	@Test 
	public void shouldConvertRowToXML() throws Exception{
		HibernateToRDFMapping mapping = new HibernateToRDFMapping(RDF_SCHEMA);
		
		String xml = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:User=\"http://localhost:8080/mesh4x/User#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">"+
		"<User:User rdf:about=\"uri:urn:1\">"+
		"<User:id rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">1</User:id>"+
		"<User:name rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">juan</User:name>"+
		"<User:pass rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">123</User:pass>"+
		"</User:User>"+
		"</rdf:RDF>";
		  
		String xmlRow = "<user><id>1</id><name>juan</name><pass>123</pass></user>";
		Element row = XMLHelper.parseElement(xmlRow);
		Element rowAsRDF = mapping.convertRowToXML("1", row);
		Assert.assertNotNull(rowAsRDF);
		Assert.assertEquals(XMLHelper.canonicalizeXML(xml), XMLHelper.canonicalizeXML(rowAsRDF));
	}
	
	
	@Test 
	public void shouldConvertXMLToRow() throws Exception{
		HibernateToRDFMapping mapping = new HibernateToRDFMapping(RDF_SCHEMA);
		
		String xml = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:User=\"http://localhost:8080/mesh4x/User#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">"+
		"<User:User rdf:about=\"uri:urn:1\">"+
		"<User:id rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">1</User:id>"+
		"<User:name rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">juan</User:name>"+
		"<User:pass rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">123</User:pass>"+
		"</User:User>"+
		"</rdf:RDF>";
		  
		String xmlRow = "<User><id>1</id><name>juan</name><pass>123</pass></User>";
		Element rowAsRDF = XMLHelper.parseElement(xml);
		Element row = mapping.convertXMLToRow(rowAsRDF);
		
		Assert.assertNotNull(row);
		Assert.assertEquals(XMLHelper.canonicalizeXML(xmlRow), XMLHelper.canonicalizeXML(row));
	}
	
	
	@Test 
	public void shouldConvertXMLToRowMultiPlayload() throws Exception{
		HibernateToRDFMapping mapping = new HibernateToRDFMapping(RDF_SCHEMA);
		
		String xml = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:User=\"http://localhost:8080/mesh4x/User#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">"+
		"<User:User rdf:about=\"uri:urn:1\">"+
		"<User:id rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">1</User:id>"+
		"<User:name rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">juan</User:name>"+
		"<User:pass rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">123</User:pass>"+
		"</User:User>"+
		"</rdf:RDF>";
		  
		String xmlRow = "<User><id>1</id><name>juan</name><pass>123</pass></User>";
		Element rowAsRDF = XMLHelper.parseElement("<payload><foo1>bar</foo1>"+xml+"<foo>bar</foo></payload>");
		Element row = mapping.convertXMLToRow(rowAsRDF);
		
		Assert.assertNotNull(row);
		Assert.assertEquals(XMLHelper.canonicalizeXML(xmlRow), XMLHelper.canonicalizeXML(row));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldConvertXMLToRowMultiPlayloadFailsIfRDFElementDoesNotExists() throws Exception{
		HibernateToRDFMapping mapping = new HibernateToRDFMapping(RDF_SCHEMA);
		
		Element rowAsRDF = XMLHelper.parseElement("<payload><foo1>bar</foo1><foo>bar</foo></payload>");
		mapping.convertXMLToRow(rowAsRDF);
	}
	
}
