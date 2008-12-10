package org.mesh4j.geo.coder;

import junit.framework.Assert;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;
import org.mesh4j.sync.payload.schema.SchemaResolver;

public class GoogleGeoCoderTests {

	@Test
	public void shouldGetLocation(){
		GoogleGeoCoder geoCoder = new GoogleGeoCoder("ABQIAAAAjpkAC9ePGem0lIq5XcMiuhT2yXp_ZAY8_ufC3CFXhHIE1NvwkxTS6gjckBmeABOGXIUiOiZObZESPg");
		
		GeoLocation geoLocation = geoCoder.getLocation("Buenos Aires");
		Assert.assertNotNull(geoLocation);
		
		System.out.println(geoLocation.toString());
	}
	
	@Test
	public void shouldGetLatLong() throws DocumentException{
		GoogleGeoCoder geoCoder = new GoogleGeoCoder("ABQIAAAAjpkAC9ePGem0lIq5XcMiuhT2yXp_ZAY8_ufC3CFXhHIE1NvwkxTS6gjckBmeABOGXIUiOiZObZESPg");

		GeoCoderLatitudePropertyResolver propertyResolver = new GeoCoderLatitudePropertyResolver(geoCoder);
		
		String variableTemplate = "geoLatitude(address)";
		Assert.assertTrue(propertyResolver.accepts(variableTemplate));

		Element element = DocumentHelper.parseText("<patient><name>jose</name><address>Buenos Aires</address></patient>").getRootElement();
		Assert.assertEquals("-34.611781", propertyResolver.getPropertyValue(element, variableTemplate));
		
		
		GeoCoderLongitudePropertyResolver propertyResolverLon = new GeoCoderLongitudePropertyResolver(geoCoder);
		String variableTemplateLon = "geoLongitude(address)";
		Assert.assertTrue(propertyResolverLon.accepts(variableTemplateLon));

		Assert.assertEquals("-58.417309", propertyResolverLon.getPropertyValue(element, variableTemplateLon));
		
	}
	
	@Test
	public void shouldSchemaResolver() throws DocumentException{
		GoogleGeoCoder geoCoder = new GoogleGeoCoder("ABQIAAAAjpkAC9ePGem0lIq5XcMiuhT2yXp_ZAY8_ufC3CFXhHIE1NvwkxTS6gjckBmeABOGXIUiOiZObZESPg");

		GeoCoderLatitudePropertyResolver propertyResolverLat = new GeoCoderLatitudePropertyResolver(geoCoder);
		GeoCoderLongitudePropertyResolver propertyResolverLon = new GeoCoderLongitudePropertyResolver(geoCoder);

		Element schemaElement = DocumentHelper.parseText("<schema><geolat>{geoLatitude(patient/address)}</geolat><geolong>{geoLongitude(patient/address)}</geolong></schema>").getRootElement();
		SchemaResolver sr = new SchemaResolver(schemaElement, propertyResolverLat, propertyResolverLon);

		Element element = DocumentHelper.parseText("<patient><name>jose</name><address>Buenos Aires</address></patient>").getRootElement();
		Assert.assertEquals("-34.611781", sr.getValue(element, "geolat"));
		Assert.assertEquals("-58.417309", sr.getValue(element, "geolong"));
	}
	
}