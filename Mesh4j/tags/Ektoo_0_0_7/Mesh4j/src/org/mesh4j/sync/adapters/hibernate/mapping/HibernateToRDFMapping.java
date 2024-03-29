package org.mesh4j.sync.adapters.hibernate.mapping;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;
import org.mesh4j.sync.adapters.feed.ISyndicationFormat;
import org.mesh4j.sync.payload.schema.ISchemaTypeFormat;
import org.mesh4j.sync.payload.schema.SchemaTypeFormat;
import org.mesh4j.sync.payload.schema.rdf.AbstractRDFIdentifiableMapping;
import org.mesh4j.sync.payload.schema.rdf.IRDFSchema;
import org.mesh4j.sync.payload.schema.rdf.RDFInstance;
import org.mesh4j.sync.utils.XMLHelper;
import org.mesh4j.sync.validations.Guard;

public class HibernateToRDFMapping extends AbstractRDFIdentifiableMapping implements IHibernateToXMLMapping {
	
	private static final String DATE_FORMAT = "yyyy-mm-dd hh:mm:ss";
	private static HashMap<String, ISchemaTypeFormat> FORMATS = new HashMap<String, ISchemaTypeFormat>();
	
	static{
		FORMATS.put(IRDFSchema.XLS_DATETIME, new SchemaTypeFormat(new SimpleDateFormat(DATE_FORMAT)));
	}
	
	// BUSINESS METHODS
	public HibernateToRDFMapping(IRDFSchema rdfSchema){
		super(rdfSchema);
	}
	
	@Override
	public Element convertRowToXML(String id, Element element) throws Exception {
		RDFInstance instance = this.rdfSchema.createNewInstanceFromPlainXML(id, element.asXML(), FORMATS);
		String rdfXml = instance.asXML();
		return XMLHelper.parseElement(rdfXml);
	}

	@Override
	public Element convertXMLToRow(Element element) throws Exception {
		String rdfXml;
		if(ISyndicationFormat.ELEMENT_PAYLOAD.equals(element.getName())){
			Element rdfElement = element.element(IRDFSchema.ELEMENT_RDF);
			if(rdfElement == null){
				Guard.throwsArgumentException("payload");
			}
			rdfXml = rdfElement.asXML();
		} else {
			rdfXml = element.asXML();
		}
		RDFInstance instance = this.rdfSchema.createNewInstanceFromRDFXML(rdfXml);
		String xml = instance.asPlainXML(FORMATS);
		return XMLHelper.parseElement(xml);
	}

	@Override
	public String getMeshId(Element entityElement) {
		if(entityElement == null){
			return null;
		}

		List<String> idColumnNames = this.rdfSchema.getIdentifiablePropertyNames();
		List<String> idValues = new ArrayList<String>();
		String idCellValue;
		for (String idColumnName : idColumnNames) {
			Element idElement = entityElement.element(idColumnName);
			if(idElement == null){
				return null;
			}
			idCellValue = idElement.getText();
			if(idCellValue == null){
				return null;
			} else {
				idValues.add(idCellValue);
			}
		}
		return makeId(idValues);	
	}
}
