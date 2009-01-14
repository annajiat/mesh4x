package org.mesh4j.sync.epiinfo.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mesh4j.sync.IFilter;
import org.mesh4j.sync.adapters.feed.FeedSyncAdapterFactory;
import org.mesh4j.sync.adapters.http.HttpSyncAdapterFactory;
import org.mesh4j.sync.adapters.kml.KMLDOMLoaderFactory;
import org.mesh4j.sync.adapters.msaccess.MsAccessSyncAdapterFactory;
import org.mesh4j.sync.adapters.msexcel.MsExcelSyncAdapterFactory;
import org.mesh4j.sync.mappings.DataSourceMapping;
import org.mesh4j.sync.mappings.EndpointMapping;
import org.mesh4j.sync.mappings.SyncMode;
import org.mesh4j.sync.message.IMessageSyncAdapter;
import org.mesh4j.sync.message.MessageSyncEngine;
import org.mesh4j.sync.ui.tasks.CancelSyncTask;
import org.mesh4j.sync.ui.tasks.EmulateIncomingSyncTask;
import org.mesh4j.sync.ui.tasks.EmulateReadyToSyncTask;
import org.mesh4j.sync.ui.tasks.ReadyToSyncResponseTask;
import org.mesh4j.sync.ui.tasks.ReadyToSyncTask;
import org.mesh4j.sync.ui.tasks.SynchronizeTask;
import org.mesh4j.sync.ui.tasks.TestPhoneTask;
import org.mesh4j.sync.ui.translator.EpiInfoUITranslator;
import org.mesh4j.sync.utils.EpiinfoCompactConsoleNotification;
import org.mesh4j.sync.utils.SyncEngineUtil;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.swtdesigner.SwingResourceManager;

public class EpiinfoCompactUI {

	private final static Log Logger = LogFactory.getLog(EpiinfoCompactUI.class);
	
	// MODEL VARIABLES
	private JFrame frame;
	private JLabel imageStatus;
	private JButton buttonOpenLog;
	private JButton buttonConfiguration;
	private JTextArea textAreaStatus;
	private JLabel labelRemoteDeleted;
	private JLabel labelRemoteUpdated;
	private JLabel labelRemoteNew;
	private JLabel labelRemoteDataSource;
	private JLabel labelOut;
	private JLabel labelIn;
	private JLabel labelSyncType;
	private JLabel labelLocalDeleted;
	private JLabel labelLocalUpdated;
	private JLabel labelLocalNew;
	private JLabel labelLocalDataSource;
	private JPanel panelProgress;
	private JComboBox comboBoxSyncMode;
	private JComboBox comboBoxMappingDataSource;
	private JButton buttonSync;
	private JButton buttonReadyToSync;
	private JButton buttonTestPhone;
	private JComboBox comboBoxEndpoint;
	private JLabel labelSyncWith;
	private JPanel panelSync;
	
	private LogFrame logFrame;
	private ConfigurationFrame cfgFrame;	
	private EpiinfoCompactConsoleNotification consoleNotification;
	
	private MessageSyncEngine syncEngine;
	private boolean syncInProcess = false;
	
	private boolean readyToSyncInProcess = false;
	private EndpointMapping readyToSyncEndpoint;
	private DataSourceMapping readyToSyncDataSource;
	
	private boolean phoneCompatibilityInProcess = false;
	private EndpointMapping phoneCompatibilityEndpoint;
	private String phoneCompatibilityId;
	
	private int smsIn = 0;
	private int smsOut = 0;
	private int numberOfRemoteAddedItems = 0;
	private int numberOfRemoteUpdatedItems = 0;
	private int numberOfRemoteDeletedItems = 0;
	private int syncMinutes = 0;
			
