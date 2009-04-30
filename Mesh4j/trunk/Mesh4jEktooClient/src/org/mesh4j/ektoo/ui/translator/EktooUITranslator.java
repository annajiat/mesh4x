package org.mesh4j.ektoo.ui.translator;

import java.text.DateFormat;
import java.util.Date;

import org.mesh4j.sync.translator.MessageTranslator;
import org.mesh4j.translator.EktooMessageTranslator;

public class EktooUITranslator 
{

	public static String getSyncViaLabel() 
	{
	  return EktooMessageTranslator.translate("EKTOO_SYNC_CHANNEL_LABEL");
	}
	
	public static String getSyncViaSMSLabel() 
	{
		return EktooMessageTranslator.translate("EKTOO_SYNC_CHANNEL_SMS_FIELD_LABEL");
	}
	
	public static String getSyncViaWebLabel() 
	{
		return EktooMessageTranslator.translate("EKTOO_SYNC_CHANNEL_WEB_FIELD_LABEL");
	}
	
	public static String getSyncViaFileLabel() 
	{
	  return EktooMessageTranslator.translate("EKTOO_SYNC_CHANNEL_FILE_FIELD_LABEL");
	}
	
	public static String getSyncTypeSendLabel() 
	{
	  return EktooMessageTranslator.translate("EKTOO_SYNC_TYPE_SEND_FIELD_LABEL");
	}
	
	public static String getSyncTypeReceiveLabel() 
	{
	  return EktooMessageTranslator.translate("EKTOO_SYNC_TYPE_RECEIVE_FIELD_LABEL");
	}
	
	public static String getSyncTypeSendAndReceiveLabel() 
	{
		//return "Send & Receive";
		return EktooMessageTranslator.translate("EKTOO_SYNC_TYPE_SEND_AND_RECEIVE_FIELD_LABEL");
	}
	
	public static String getMessageSyncSyccessfuly() 
	{
		//return "Successfull";
	  return EktooMessageTranslator.translate("EKTOO_SYNC_PROCESS_SUCCESS_MESSAGE");
	}

	public static String getMessageSyncConflicts() 
	{
		//return "Conflicts";
		return EktooMessageTranslator.translate("EKTOO_SYNC_PROCESS_CONFLICT_MESSAGE");
	}
	
	
	public static String getExcelFileDescription() 
	{
		//return "Microsoft Excel File(s)";
		return EktooMessageTranslator.translate("EKTOO_EXCEL_FILE_TYPE_DESCRIPTION");
	}
	public static String getExcelFileSelectorTitle() 
	{
		//return "Select Excel File (.xls)";
		return EktooMessageTranslator.translate("EKTOO_EXCEL_FILE_CHOOSER_TITLE");
	}
	public static String getExcelWorksheetLabel() 
	{
		//return "Worksheet";
	  return EktooMessageTranslator.translate("EKTOO_EXCEL_WORKSHEET_FIELD_LABEL");
	}
	public static String getExcelUniqueColumnLabel() 
	{
		//return "Unique Column";
		return EktooMessageTranslator.translate("EKTOO_EXCEL_UNIQUE_COLUMN_FIELD_LABEL");
	}
	public static String getExcelFileLabel() 
	{
		//return "File";
		return EktooMessageTranslator.translate("EKTOO_EXCEL_FILE_FIELD_LABEL");
	}
	
