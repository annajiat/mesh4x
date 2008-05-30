package com.mesh4j.sync.adapters.kml;

import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.mesh4j.sync.adapters.dom.DOMLoader;
import com.mesh4j.sync.adapters.dom.IMeshDOM;
import com.mesh4j.sync.parsers.IXMLView;
import com.mesh4j.sync.security.IIdentityProvider;
import com.mesh4j.sync.translator.MessageTranslator;
import com.mesh4j.sync.utils.ZipUtils;
import com.mesh4j.sync.validations.Guard;
import com.mesh4j.sync.validations.MeshException;

public class KMZDOMLoader extends DOMLoader {

	public KMZDOMLoader(String fileName, IIdentityProvider identityProvider,
			IXMLView xmlView) {
		super(fileName, identityProvider, xmlView);
		
		String localFileName = fileName.trim(); 
		if(!localFileName.toUpperCase().endsWith(".KMZ")){
			Guard.throwsArgumentException("Arg_InvalidKMLFileName", fileName);
		}
	}

	@Override
	protected void flush() {
		try {
			ZipUtils.write(this.getFile(), KmlNames.KMZ_DEFAULT_ENTRY_NAME_TO_KML, this.getDOM().asXML());
		} catch (IOException e) {
			throw new MeshException(e);
		}		
	}

	@Override
	protected IMeshDOM load() {
		try {
			String kml = ZipUtils.getTextEntryContent(this.getFile(), KmlNames.KMZ_DEFAULT_ENTRY_NAME_TO_KML);
			Document document = DocumentHelper.parseText(kml);
			return new KMLDOM(document, getIdentityProvider(), getXMLView());
		} catch (DocumentException e) {
			throw new MeshException(e);
		} catch (IOException e) {
			throw new MeshException(e);
		}
	}

	@Override
	protected IMeshDOM createDocument(String name) {
		return new KMLDOM(name, getIdentityProvider(), getXMLView());
	}	
	
	public String getFriendlyName() {
		return MessageTranslator.translate(this.getClass().getName());
	}	
}
