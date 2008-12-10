package org.mesh4j.sync.adapters.kml.exporter;

import java.text.MessageFormat;
import java.util.List;

import org.dom4j.Element;
import org.mesh4j.sync.model.Item;
import org.mesh4j.sync.payload.schema.ISchemaResolver;
import org.mesh4j.sync.utils.FileUtils;

public class KMLExporter {

	public static void makeKMLWithNetworkLink(String fileName, String documentName, String url) throws Exception{
		String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://earth.google.com/kml/2.2\"><Document><name>{0}</name><open>1</open><NetworkLink><name>{0}</name><visibility>0</visibility><open>0</open><refreshVisibility>0</refreshVisibility><flyToView>0</flyToView><Link><href>{1}</href></Link></NetworkLink></Document></kml>";	
		FileUtils.write(fileName, MessageFormat.format(template, documentName, url).getBytes());
	}
	
	public static void export(String fileName, String documentName, List<Item> items, ISchemaResolver propertyResolver) throws Exception {
		String kmlXml = generateKML(documentName, items, propertyResolver);
		FileUtils.write(fileName, kmlXml.getBytes());
	}
	
	public static String generateKML(String documentName, List<Item> items, ISchemaResolver schemaResolver) {
		StringBuffer sb = new StringBuffer();
		sb.append(MessageFormat.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://earth.google.com/kml/2.2\"><Document><name>{0}</name><open>1</open>", documentName));
		for (Item item : items) {
			makeElement(sb, item.getContent().getPayload(), schemaResolver);
		}
		sb.append("</Document></kml>");
		return sb.toString();
	}
	
	// TODO (JMT) RDF schema
	private static void makeElement(StringBuffer sb, Element element, ISchemaResolver schemaResolver) {
		String longitude= schemaResolver.getValue(element, "//geo.longitude");
		String latitude= schemaResolver.getValue(element, "//geo.latitude");

		if(longitude != null && longitude.trim().length() > 0 && latitude != null && latitude.trim().length() > 0){
			String name = schemaResolver.getValue(element, "//item.title");
			String description = schemaResolver.getValue(element, "//item.description");
			sb.append(MessageFormat.format("<Placemark><name>{0}</name><description><![CDATA[{1}]]></description><Point><coordinates>{2},{3}</coordinates></Point></Placemark>", name, description, longitude, latitude));
		}		
	}

}
