package org.mesh4j.ektoo.controller;

import java.util.List;

import org.mesh4j.ektoo.ISyncAdapterBuilder;
import org.mesh4j.ektoo.SyncAdapterBuilder;
import org.mesh4j.ektoo.properties.PropertiesProvider;
import org.mesh4j.ektoo.ui.SyncItemUI;
import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.SyncEngine;
import org.mesh4j.sync.model.Item;
import org.mesh4j.sync.payload.schema.ISchema;
import org.mesh4j.sync.validations.Guard;

/**
 * @author Bhuiyan Mohammad Iklash
 * 
 */
public class EktooUIController 
{
	// MODEL VARIABLESs
	ISyncAdapterBuilder adapterBuilder;

	// BUISINESS METHODS
	public EktooUIController(PropertiesProvider propertiesProvider) 
	{
		Guard.argumentNotNull(propertiesProvider, "propertiesProvider");
		this.adapterBuilder = new SyncAdapterBuilder(propertiesProvider);
	}

	public String sync(SyncItemUI source, SyncItemUI target) 
	{
	  ISyncAdapter sourceAdapter = source.createAdapter();
	  ISyncAdapter targetAdapter = target.createAdapter();
    
	  // TODO (NBL) make it generic and improve this code
	  if (targetAdapter == null || 
	        (
	            ( (String)target.getListType().getSelectedItem()).equals( SyncItemUI.MS_EXCEL_PANEL )
	            && !( (String)source.getListType().getSelectedItem()).equals( SyncItemUI.MS_EXCEL_PANEL )
	        )
	      ) 
    {
      targetAdapter = target.createAdapter(source.createSchema());
    }
	  
		return sync(sourceAdapter, targetAdapter);
	}

	public String sync(ISyncAdapter sourceAdapter, ISyncAdapter targetAdapter) 
	{
		SyncEngine engine = new SyncEngine(sourceAdapter, targetAdapter);
    
		List<Item> items = engine.synchronize();
		
		// TODO (NBL) change these hardcode string
		if (items != null && items.size() > 0) 
		  return "failed";
		
		return "success";
	}
}
