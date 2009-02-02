package org.mesh4j.sync.ui.utils;

import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Timer;

import org.mesh4j.sync.IFilter;
import org.mesh4j.sync.mappings.DataSourceMapping;
import org.mesh4j.sync.mappings.EndpointMapping;
import org.mesh4j.sync.message.channel.sms.ISmsChannel;
import org.mesh4j.sync.message.channel.sms.ISmsReceiver;
import org.mesh4j.sync.message.channel.sms.SmsEndpoint;
import org.mesh4j.sync.ui.MeshCompactUI;
import org.mesh4j.sync.ui.tasks.ReadyToSyncResponseTask;
import org.mesh4j.sync.ui.tasks.ReadyToSyncTask;
import org.mesh4j.sync.ui.tasks.TestPhoneResponseTask;
import org.mesh4j.sync.ui.tasks.TestPhoneTask;
import org.mesh4j.sync.ui.translator.MeshCompactUITranslator;
import org.mesh4j.sync.utils.EndpointProvider;

public class ProcessCustomMessages implements ISmsReceiver {
	
	
	private final static Object SEMAPHORE = new Object();

	private final static IFilter<String> FILTER = new IFilter<String>(){		
		@Override public boolean applies(String message) {
			return ReadyToSyncTask.isQuestion(message) || ReadyToSyncTask.isAnswer(message) || TestPhoneTask.isTestPhoneMessage(message);
		}			
	};	
	
	// MODEL VARIABLES
	private MeshCompactUI ui;

	private boolean readyToSyncInProcess = false;
	private EndpointMapping readyToSyncEndpoint;
	private DataSourceMapping readyToSyncDataSource;
	
	private boolean phoneCompatibilityInProcess = false;
	private EndpointMapping phoneCompatibilityEndpoint;
	private String phoneCompatibilityId;
	
	
	public ProcessCustomMessages(MeshCompactUI ui) {

		super();
		this.ui = ui;
		
		((ISmsChannel)this.ui.getSyncEngine().getChannel()).getSmsConnection().registerMessageReceiver(FILTER, this);
	}


	// MODEL VARIABLES
	@Override
	public void receiveSms(SmsEndpoint endpoint, String message, Date date) {
		synchronized (SEMAPHORE) {
			
			if(ReadyToSyncTask.isQuestion(message)){
				String dataSourceAlias = ReadyToSyncTask.getDataSourceAlias(message);
				String dataSourceDescription = ReadyToSyncTask.getDataSourceDescription(message);
				
				boolean isDataSourceAvailable = this.ui.getSourceIdMapper().isDataSourceAvailable(dataSourceAlias);
				
				ReadyToSyncResponseTask responseTask = new ReadyToSyncResponseTask(ui, endpoint.getEndpointId(), dataSourceAlias, isDataSourceAvailable);
				responseTask.execute();
				
				String userName = ReadyToSyncTask.getQuestionUserName(message);
				EndpointMapping endpointMapping = EndpointProvider.createNewEndpointMappingIfAbsent(userName, endpoint.getEndpointId(), this.ui.getPropertiesProvider());
				if(endpointMapping != null){
					this.ui.notifyNewEndpointMapping(endpointMapping);
				}
				
				if(isDataSourceAvailable){
					this.ui.notifyReadyToSyncAnswerSent(dataSourceAlias, endpoint.getEndpointId());
				} else {
					this.ui.notifyNotAvailableDataSource(dataSourceAlias, dataSourceDescription, endpoint.getEndpointId());
				}
			}
			
			if(this.readyToSyncInProcess 
					&& this.readyToSyncEndpoint.getEndpoint().equals(endpoint.getEndpointId())){
				if(ReadyToSyncTask.isAnswerOk(message, this.readyToSyncDataSource.getAlias())){
					notifyEndpointIsReadyToSync();
				}
				
				if(ReadyToSyncTask.isAnswerNotOk(message, this.readyToSyncDataSource.getAlias())){
					notifyEndpointIsNotReadyToSync();
				}
			} 
	
			if(TestPhoneTask.isTestPhoneMessage(message)){
				if(this.phoneCompatibilityInProcess 
					&& TestPhoneTask.makeAnswer(this.phoneCompatibilityId, ui.getPropertiesProvider().getLoggedUserName()).equals(message)
					&& this.phoneCompatibilityEndpoint.getEndpoint().equals(endpoint.getEndpointId())){
					
						// is response
						this.resetPhoneCompatibility();			
						ui.getSyncSessionView().setOk(MeshCompactUITranslator.getMessagePhoneIsCompatible());
						ui.fullEnableAllButtons();
						ui.notifyOwnerNotWorking();
				} else {
					// if is a question -> response, if is a old response -> discard
					
					if(TestPhoneTask.isQuestion(message, ui.getPropertiesProvider().getLoggedUserName())){
						new TestPhoneResponseTask(this.ui, endpoint.getEndpointId(), message).execute();
					
						String userName = TestPhoneTask.getUserName(message);
						EndpointMapping endpointMapping = EndpointProvider.createNewEndpointMappingIfAbsent(userName, endpoint.getEndpointId(), this.ui.getPropertiesProvider());
						if(endpointMapping != null){
							this.ui.notifyNewEndpointMapping(endpointMapping);
						}
					}	
				}
			}		
		}
	}

