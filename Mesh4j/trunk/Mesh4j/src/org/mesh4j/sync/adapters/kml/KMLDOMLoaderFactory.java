package org.mesh4j.sync.adapters.kml;

import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.adapters.ISyncAdapterFactory;
import org.mesh4j.sync.adapters.dom.DOMAdapter;
import org.mesh4j.sync.adapters.dom.DOMLoader;
import org.mesh4j.sync.adapters.dom.MeshNames;
import org.mesh4j.sync.adapters.dom.parsers.FileManager;
import org.mesh4j.sync.adapters.dom.parsers.FileXMLViewElement;
import org.mesh4j.sync.adapters.dom.parsers.HierarchyXMLViewElement;
import org.mesh4j.sync.parsers.XMLView;
import org.mesh4j.sync.security.IIdentityProvider;
import org.mesh4j.sync.validations.Guard;

public class KMLDOMLoaderFactory implements ISyncAdapterFactory {

	public static final String SOURCE_TYPE = KmlNames.KML_PREFIX;
	
	// BUSINESS METHODS
	public KMLDOMLoaderFactory(){
		super();
	}
	
	public static boolean isKML(String fileName){
		if(fileName == null || fileName.isEmpty()){
			return false;
		}
		String localFileName = fileName.trim();
		return localFileName.toUpperCase().endsWith(".KMZ") || localFileName.toUpperCase().endsWith(".KML");
	}
	
	public static DOMLoader createDOMLoader(String fileName, IIdentityProvider identityProvider){
		Guard.argumentNotNullOrEmptyString(fileName, "fileName");
		Guard.argumentNotNull(identityProvider, "identityProvider");
		
		String localFileName = fileName.trim();
		if(localFileName.toUpperCase().endsWith(".KMZ")){
			FileManager fileManager = new FileManager();
			return new KMZDOMLoader(fileName, identityProvider, createView(fileManager), fileManager);
		} else if (localFileName.toUpperCase().endsWith(".KML")){
			FileManager fileManager = new FileManager();
			return new KMLDOMLoader(fileName, identityProvider, createView(fileManager), fileManager);
		} else {
			Guard.throwsArgumentException("Arg_InvalidKMLFileName", fileName);
			return null; // Only for java compilation
		}
	}
	
	public static XMLView createView(FileManager fileManager){
		HierarchyXMLViewElement hierarchyView = new HierarchyXMLViewElement();
		
		KMLViewElement folderView = new KMLViewElement(KmlNames.KML_QNAME_FOLDER, hierarchyView, false);
		folderView.addAttribute(MeshNames.MESH_QNAME_SYNC_ID);
		folderView.addAttribute(KmlNames.KML_ATTRIBUTE_ID_QNAME);
		folderView.addElement(KmlNames.KML_ELEMENT_NAME);
		folderView.addElement(KmlNames.KML_ELEMENT_DESCRIPTION);
		folderView.addElement(KmlNames.KML_ELEMENT_EXTENDED_DATA);
		
		KMLViewElement placemarkView = new KMLViewElement(KmlNames.KML_QNAME_PLACEMARK, hierarchyView, false);
		KMLViewElement photoView = new KMLViewElement(KmlNames.KML_QNAME_PHOTO_OVERLAY, hierarchyView, false);
		KMLViewElement imageView = new KMLViewElement(KmlNames.KML_QNAME_GROUND_OVERLAY, hierarchyView, false);
		KMLViewElement styleMapView = new KMLViewElement(KmlNames.KML_QNAME_STYLE_MAP, hierarchyView, true);
		KMLViewElement styleView = new KMLViewElement(KmlNames.KML_QNAME_STYLE, hierarchyView, true);
		KMLSchemaXMLViewElement schemaView = new KMLSchemaXMLViewElement();
		KMLDocumentExtendedDataViewElement documentExtendedDataView = new KMLDocumentExtendedDataViewElement();
		FileXMLViewElement fileView = new FileXMLViewElement(fileManager);
		return new XMLView(styleView, styleMapView, folderView, placemarkView, photoView, imageView, schemaView, documentExtendedDataView, hierarchyView, fileView);
	}
	
	// ISyncAdapterFactry methods
	
	public static String createSourceDefinition(String kmlFileName){
		return SOURCE_TYPE + ":" + kmlFileName;
	}
	
	@Override
	public boolean acceptsSource(String sourceId, String sourceDefinition) {
		return sourceDefinition != null && sourceDefinition.toUpperCase().startsWith(SOURCE_TYPE) && isKML(sourceDefinition);
	}

	@Override
	public ISyncAdapter createSyncAdapter(String sourceAlias, String sourceDefinition, IIdentityProvider identityProvider) throws Exception {
		String fileName = sourceDefinition.substring(SOURCE_TYPE.length() + 1, sourceDefinition.length());
		String kmlFileName = fileName;
		DOMAdapter kmlAdapter = new DOMAdapter(createDOMLoader(kmlFileName, identityProvider));
		return kmlAdapter;
	}

	@Override
	public String getSourceType() {
		return SOURCE_TYPE;
	}

	public static ISyncAdapter createKMLAdapter(String kmlFileName, IIdentityProvider identityProvider) {
		return new DOMAdapter(createDOMLoader(kmlFileName, identityProvider));
	}
}