	// BUSINESS METHODS
	
	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EpiinfoCompactUI window = new EpiinfoCompactUI();
					window.frame.pack();
					window.frame.setSize(window.frame.getPreferredSize());
					window.frame.setVisible(true);
				} catch (Exception e) {
					Logger.error(e.getMessage(), e);
				}
			}
		});
	}

	public EpiinfoCompactUI() throws Exception {
		this.createUI();
		
		IFilter<String> messageFilter = new IFilter<String>(){

			@Override
			public boolean applies(String message) {
				return ReadyToSyncTask.isQuestion(message) || TestPhoneTask.isQuestion(message);
			}
			
		};
		
		this.consoleNotification = new EpiinfoCompactConsoleNotification(logFrame, this, messageFilter);
		this.setReadyImageStatus();
		this.syncEngine = SyncEngineUtil.createSyncEngine(consoleNotification);
		this.startUpSyncEngine();
		this.startScheduler();	
	}
	
	private void startScheduler() {
		Action refreshStatus = new AbstractAction() {
			private static final long serialVersionUID = 6527495893765136292L;
			public void actionPerformed(ActionEvent e) {
				refresh();
		    }
		};

		// 1 minute
		new Timer(1 * 60 * 1000, refreshStatus).start();
	}

	protected void startUpSyncEngine() throws Exception {
		try{
			if(this.syncEngine == null){
				this.notifyStartUpError();
			} else {
				this.syncEngine.getChannel().startUp();		
			}
		} catch(Throwable e){
			this.notifyStartUpError();
			Logger.error(e.getMessage(), e);
		}
	}

	private void shutdownSyncEngine(){
		try{
			this.syncEngine.getChannel().shutdown();
		} catch(Throwable e){
			Logger.error(e.getMessage(), e);
		}
	}
	
	// Status
	public void setStatus(String status) {
		this.textAreaStatus.setText(status);
	}
	
	public void setError(String error){
		this.textAreaStatus.setForeground(new Color(255, 0, 0));
		this.textAreaStatus.setText(error);
	}
	
	public void notifyEndSync(boolean error) {
		this.syncInProcess = false;
		if(error){
			this.setStatus("Sync failed");
			this.setErrorImageStatus();
		} else {
			this.setStatus("Sync successfully");
			this.setEndSyncImageStatus();
		}
		
		this.buttonSync.setText("Sync now");
		this.enableAllButtons();		
	}
	
	public void updateRemoteDataSource(String sourceType) {
		this.labelRemoteDataSource.setIcon(SwingResourceManager.getIcon(EpiinfoCompactUI.class, getSourceImage(sourceType, true)));
	}
	
	private String getSourceImage(String sourceType, boolean remote) {
		if(FeedSyncAdapterFactory.SOURCE_TYPE.equals(sourceType)){
			return "/feedRSSDataSource.png";
		} else if (HttpSyncAdapterFactory.SOURCE_TYPE.equals(sourceType)){
			return "/httpDataSource.png";
		} else if (KMLDOMLoaderFactory.SOURCE_TYPE.equals(sourceType)){
			return "/kmlDataSource.png";
		} else if (MsAccessSyncAdapterFactory.SOURCE_TYPE.equals(sourceType)){
			if(remote){
				return "/msAccessDataSourceRemote.png";
			} else {
				return "/msAccessDataSource.png";
			}
		} else if (MsExcelSyncAdapterFactory.SOURCE_TYPE.equals(sourceType)){
			return "/msExcelDataSource.png";
		} else {
			return "/undefinedDataSource.png";
		}
	}

	public void notifyBeginSync(String sourceId, boolean sendChanges, boolean receiveChanges) {
		
		IMessageSyncAdapter adapter = this.syncEngine.getSource(sourceId);
		
		this.labelLocalDataSource.setIcon(SwingResourceManager.getIcon(EpiinfoCompactUI.class, getSourceImage(adapter.getSourceType(), false)));
		this.labelRemoteDataSource.setIcon(SwingResourceManager.getIcon(EpiinfoCompactUI.class, "/undefinedDataSource.png"));

		if(sendChanges && receiveChanges){
			this.labelSyncType.setIcon(SwingResourceManager.getIcon(EpiinfoCompactUI.class, "/2WaySync.png"));
		}else if(sendChanges){
			this.labelSyncType.setIcon(SwingResourceManager.getIcon(EpiinfoCompactUI.class, "/sendChangesOnly.png"));
		} else {
			this.labelSyncType.setIcon(SwingResourceManager.getIcon(EpiinfoCompactUI.class, "/receiveChangesOnly.png"));
		}
		this.labelLocalNew.setText("New: 0");
		this.labelLocalDeleted.setText("Deleted: 0");
		this.labelLocalUpdated.setText("Updated: 0");

		this.labelRemoteNew.setText("New: 0");
		this.labelRemoteDeleted.setText("Deleted: 0");
		this.labelRemoteUpdated.setText("Updated: 0");

		this.labelIn.setText("In: 0");
		this.labelOut.setText("Out: 0");
		
		this.smsIn = 0;
		this.smsOut = 0;
		this.numberOfRemoteAddedItems = 0;
		this.numberOfRemoteDeletedItems = 0;
		this.numberOfRemoteUpdatedItems = 0;
		this.syncMinutes = 0;
		
		this.syncInProcess = true;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		
		this.setStatus("Sync Started: " + dateFormat.format(new Date()));
		this.buttonSync.setText("Cancel Sync");	
		this.setInProcessImageStatus();
		this.logFrame.cleanLog();
		this.disableAllButtons();
	}
	
	public void notifyErrorSync(Throwable t)	{
		this.syncInProcess = false;
		this.setErrorImageStatus();
		this.logFrame.logError(t, EpiInfoUITranslator.getLabelFailed());
	}

	public void notifyBeginCancelSync()	{
		this.disableAllButtons();
		this.setInProcessImageStatus();
	}

	public void notifyEndCancelSync() {
		this.syncInProcess = false;
		this.setStatus("Cancel Sync successfully");
		
		this.buttonSync.setText("Sync now");
		this.setEndSyncImageStatus();
		this.enableAllButtons();		
	}
	
	public void notifyStartUpError()	{
		this.setStatus("Start up error, please check phone configuration and compatibility !!!");
		this.setErrorImageStatus();

		comboBoxEndpoint.setEnabled(false);
		comboBoxMappingDataSource.setEnabled(false);
		comboBoxSyncMode.setEnabled(false);
		
		buttonReadyToSync.setEnabled(false);
		buttonTestPhone.setEnabled(true);
		buttonSync.setEnabled(false); 
	}
	
	public void notifyStartTestForPhoneCompatibility(EndpointMapping endpoint, String id){
		this.phoneCompatibilityInProcess = true;
		this.phoneCompatibilityEndpoint = endpoint;
		this.phoneCompatibilityId = id;
		this.fullDisableAllButtons();
		this.setInProcessImageStatus();
		this.setStatus("Testing phone compatibility....");
		
		Action errorAction = new AbstractAction(){

			private static final long serialVersionUID = 4028395273128514170L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(phoneCompatibilityInProcess){
					phoneCompatibilityInProcess = false;
					setErrorImageStatus();
					setStatus("Phone compatibility error: Please check if the phone is plugged to the computa and try again...");
					phoneCompatibilityEndpoint = null;
					phoneCompatibilityId = null;
					fullEnableAllButtons();
				}
			}
		};
		new Timer(1 * 60 * 1000, errorAction).start();
	}
	
	public void notifyPhoneIsCompatible() {
		this.phoneCompatibilityInProcess = false;
		this.phoneCompatibilityEndpoint = null;
		this.phoneCompatibilityId = null;
		
		this.setStatus("Phone is compatible....");
		this.fullEnableAllButtons();
		this.setReadyImageStatus();
		this.disableAllButtons();
	}
	
	public void notifyStartReadyToSync(EndpointMapping endpoint, DataSourceMapping dataSource){
		this.setInProcessImageStatus();
		this.fullDisableAllButtons();
		this.setStatus("Processing: Is " + endpoint.getAlias() + " ready to sync " + dataSource.getAlias() + " ?");
		this.readyToSyncInProcess = true;
		this.readyToSyncEndpoint = endpoint;
		this.readyToSyncDataSource = dataSource;
		
		Action errorReadyToSync = new AbstractAction(){
			private static final long serialVersionUID = 4028395273128514170L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(readyToSyncInProcess){
					readyToSyncInProcess = false;
					setErrorImageStatus();
					setStatus(readyToSyncEndpoint.getAlias() + " is NOT ready to sync " + readyToSyncDataSource.getAlias());
					readyToSyncEndpoint = null;
					readyToSyncDataSource = null;
					fullEnableAllButtons();
				}
			}
		};
		new Timer(1 * 60 * 1000, errorReadyToSync).start();
	}
	
	public void notifyEndpointIsReadyToSync(){
		this.readyToSyncInProcess = false;
		this.setReadyImageStatus();
		this.setStatus(readyToSyncEndpoint.getAlias() + " is ready to sync " + readyToSyncDataSource.getAlias());
		this.readyToSyncEndpoint = null;
		this.readyToSyncDataSource = null;
		this.fullEnableAllButtons();
	}
	
	public void notifyReceiveMessage(String endpoint, String message, Date date) {
		
		if(ReadyToSyncTask.isQuestion(message)){
			String dataSourceAlias = ReadyToSyncTask.getDataSourceAlias(message);
			
			boolean isDataSourceAvailable = SyncEngineUtil.isDataSourceAvailable(dataSourceAlias);
			ReadyToSyncResponseTask responseTask = new ReadyToSyncResponseTask(this, endpoint, dataSourceAlias, isDataSourceAvailable);
			responseTask.execute();
		}
		
		if(this.readyToSyncInProcess 
				&& this.readyToSyncEndpoint.getEndpoint().equals(endpoint)
				&& ReadyToSyncTask.isAnswer(message, this.readyToSyncDataSource.getAlias())){
			this.notifyEndpointIsReadyToSync();
		} 
		
		if(this.phoneCompatibilityInProcess 
				&& this.phoneCompatibilityEndpoint.getEndpoint().equals(endpoint) 
				&& TestPhoneTask.makeAnswer(this.phoneCompatibilityId).equals(message)){
			this.notifyPhoneIsCompatible();
		} 		
	}
	
	// enable/Disable
	public void enableAllButtons() {
		this.comboBoxEndpoint.setEnabled(true);
		this.comboBoxMappingDataSource.setEnabled(true);
		this.comboBoxSyncMode.setEnabled(true);
		
		this.buttonReadyToSync.setEnabled(true);
		this.buttonTestPhone.setEnabled(true);		
	}
	
	public void disableAllButtons() {
		this.comboBoxEndpoint.setEnabled(false);
		this.comboBoxMappingDataSource.setEnabled(false);
		this.comboBoxSyncMode.setEnabled(false);
		
		this.buttonReadyToSync.setEnabled(false);
		this.buttonTestPhone.setEnabled(false);
	}
	
	public void fullEnableAllButtons() {
		enableAllButtons();
		this.buttonSync.setEnabled(true);		
	}
	
	public void fullDisableAllButtons() {
		this.disableAllButtons();
		this.buttonSync.setEnabled(false);
	}
	
	// Status images methods
	
	public void setErrorImageStatus() {
		this.imageStatus.setIcon(SwingResourceManager.getIcon(EpiinfoUI.class, "/error.png"));		
	}
	
	public void setInProcessImageStatus() {
		this.imageStatus.setIcon(SwingResourceManager.getIcon(EpiinfoUI.class, "/inProcess.gif"));
	}
	
	public void setEndSyncImageStatus() {
		this.imageStatus.setIcon(SwingResourceManager.getIcon(EpiinfoUI.class, "/ok.png"));		
	}
	
	public void setReadyImageStatus() {
		this.imageStatus.setIcon(null);	
	}
	
	// Detailed status
	
	public void increaseSmsIn() {
		this.smsIn = this.smsIn + 1;
		this.labelIn.setText("In: " + this.smsIn);
	}
	
	public void increaseSmsOut() {
		this.smsOut = this.smsOut + 1;
		this.labelOut.setText("Out: " + this.smsOut);
	}

	public void updateLocalStatus(int addTotal, int updateTotal, int deleteTotal) {
		this.labelLocalNew.setText("New: " + addTotal);
		this.labelLocalDeleted.setText("Deleted: " + deleteTotal);
		this.labelLocalUpdated.setText("Updated: " + updateTotal);		
	}
	
	public void updateRemoteStatus(int addTotal, int updateTotal, int deleteTotal) {
		if(addTotal > this.numberOfRemoteAddedItems || 
				updateTotal > this.numberOfRemoteUpdatedItems ||
				deleteTotal > this.numberOfRemoteDeletedItems){
			this.numberOfRemoteAddedItems = addTotal;
			this.numberOfRemoteDeletedItems = deleteTotal;
			this.numberOfRemoteUpdatedItems = updateTotal;
		
			this.labelRemoteNew.setText("New: " + addTotal);
			this.labelRemoteDeleted.setText("Deleted: " + deleteTotal);
			this.labelRemoteUpdated.setText("Updated: " + updateTotal);
		}
	}
	
	protected void refresh(){
		if (syncInProcess) {
			this.syncMinutes = this.syncMinutes + 1;
			String actualStatus = this.textAreaStatus.getText();
			int index = actualStatus.indexOf(" (");
			if(index > 0){
				actualStatus = actualStatus.substring(0, index);
			}
			
			StringBuffer sb = new StringBuffer();
			sb.append(actualStatus);
					
			int minutes = this.syncMinutes % 60;
			if(minutes > 0){
				sb.append(" (");
				int hrs = this.syncMinutes / 60;
				if(hrs > 0){
					hrs = hrs % 24;
					int days = hrs / 24;
					if(days > 0){
						sb.append(days);
						if(days > 1){
							sb.append(" days ");
						} else {
							sb.append(" day ");
						}
					}
					
					sb.append(hrs);
					if(hrs > 1){
						sb.append(" hours ");
					} else {
						sb.append(" hour ");
					}
				}
				
				sb.append(minutes);
				if(minutes > 1){
					sb.append(" minutes ");
				} else {
					sb.append(" minute ");
				}				
				sb.append("ago)");
			}		
		    setStatus(sb.toString());
		}
	}
	
	// UI Design
	private void createUI() {
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.WHITE);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		WindowAdapter windowAdapter = new WindowAdapter() {
			public void windowClosing(final WindowEvent e) {
				if(syncInProcess){
					Object[] options = {"Cancel sync and Close", "Continues working"};
					int n = JOptionPane.showOptionDialog(frame,
						"You are syncronization "+ getSelectedDataSource()+" with " + getSelectedEndpoint() +". What would you like to do?",
						"Close Application",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,     //do not use a custom Icon
						options,  //the titles of buttons
						options[0]); //default button title
					if(n == 0){
						CancelSyncTask cancelSync = new CancelSyncTask(EpiinfoCompactUI.this);
						cancelSync.execute();
						
						EpiinfoCompactUI.this.frame.setVisible(false);
						EpiinfoCompactUI.this.frame.dispose();

					} else {
						return;
					}
				} else {
					EpiinfoCompactUI.this.frame.setVisible(false);
					EpiinfoCompactUI.this.frame.dispose();
				}
			}
			
			public void windowClosed(final WindowEvent e) {
				shutdownSyncEngine();
			}
		};
		
		frame.addWindowListener(windowAdapter);
		frame.setIconImage(SwingResourceManager.getImage(EpiinfoCompactUI.class, "/cdc.gif"));
		frame.getContentPane().setLayout(new FormLayout(
			new ColumnSpec[] {
				ColumnSpec.decode("20dlu"),
				ColumnSpec.decode("259dlu"),
				ColumnSpec.decode("10dlu")},
			new RowSpec[] {
				RowSpec.decode("6dlu"),
				RowSpec.decode("89dlu"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("115dlu"),
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC}));
		frame.setResizable(false);
		frame.setTitle(EpiInfoUITranslator.getTitle());
		frame.setBounds(100, 100, 588, 446);
		frame.getContentPane().add(getPanelSync(), new CellConstraints(2, 2));
		frame.getContentPane().add(getPanelProgress(), new CellConstraints(2, 4, CellConstraints.FILL, CellConstraints.FILL));

		final JPanel panelStatus = new JPanel();
		panelStatus.setBackground(Color.WHITE);
		panelStatus.setLayout(new FormLayout(
			"259dlu",
			"11dlu, 14dlu"));
		frame.getContentPane().add(panelStatus, new CellConstraints(2, 5));

		final JPanel panelStatusButtons = new JPanel();
		panelStatusButtons.setBackground(Color.WHITE);
		panelStatusButtons.setLayout(new FormLayout(
			new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC},
			new RowSpec[] {
				RowSpec.decode("14dlu")}));
		panelStatus.add(panelStatusButtons, new CellConstraints(1, 2));
		panelStatusButtons.add(getButtonOpenLog(), new CellConstraints(1, 1, CellConstraints.CENTER, CellConstraints.FILL));
		panelStatusButtons.add(getButtonConfiguration(), new CellConstraints(3, 1, CellConstraints.CENTER, CellConstraints.FILL));

		final JButton emulateSyncButton = new JButton();
		emulateSyncButton.setFont(new Font("Calibri", Font.PLAIN, 10));
		emulateSyncButton.setContentAreaFilled(false);
		emulateSyncButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		emulateSyncButton.setBorderPainted(false);
		emulateSyncButton.setName("");
		emulateSyncButton.setOpaque(false);
		emulateSyncButton.setText("EmulateSync");
		ActionListener emulateSyncActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				EmulateIncomingSyncTask task = new EmulateIncomingSyncTask(EpiinfoCompactUI.this);
				task.execute();
			}
		};	
		emulateSyncButton.addActionListener(emulateSyncActionListener);
		
		panelStatusButtons.add(emulateSyncButton, new CellConstraints(5, 1, CellConstraints.CENTER, CellConstraints.FILL));

		final JButton emulatereadtyosyncButton = new JButton();
		emulatereadtyosyncButton.setContentAreaFilled(false);
		emulatereadtyosyncButton.setBorderPainted(false);
		emulatereadtyosyncButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		emulatereadtyosyncButton.setFont(new Font("Calibri", Font.PLAIN, 10));
		emulatereadtyosyncButton.setOpaque(false);
		emulatereadtyosyncButton.setText("Emulate Ready");
		ActionListener emulateReadyToSyncActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				EmulateReadyToSyncTask task = new EmulateReadyToSyncTask(EpiinfoCompactUI.this);
				task.execute();
			}
		};	
		emulatereadtyosyncButton.addActionListener(emulateReadyToSyncActionListener);
		
		panelStatusButtons.add(emulatereadtyosyncButton, new CellConstraints(7, 1, CellConstraints.CENTER, CellConstraints.FILL));
		
		panelStatus.add(getTextAreaStatus(), new CellConstraints(1, 1, CellConstraints.FILL, CellConstraints.FILL));
		
		logFrame = new LogFrame();
		cfgFrame = new ConfigurationFrame();

	}

	protected JPanel getPanelSync() {
		if (panelSync == null) {
			panelSync = new JPanel();
			panelSync.setBackground(Color.WHITE);
			panelSync.setLayout(new FormLayout(
				new ColumnSpec[] {
					FormFactory.DEFAULT_COLSPEC,
					ColumnSpec.decode("10dlu"),
					ColumnSpec.decode("97dlu"),
					ColumnSpec.decode("27dlu"),
					ColumnSpec.decode("70dlu")},
				new RowSpec[] {
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("15dlu"),
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					RowSpec.decode("11dlu"),
					FormFactory.DEFAULT_ROWSPEC}));
			panelSync.add(getLabelSyncWith(), new CellConstraints());
			panelSync.add(getComboBoxEndpoint(), new CellConstraints(3, 1));
			panelSync.add(getButtonReadyToSync(), new CellConstraints(5, 3, CellConstraints.DEFAULT, CellConstraints.CENTER));
			panelSync.add(getButtonSync(), new CellConstraints(5, 5));

			final JPanel panel = new JPanel();
			panel.setBackground(Color.WHITE);
			panel.setLayout(new FormLayout(
				new ColumnSpec[] {
					ColumnSpec.decode("75dlu"),
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("98dlu")},
				new RowSpec[] {
					FormFactory.DEFAULT_ROWSPEC}));
			panelSync.add(panel, new CellConstraints(1, 7, 5, 1));
			panel.add(getComboBoxMappingDataSource(), new CellConstraints());
			panel.add(getComboBoxSyncMode(), new CellConstraints(3, 1));

			final JPanel panel_1 = new JPanel();
			panel_1.setLayout(new FormLayout(
				"70dlu",
				"14dlu"));
			panelSync.add(panel_1, new CellConstraints(5, 1));
			panel_1.add(getButtonTestPhone(), new CellConstraints());
		}
		return panelSync;
	}

	protected JLabel getLabelSyncWith() {
		if (labelSyncWith == null) {
			labelSyncWith = new JLabel();
			labelSyncWith.setFont(new Font("Calibri", Font.BOLD, 14));
			labelSyncWith.setText("Sync With:");
		}
		return labelSyncWith;
	}

	public JComboBox getComboBoxEndpoint() {
		if (comboBoxEndpoint == null) {
			comboBoxEndpoint = new JComboBox();
			comboBoxEndpoint.setFont(new Font("Calibri", Font.PLAIN, 12));
			comboBoxEndpoint.setModel(new DefaultComboBoxModel(SyncEngineUtil.getEndpointMappings()));
		}
		return comboBoxEndpoint;
	}

	protected JButton getButtonTestPhone() {
		if (buttonTestPhone == null) {
			buttonTestPhone = new JButton();
			buttonTestPhone.setFont(new Font("Calibri", Font.BOLD, 12));
			buttonTestPhone.setBackground(UIManager.getColor("Button.background"));
			buttonTestPhone.setText("Test Phone");
			
			ActionListener testPhoneActionListener = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					TestPhoneTask task = new TestPhoneTask(EpiinfoCompactUI.this);
					task.execute();
				}
			};	
			buttonTestPhone.addActionListener(testPhoneActionListener);
		} 
		return buttonTestPhone;
	}

	protected JButton getButtonReadyToSync() {
		if (buttonReadyToSync == null) {
			buttonReadyToSync = new JButton();
			buttonReadyToSync.setFont(new Font("Calibri", Font.BOLD, 12));
			buttonReadyToSync.setText("Ready To Sync ?");
			
			ActionListener readyToSyncActionListener = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					SwingWorker<Void, Void> task = new ReadyToSyncTask(EpiinfoCompactUI.this);
					task.execute();
				}
			};	
			buttonReadyToSync.addActionListener(readyToSyncActionListener);
		}
		return buttonReadyToSync;
	}

	protected JButton getButtonSync() {
		if (buttonSync == null) {
			buttonSync = new JButton();
			buttonSync.setFont(new Font("Arial", Font.PLAIN, 16));
			buttonSync.setText("Sync Now");
			
			ActionListener synchronizeActionListener = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					SwingWorker<Void, Void> task = syncInProcess ? new CancelSyncTask(EpiinfoCompactUI.this) : new SynchronizeTask(EpiinfoCompactUI.this);
					task.execute();
				}
			};	
			buttonSync.addActionListener(synchronizeActionListener);
		}
		return buttonSync;
	}


	public JComboBox getComboBoxMappingDataSource() {
		if (comboBoxMappingDataSource == null) {
			comboBoxMappingDataSource = new JComboBox();
			comboBoxMappingDataSource.setFont(new Font("Calibri", Font.PLAIN, 12));
			comboBoxMappingDataSource.setModel(new DefaultComboBoxModel(SyncEngineUtil.getDataSourceMappings()));
		}
		return comboBoxMappingDataSource;
	}

	public JComboBox getComboBoxSyncMode() {
		if (comboBoxSyncMode == null) {
			comboBoxSyncMode = new JComboBox();
			comboBoxSyncMode.setFont(new Font("Calibri", Font.PLAIN, 12));

			DefaultComboBoxModel syncTypesTableModel = new DefaultComboBoxModel(new SyncMode[]{SyncMode.SendAndReceiveChanges, SyncMode.SendChangesOnly, SyncMode.ReceiveChangesOnly});
			comboBoxSyncMode.setModel(syncTypesTableModel);
		}
		return comboBoxSyncMode;
	}

	protected JPanel getPanelProgress() {
		if (panelProgress == null) {
			panelProgress = new JPanel();
			panelProgress.setBackground(Color.WHITE);
			panelProgress.setLayout(new FormLayout(
				new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("52dlu"),
					ColumnSpec.decode("140dlu"),
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("56dlu")},
				new RowSpec[] {
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					RowSpec.decode("17dlu"),
					FormFactory.RELATED_GAP_ROWSPEC}));
			panelProgress.add(getLabelLocalDataSource(), new CellConstraints(2, 1, CellConstraints.FILL, CellConstraints.FILL));
			panelProgress.add(getLabelLocalNew(), new CellConstraints(2, 3));
			panelProgress.add(getLabelLocalUpdated(), new CellConstraints(2, 4));
			panelProgress.add(getLabelLocalDeleted(), new CellConstraints(2, 5, CellConstraints.DEFAULT, CellConstraints.TOP));
			panelProgress.add(getLabelSyncType(), new CellConstraints(3, 1, CellConstraints.CENTER, CellConstraints.FILL));
			panelProgress.add(getLabelRemoteDataSource(), new CellConstraints(5, 1, CellConstraints.FILL, CellConstraints.FILL));
			panelProgress.add(getLabelRemoteNew(), new CellConstraints(5, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
			panelProgress.add(getLabelRemoteUpdated(), new CellConstraints(5, 4, CellConstraints.LEFT, CellConstraints.DEFAULT));
			panelProgress.add(getLabelRemoteDeleted(), new CellConstraints(5, 5, CellConstraints.LEFT, CellConstraints.TOP));

			final JPanel panelInOut = new JPanel();
			panelInOut.setBackground(Color.WHITE);
			panelInOut.setLayout(new FormLayout(
				"24dlu, 56dlu, 28dlu, 18dlu",
				"19dlu, 18dlu"));
			panelProgress.add(panelInOut, new CellConstraints(3, 3, 1, 3));

			final JLabel imageInOut = new JLabel();
			imageInOut.setIcon(SwingResourceManager.getIcon(EpiinfoCompactUI.class, "/inOut.png"));
			imageInOut.setText("");
			panelInOut.add(imageInOut, new CellConstraints(2, 1, 1, 2, CellConstraints.RIGHT, CellConstraints.DEFAULT));
			panelInOut.add(getLabelIn(), new CellConstraints(3, 1, CellConstraints.LEFT, CellConstraints.BOTTOM));
			panelInOut.add(getLabelOut(), new CellConstraints(3, 2, CellConstraints.LEFT, CellConstraints.DEFAULT));
			panelInOut.add(getImageStatus(), new CellConstraints(4, 1, CellConstraints.FILL, CellConstraints.FILL));
		}
		return panelProgress;
	}

	protected JLabel getLabelLocalDataSource() {
		if (labelLocalDataSource == null) {
			labelLocalDataSource = new JLabel();
			labelLocalDataSource.setIcon(SwingResourceManager.getIcon(EpiinfoCompactUI.class, "/undefinedDataSource.png"));
			labelLocalDataSource.setText("");
		}
		return labelLocalDataSource;
	}

	protected JLabel getLabelLocalNew() {
		if (labelLocalNew == null) {
			labelLocalNew = new JLabel();
			labelLocalNew.setFont(new Font("Calibri", Font.BOLD, 12));
			labelLocalNew.setText("New: ");
		}
		return labelLocalNew;
	}

	protected JLabel getLabelLocalUpdated() {
		if (labelLocalUpdated == null) {
			labelLocalUpdated = new JLabel();
			labelLocalUpdated.setFont(new Font("Calibri", Font.BOLD, 12));
			labelLocalUpdated.setText("Updated: ");
		}
		return labelLocalUpdated;
	}

	protected JLabel getLabelLocalDeleted() {
		if (labelLocalDeleted == null) {
			labelLocalDeleted = new JLabel();
			labelLocalDeleted.setFont(new Font("Calibri", Font.BOLD, 12));
			labelLocalDeleted.setText("Deleted: ");
		}
		return labelLocalDeleted;
	}

	protected JLabel getLabelSyncType() {
		if (labelSyncType == null) {
			labelSyncType = new JLabel();
			labelSyncType.setIcon(SwingResourceManager.getIcon(EpiinfoCompactUI.class, "/2WaySync.png"));
			labelSyncType.setText("");
		}
		return labelSyncType;
	}

	protected JLabel getLabelIn() {
		if (labelIn == null) {
			labelIn = new JLabel();
			labelIn.setFont(new Font("Calibri", Font.BOLD, 12));
			labelIn.setText("In: ");
		}
		return labelIn;
	}

	protected JLabel getLabelOut() {
		if (labelOut == null) {
			labelOut = new JLabel();
			labelOut.setFont(new Font("Calibri", Font.BOLD, 12));
			labelOut.setText("Out: ");
		}
		return labelOut;
	}

	protected JLabel getLabelRemoteDataSource() {
		if (labelRemoteDataSource == null) {
			labelRemoteDataSource = new JLabel();
			labelRemoteDataSource.setIcon(SwingResourceManager.getIcon(EpiinfoCompactUI.class, "/undefinedDataSource.png"));
			labelRemoteDataSource.setText("");
		}
		return labelRemoteDataSource;
	}

	protected JLabel getLabelRemoteNew() {
		if (labelRemoteNew == null) {
			labelRemoteNew = new JLabel();
			labelRemoteNew.setFont(new Font("Calibri", Font.BOLD, 12));
			labelRemoteNew.setText("New: ");
		}
		return labelRemoteNew;
	}

	protected JLabel getLabelRemoteUpdated() {
		if (labelRemoteUpdated == null) {
			labelRemoteUpdated = new JLabel();
			labelRemoteUpdated.setFont(new Font("Calibri", Font.BOLD, 12));
			labelRemoteUpdated.setText("Updated: ");
		}
		return labelRemoteUpdated;
	}

	protected JLabel getLabelRemoteDeleted() {
		if (labelRemoteDeleted == null) {
			labelRemoteDeleted = new JLabel();
			labelRemoteDeleted.setFont(new Font("Calibri", Font.BOLD, 12));
			labelRemoteDeleted.setText("Deleted: ");
		}
		return labelRemoteDeleted;
	}


	protected JButton getButtonOpenLog() {
		if (buttonOpenLog == null) {
			buttonOpenLog = new JButton();
			Font font = new Font("Calibri", Font.PLAIN, 10);
			buttonOpenLog.setFont(font);
			buttonOpenLog.setOpaque(false);
			buttonOpenLog.setContentAreaFilled(false);
			buttonOpenLog.setBorderPainted(false);
			buttonOpenLog.setBorder(new EmptyBorder(0, 0, 0, 0));
			buttonOpenLog.setText("Open Log Windows");
			
			buttonOpenLog.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(logFrame.isVisible()){
						logFrame.toFront();
					} else {
						logFrame.pack();
						logFrame.setVisible(true);
					}
				}
			});
		}
		return buttonOpenLog;
	}
	
	protected JButton getButtonConfiguration() {
		if (buttonConfiguration == null) {
			buttonConfiguration = new JButton();
			buttonConfiguration.setContentAreaFilled(false);
			buttonConfiguration.setFont(new Font("Calibri", Font.PLAIN, 10));
			buttonConfiguration.setOpaque(false);
			buttonConfiguration.setBorderPainted(false);
			buttonConfiguration.setBorder(new EmptyBorder(0, 0, 0, 0));
			buttonConfiguration.setText("Configuration");
			
			buttonConfiguration.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(cfgFrame.isVisible()){
						cfgFrame.toFront();
					} else {
						cfgFrame.pack();
						cfgFrame.setVisible(true);
					}
				}
			});
		}
		return buttonConfiguration;
	}

	protected JLabel getImageStatus() {
		if (imageStatus == null) {
			imageStatus = new JLabel();
			imageStatus.setIcon(SwingResourceManager.getIcon(EpiinfoCompactUI.class, "/error.png"));
			imageStatus.setText("");
		}
		return imageStatus;
	}
	
	private JTextArea getTextAreaStatus(){
		if (textAreaStatus == null) {
			textAreaStatus = new JTextArea();
			textAreaStatus.setFont(new Font("Calibri", Font.BOLD, 12));
			textAreaStatus.setLineWrap(true);
			textAreaStatus.setWrapStyleWord(true);
			textAreaStatus.setOpaque(true);
			textAreaStatus.setEditable(false);
			textAreaStatus.setText("Welcome to Epi Info Data Exchange !!!!");
		}
		return textAreaStatus;
		
	}

	public Component getFrame() {
		return this.frame;
	}

	public MessageSyncEngine getSyncEngine() {
		return this.syncEngine;
	}
	
	public EpiinfoCompactConsoleNotification getConsoleNotification() {
		return this.consoleNotification;
	}

	public DataSourceMapping getSelectedDataSource(){
		return (DataSourceMapping)this.getComboBoxMappingDataSource().getSelectedItem();
	}
	
	public EndpointMapping getSelectedEndpoint(){
		return (EndpointMapping)this.getComboBoxEndpoint().getSelectedItem();
	}
}