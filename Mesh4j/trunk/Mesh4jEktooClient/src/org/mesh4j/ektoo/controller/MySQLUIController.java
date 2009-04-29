package org.mesh4j.ektoo.controller;

import java.beans.PropertyChangeEvent;

import org.mesh4j.ektoo.GoogleSpreadSheetInfo;
import org.mesh4j.ektoo.ISyncAdapterBuilder;
import org.mesh4j.ektoo.IUIController;
import org.mesh4j.ektoo.SyncAdapterBuilder;
import org.mesh4j.ektoo.model.MySQLAdapterModel;
import org.mesh4j.ektoo.properties.PropertiesProvider;
import org.mesh4j.sync.ISyncAdapter;
import org.mesh4j.sync.validations.Guard;
/**
 * @author Bhuiyan Mohammad Iklash
 *
 */
public class MySQLUIController extends AbstractController implements IUIController
{
  private static final String USER_NAME_PROPERTY      = "UserName";
  private static final String USER_PASSWORD_PROPERTY  = "UserPassword";
  private static final String HOST_NAME_PROPERTY      = "HostName";
  private static final String PORT_NO_PROPERTY        = "PortNo";
  private static final String DATABASE_NAME_PROPERTY  = "DatabaseName";
  private static final String TABLE_NAME_PROPERTY     = "TableName";

  private ISyncAdapterBuilder adapterBuilder;
  private PropertiesProvider propertiesProvider;

	public MySQLUIController(PropertiesProvider propertiesProvider) 
	{
		Guard.argumentNotNull(propertiesProvider, "propertiesProvider");
    this.propertiesProvider = propertiesProvider;
    this.adapterBuilder = new SyncAdapterBuilder(propertiesProvider);
	}

  public void changeUserName(String userName)
  {
    setModelProperty( USER_NAME_PROPERTY, userName);
  }
	
  public void changeUserPassword(String userPassword)
  {
    setModelProperty( USER_PASSWORD_PROPERTY, userPassword);
  }

  public void changeHostName(String hostName)
  {
    setModelProperty( HOST_NAME_PROPERTY, hostName);
  }

  public void changePortNo(String portNo)
  {
    setModelProperty( PORT_NO_PROPERTY, portNo);
  }

  public void changeDatabaseName(String databaseName)
  {
    setModelProperty( DATABASE_NAME_PROPERTY, databaseName);
  }
  
  public void changeTableName(String tableName)
  {
    setModelProperty( TABLE_NAME_PROPERTY, tableName);
  }
  
	@Override
	public ISyncAdapter createAdapter() 
	{
	  MySQLAdapterModel model = (MySQLAdapterModel)this.getModel();
	  if (model == null) return null;
	  
	  String hostName = model.getHostName();
	  if (hostName == null) return null;
	  
	  int portNo = model.getPortNo();
	  if (portNo < 0) return null;
	  
	  String databaseName = model.getDatabaseName();
	  if (databaseName == null) return null;
	  
	  String tableName = model.getTableName();
	  if (tableName == null) return null;
	  
		return adapterBuilder.createMySQLAdapter(hostName, portNo, databaseName, tableName);
	}

  @Override
  public void propertyChange(PropertyChangeEvent arg0)
  {   
  }
}
