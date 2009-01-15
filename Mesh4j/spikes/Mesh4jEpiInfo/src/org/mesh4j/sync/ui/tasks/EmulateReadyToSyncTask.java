package org.mesh4j.sync.ui.tasks;

import javax.swing.SwingWorker;

import org.mesh4j.sync.epiinfo.ui.EpiinfoCompactUI;
import org.mesh4j.sync.mappings.DataSourceMapping;
import org.mesh4j.sync.mappings.EndpointMapping;
import org.mesh4j.sync.message.channel.sms.SmsEndpoint;
import org.mesh4j.sync.message.channel.sms.connection.InMemorySmsConnection;
import org.mesh4j.sync.message.channel.sms.core.SmsChannel;

public class EmulateReadyToSyncTask extends SwingWorker<Void, Void> {

	// MODEL VARIABLEs
	private EpiinfoCompactUI ui;
	private boolean okAnswer;
	
	// BUSINESS METHODS
	public EmulateReadyToSyncTask(EpiinfoCompactUI ui, boolean okAnswer){
		super();
		this.ui = ui;
		this.okAnswer = okAnswer;
	}
	
    public Void doInBackground() {
    	DataSourceMapping dataSource;
		if(okAnswer){
			dataSource = (DataSourceMapping)ui.getComboBoxMappingDataSource().getSelectedItem();
		} else {
			dataSource = new DataSourceMapping("undefined", "undefined", "undefined", "undefined");
		}
			
		EndpointMapping endpoint = (EndpointMapping)ui.getComboBoxEndpoint().getSelectedItem();

		SmsChannel foregroundChannel = (SmsChannel)ui.getSyncEngine().getChannel();
		InMemorySmsConnection smsConnection = (InMemorySmsConnection) foregroundChannel.getSmsConnection();
		
		String message = ReadyToSyncTask.makeQuestion(dataSource);
		smsConnection.receive(message, new SmsEndpoint(endpoint.getEndpoint()));
		return null;
    }
}
