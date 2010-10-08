package org.mesh4j.meshes.model;

import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.adapters.epiinfo.EpiInfoSyncAdapterFactory;
import org.mesh4j.sync.payload.schema.ISchema;
import org.mesh4j.sync.payload.schema.rdf.IRDFSchema;
import org.mesh4j.sync.security.LoggedInIdentityProvider;

@XmlRootElement(name = "epiInfoDataSource")
@XmlAccessorType(XmlAccessType.NONE)
public class EpiInfoDataSource extends MsAccessDataSource {
	
	@Override
	public ISyncAdapter createSyncAdapter(ISchema schema, String baseDirectory, FeedRef feedRef) {
		return EpiInfoSyncAdapterFactory.createSyncAdapter(getFileName(), feedRef.getLocalName(), getRdfSchemaBaseUri(), baseDirectory, new LoggedInIdentityProvider(), (IRDFSchema) schema);
	}
	
	@Override
	public String toString() {
		return String.format("EpiInfo: %s", new File(getFileName()).getName());
	}
	
	@Override
	public void accept(MeshVisitor visitor) {
		visitor.visit(this);
	}

}