	public void resetPhoneCompatibility() {
		synchronized (SEMAPHORE) {
			phoneCompatibilityInProcess = false;
			phoneCompatibilityEndpoint = null;
			phoneCompatibilityId = null;
		}
		
	}


	public boolean isPhoneCompatibilityInProcess() {
		return phoneCompatibilityInProcess;
	}
	
	public void notifyStartTestForPhoneCompatibility(EndpointMapping endpoint, String id){
		synchronized (SEMAPHORE) {
			this.phoneCompatibilityInProcess = true;
			this.phoneCompatibilityEndpoint = endpoint;
			this.phoneCompatibilityId = id;	
		
			ui.fullDisableAllButtons();
		
			String msg = MeshCompactUITranslator.getMessageTestingPhoneCompatibility();
			ui.getSyncSessionView().viewSession(null);
			ui.getSyncSessionView().setInProcess(msg);
			ui.notifyOwnerWorking();
		}
		
		Action errorAction = new AbstractAction(){

			private static final long serialVersionUID = 4028395273128514170L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(isPhoneCompatibilityInProcess()){
					resetPhoneCompatibility();
					
					String msg = MeshCompactUITranslator.getMessageTimeOutPhoneCompatibility();
					ui.getSyncSessionView().setError(msg);
					
					ui.fullEnableAllButtons();
					
					ui.notifyOwnerNotWorking();
				}
			}
		};
		new Timer(ui.getPropertiesProvider().getDefaultTestPhoneDelay(), errorAction).start();
	}
	
	public void notifyStartReadyToSync(EndpointMapping endpoint, DataSourceMapping dataSource){

		synchronized (SEMAPHORE) {
			
			this.readyToSyncInProcess = true;
			this.readyToSyncEndpoint = endpoint;
			this.readyToSyncDataSource = dataSource;
	
			ui.fullDisableAllButtons();
	
			String msg = MeshCompactUITranslator.getMessageProcessingReadyToSync(endpoint.getAlias(), dataSource.getAlias());
			ui.getSyncSessionView().viewSession(null);
			ui.getSyncSessionView().setInProcess(msg);
			
			ui.notifyOwnerWorking();
		}
		
		Action errorReadyToSync = new AbstractAction(){
			private static final long serialVersionUID = 4028395273128514170L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(readyToSyncInProcess){
					notifyEndpointIsNotReadyToSync();
				}
			}
		};
		new Timer(ui.getPropertiesProvider().getDefaultReadyToSyncDelay(), errorReadyToSync).start();
	}
	
	public void notifyEndpointIsReadyToSync(){
		synchronized (SEMAPHORE) {
			this.readyToSyncInProcess = false;
			
			ui.getSyncSessionView().setOk(MeshCompactUITranslator.getMessageEndpointIsReadyToSync(readyToSyncEndpoint.getAlias(), readyToSyncDataSource.getAlias()));
			this.readyToSyncEndpoint = null;
			this.readyToSyncDataSource = null;
			ui.fullEnableAllButtons();
			
			ui.notifyOwnerNotWorking();
		}
	}
	
	public void notifyEndpointIsNotReadyToSync(){
		synchronized (SEMAPHORE) {
			readyToSyncInProcess = false;
			
			String msg = MeshCompactUITranslator.getMessageEndpointIsNotReadyToSync(readyToSyncEndpoint.getAlias(), readyToSyncDataSource.getAlias());
			ui.getSyncSessionView().setError(msg);
			readyToSyncEndpoint = null;
			readyToSyncDataSource = null;
			ui.fullEnableAllButtons();
			
			ui.notifyOwnerNotWorking();
		}
	}


	public boolean isReadyToSyncInProcess() {
		return this.readyToSyncInProcess;
	}

}
