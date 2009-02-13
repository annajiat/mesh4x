package org.mesh4j.sync.properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.mesh4j.sync.security.IIdentityProvider;
import org.mesh4j.sync.security.LoggedInIdentityProvider;
import org.mesh4j.sync.validations.MeshException;


public class PropertiesProvider {

	// MODEL VARIABLES
	private Properties properties;
	
	// BUSINESS METHODS
	public PropertiesProvider(){
		super();
		initialize("mesh4j.properties");
	}
	
	public PropertiesProvider(String resourceName){
		super();
		initialize(resourceName);
	}
	
	private void initialize(String resourceName) {
		try{
			FileReader reader = new FileReader(resourceName);
			Properties prop = new Properties();
			prop.load(reader);
			this.properties = prop;
			reader.close();
		} catch (Exception e) {
			throw new MeshException(e);
		}
	}

	public String getDefaultEnpoint1() throws IOException {
		String defaultEndpoint1 =  this.properties.getProperty("default.kml.file", "");
		File file = new File(defaultEndpoint1);
		defaultEndpoint1 = file.getCanonicalPath();
		return defaultEndpoint1;
	}

	public String getDefaultEnpoint2() {
		return this.properties.getProperty("default.feed.url", "");
	}

	public IIdentityProvider getIdentityProvider() {
		try{
			String identityProviderClassName =  this.properties.getProperty("sync.identity.provider", LoggedInIdentityProvider.class.getName());
			IIdentityProvider security = (IIdentityProvider)makeNewInstance(identityProviderClassName);
			return security;
		} catch (Exception e) {
			throw new MeshException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Object makeNewInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class clazz = Class.forName(className);
		return clazz.newInstance();
	}

	public String getBaseDirectory() throws IOException {
		String baseDir =  this.properties.getProperty("default.base.directory", getCurrentDirectory());
		File file = new File(baseDir);
		return file.getCanonicalPath();
	}
	
	public String getCurrentDirectory(){
		return System.getProperty("user.dir");
	}

	public int getInt(String key) {
		return Integer.valueOf(this.properties.getProperty(key, "0"));
	}

	public String getString(String key) {
		return this.properties.getProperty(key, "");
	}

	public boolean getBoolean(String key) {
		return Boolean.valueOf(this.properties.getProperty(key, "true"));
	}

	public Object getInstance(String key, Object defaultInstanceIfAbsent) {
		try{
			String className = this.getString(key);
			if(className == null || className.length() == 0){
				return defaultInstanceIfAbsent;
			}
			return makeNewInstance(className);
		} catch (Exception e) {
			throw new MeshException(e);
		}
	}
}
