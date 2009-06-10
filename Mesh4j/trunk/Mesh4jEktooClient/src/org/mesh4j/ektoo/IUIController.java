package org.mesh4j.ektoo;

import org.mesh4j.sync.ISyncAdapter;
/**
 * @author Bhuiyan Mohammad Iklash
 *
 */
public interface IUIController 
{
  public ISyncAdapter createAdapter();
  public ISyncAdapter createAdapter(UISchema schema);
  public UISchema fetchSchema(ISyncAdapter adapter); 
}
