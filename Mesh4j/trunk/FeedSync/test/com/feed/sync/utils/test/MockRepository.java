package com.feed.sync.utils.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import com.feed.sync.AbstractRepository;
import com.feed.sync.Predicate;
import com.feed.sync.model.History;
import com.feed.sync.model.Item;
import com.feed.sync.model.NullModelItem;
import com.feed.sync.validations.Guard;

public class MockRepository extends AbstractRepository {

	// MODEL VARIABLES
	private String name;
	private Hashtable<String, Item> items = new Hashtable<String, Item>();

	// BUSINESS METHODS
	public MockRepository()
	{
		super();
	}

	public MockRepository(Item item)
	{
		super();
		items.put(item.getSyncId(), item);
	}
	public MockRepository(Item[] allItems)
	{
		
		super();
		for (Item item : allItems)
		{
			items.put(item.getSyncId(), item);
		}
	}

	public MockRepository(String name)
	{
		super();
		this.name = name;
	}

	public String getFriendlyName()
	{
		return name;
	}

	public boolean supportsMerge()
	{
		return false;
	}

	public void add(Item item)
	{
		Guard.argumentNotNull(item, "item");

		if (items.containsKey(item.getSyncId()))
			throw new IllegalArgumentException();

		items.put(item.getSyncId(), item);
	}

	public Item get(String id)
	{
		Guard.argumentNotNullOrEmptyString(id, "id");

		if (items.containsKey(id))
			return items.get(id).clone();
		else
			return null;
	}

	protected List<Item> getAll(Date since, Predicate<Item> filter)
	{
		Guard.argumentNotNull(filter, "filter");

		ArrayList<Item> allItems = new ArrayList<Item>();
		for(Item item : items.values())
		{
			History lastUpdate = item.getLastUpdate();
			if ((since == null || 
					lastUpdate == null || 
					lastUpdate.getWhen() == null || 
					lastUpdate.getWhen().compareTo(since)>= 0)
				&& filter.evaluate(item))
				allItems.add(item.clone());
		}
		return allItems;
	}

	public void delete(String id)
	{
		Guard.argumentNotNullOrEmptyString(id, "id");

		items.remove(id);
	}

	public void update(Item item)
	{
		Guard.argumentNotNull(item, "item");

		Item i;
		if (item.getSync().isDeleted()){
			i = new Item(new NullModelItem(item.getSyncId()), item.getSync().clone());
		}else{
			i = item.clone();
		}

		items.put(item.getSyncId(), i);
	}

	public List<Item> merge(List<Item> items)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCurrentAuthor() {
		return "JMT";
	}


	public Hashtable<String, Item> getItems() {
		return items;
	}
}
