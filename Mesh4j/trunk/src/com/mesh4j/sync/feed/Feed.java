package com.mesh4j.sync.feed;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import com.mesh4j.sync.model.Item;

public class Feed {

	// MODEL VARIABLES
	private List<Item> feedItems = new ArrayList<Item>();
	private Element payload;
		
	// BUSINES METHODS
	
	public Feed addItem(Item item) {
		this.feedItems.add(item);
		return this;
		
	}

	public Feed deleteItem(Item item) {
		this.feedItems.remove(item);
		return this;
	}

	public List<Item> getItems() {
		return this.feedItems;
	}

	public Element getPayload() {
		return payload;
	}
	public void setPayload(Element payload) {
		this.payload = payload;		
	}

}