	public static String getGooglePasswordLabel() 
	{
		//return "Password";
		return EktooMessageTranslator.translate("EKTOO_GOOGLE_PASSWORD_FIELD_LABEL");
	}
	public static String getGoogleUserLabel() 
	{
		//return "User";
	  return EktooMessageTranslator.translate("EKTOO_GOOGLE_USER_FIELD_LABEL");
	}
	public static String getGoogleWorksheetLabel() 
	{
		//return "Worksheet";
		return EktooMessageTranslator.translate("EKTOO_GOOGLE_WORKSHEET_FIELD_LABEL");
	}
	public static String getGoogleKeyLabel() 
	{
		//return "Key";
		return EktooMessageTranslator.translate("EKTOO_GOOGLE_KEY_FIELD_LABEL");
	}
	public static String getUniqueColumnNameLabel() 
	{
		//return "Unique Column";
		return EktooMessageTranslator.translate("EKTOO_GOOGLE_UNIQUE_COLUMN_FIELD_LABEL");
	}
	public static String getSyncTypeLabel() 
	{
		//return "Sync Type";
	  return EktooMessageTranslator.translate("EKTOO_SYNC_TYPE_LABEL");
	}
	public static String getSourceSyncItemSelectorTitle()
	{
		//return "Source";
	  return EktooMessageTranslator.translate("EKTOO_SYNC_SOURCE_LABEL");
	}
	public static String getTargetSyncItemSelectorTitle() 
	{
		//return "Target";
		return EktooMessageTranslator.translate("EKTOO_SYNC_TARGET_LABEL");
	}
	public static String getExcelTableLabel() 
	{
		//return "Table";
		return EktooMessageTranslator.translate("EKTOO_EXCEL_TABLE_FIELD_LABEL");
	}
	public static String getAccessFileLabel() 
	{
		//return "Database";
		return EktooMessageTranslator.translate("EKTOO_ACCESS_FILE_FIELD_LABEL");
	}
	public static String getAccessTableLabel() 
	{
		//return "Table";
		return EktooMessageTranslator.translate("EKTOO_ACCESS_TABLE_FIELD_LABEL");
	}
  public static String getFileLabel()
  {
    //return "File";
    return EktooMessageTranslator.translate("EKTOO_FILE_FIELD_DEFAULT_LABEL");
  }
  public static String getBrowseButtonLabel()
  {
    //return "...";
    return EktooMessageTranslator.translate("EKTOO_FILE_CHOOSER_BUTTON_DEFAULT_LABEL");
  }
  public static String getTableLabel()
  {
    return EktooMessageTranslator.translate("EKTOO_TABLE_FIELD_DEFAULT_LABEL");
  }
  public static String getFieldLabel()
  {
    return EktooMessageTranslator.translate("EKTOO_FIELD_FIELD_DEFAULT_LABEL");
  }
  public static String getGoogleWorksheetColumnLabel()
  {
    return EktooMessageTranslator.translate("EKTOO_GOOGLE_WORKSHEET_COLUMN_FIELD_LABEL");
  }
  public static String getKmlUriLabel()
  {
    return EktooMessageTranslator.translate("EKTOO_KML_URI_FIELD_LABEL");
  }
  public static String getSyncDataSourceType()
  {
    return EktooMessageTranslator.translate("EKTOO_SYNC_PROCESS_DATA_SOURCE_LABEL");
  }
  public static String getSyncLabel()
  {
    return EktooMessageTranslator.translate("EKTOO_SYNC_PROCESS_START_BUTTON_LABEL");
  }

  public static String getDataSourceType()
  {
    return EktooMessageTranslator.translate("EKTOO_SYNC_PROCESS_DATA_SOURCE_LIST_LABEL");
  }

  public static String getMySQLHostLabel()
  {
    return EktooMessageTranslator.translate("EKTOO_MYSQL_HOST_NAME_FIELD_LABEL");
  }		
  public static String getMySQLPortLabel()
  {
    return EktooMessageTranslator.translate("EKTOO_MYSQL_PORT_NO_FIELD_LABEL");
  }   
  public static String getMySQLDatabaseLabel()
  {
    return EktooMessageTranslator.translate("EKTOO_MYSQL_DATABASE_NAME_FIELD_LABEL");
  }  
  public static String getMySQLTableLabel()
  {
    return EktooMessageTranslator.translate("EKTOO_MYSQL_TABLE_NAME_FIELD_LABEL");
  }

  public static String getMySQLUserLabel()
  {
    return EktooMessageTranslator.translate("EKTOO_MYSQL_USER_NAME_FIELD_LABEL");
  }

  public static String getMySQLPasswordLabel()
  {
    return EktooMessageTranslator.translate("EKTOO_MYSQL_USER_PASSWORD_FIELD_LABEL");
  }
}
