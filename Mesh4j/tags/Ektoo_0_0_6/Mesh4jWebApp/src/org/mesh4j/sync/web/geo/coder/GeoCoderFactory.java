package org.mesh4j.sync.web.geo.coder;

import java.util.Properties;

import org.mesh4j.geo.coder.GoogleGeoCoder;
import org.mesh4j.geo.coder.IGeoCoder;
import org.mesh4j.sync.servlet.FeedServlet;

public class GeoCoderFactory {

	// CONSTANTS
	private static final String GEO_CODER_CLASS_NAME = "geo.coder";
	private static final String GOOGLE_GEO_CODER_KEY = "google.geo.coder.key";

	// BUSINESS METHODS
	
	public static IGeoCoder createGeoCoder(FeedServlet feedServlet, Properties properties) {
		 String geoCoderClassName = properties.getProperty(GEO_CODER_CLASS_NAME);
		 if(GoogleGeoCoder.class.getName().equals(geoCoderClassName)){
			 String googleKey = properties.getProperty(GOOGLE_GEO_CODER_KEY);
			 if(googleKey == null || googleKey.trim().length() == 0){
				 return null;
			 } else {
				 return new GoogleGeoCoder(googleKey);
			 }
		 }
		 return null;
	}

}
