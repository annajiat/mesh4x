package org.mesh4j.sync.adapters.http;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.mesh4j.sync.adapters.ISyncAdapterFactory;
import org.mesh4j.sync.adapters.feed.ContentReader;
import org.mesh4j.sync.adapters.feed.ContentWriter;
import org.mesh4j.sync.adapters.feed.rss.RssSyndicationFormat;
import org.mesh4j.sync.id.generator.IdGenerator;
import org.mesh4j.sync.security.IIdentityProvider;

public class HttpSyncAdapterFactory implements ISyncAdapterFactory {

	public static final String SOURCE_TYPE = "HTTP";
	
	public static final HttpSyncAdapterFactory INSTANCE = new HttpSyncAdapterFactory();
	
	@Override
	public boolean acceptsSource(String sourceId, String sourceDefinition) {
		return sourceDefinition != null && sourceDefinition.toUpperCase().startsWith("HTTP://") && isValidURL(sourceDefinition);
	}

	@Override
	public HttpSyncAdapter createSyncAdapter(String sourceAlias, String sourceDefinition, IIdentityProvider identityProvider) throws Exception {
		return new HttpSyncAdapter(sourceDefinition, RssSyndicationFormat.INSTANCE, identityProvider, IdGenerator.INSTANCE, ContentWriter.INSTANCE, ContentReader.INSTANCE);
	}

	@Override
	public String getSourceType() {
		return SOURCE_TYPE;
	}
	
	
	public static boolean isValidURL(String url){
		URL newURL;
		try {
			newURL = new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}
			
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) newURL.openConnection();
			conn.connect();
		} catch (Exception e) {
			return false;
		}finally{
	    	if(conn != null){
	    		conn.disconnect();
	    	}
		}
		return true;
	}

	public static boolean isURL(String url) {
		return url != null && url.toUpperCase().startsWith("HTTP://");
	}

	public static boolean isMalformedURL(String url) {
		try {
			new URL(url);
			return false;
		} catch (MalformedURLException e) {
			return true;
		}
	}

}
