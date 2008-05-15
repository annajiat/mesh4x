package com.mesh4j.sync.model;

import org.dom4j.Element;

import com.mesh4j.sync.validations.Guard;

public abstract class Content implements IContent{

	// MODEL VARIABLESs
	private String id;
	private Element payload;
	private int version;
	
	// BUSINESS METHODS
	public Content(Element payload, String id) {
		Guard.argumentNotNull(payload, "payload");
		Guard.argumentNotNull(id, "id");
		
		this.payload = payload;
		this.id = id;
		this.refreshVersion();
	}

	public abstract Content clone();
	
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj != null)
        {
        	if(obj instanceof IContent){
        		IContent otherItem = (IContent) obj;
        		return
        			this.getId().equals(otherItem.getId())
        			&& this.getVersion() == otherItem.getVersion()
        			&& this.getPayload().asXML().equals(otherItem.getPayload().asXML());
        	}
        }
        return false;
    }

    public int hashCode()
    {
		String payloadXML = payload.asXML();
		return this.id.hashCode() + this.version + payloadXML.hashCode();
    }
	public void refreshVersion() {
		this.version = this.getPayload().asXML().hashCode();		
	}

	public String getId() {
		return id;
	}

	public Element getPayload() {
		return payload;
	}

	public int getVersion() {
		return version;
	}
}
