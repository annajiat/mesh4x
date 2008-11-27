package org.mesh4j.sync.epiinfo.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mesh4j.sync.adapters.msaccess.MsAccessHelper;
import org.mesh4j.sync.message.MessageSyncEngine;
import org.mesh4j.sync.message.channel.sms.connection.smslib.Modem;
import org.mesh4j.sync.message.channel.sms.connection.smslib.ModemHelper;
import org.mesh4j.sync.message.core.NonMessageEncoding;
import org.mesh4j.sync.message.encoding.IMessageEncoding;
import org.mesh4j.sync.model.Item;
import org.mesh4j.sync.properties.PropertiesProvider;
import org.mesh4j.sync.security.IIdentityProvider;
import org.mesh4j.sync.security.NullIdentityProvider;
import org.mesh4j.sync.ui.translator.EpiInfoUITranslator;
import org.mesh4j.sync.utils.EpiInfoConsoleNotification;
import org.mesh4j.sync.utils.FileNameResolver;
import org.mesh4j.sync.utils.SyncEngineUtil;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.swtdesigner.FocusTraversalOnArray;
import com.swtdesigner.SwingResourceManager;

public class EpiinfoUI{

	private final static Log Logger = LogFactory.getLog(EpiinfoUI.class);

	// CONSTANTS
	private final static int SYNCHRONIZE_HTTP = -1;
	private final static int SYNCHRONIZE_SMS = 0;
	private final static int CANCEL_SYNC = 1;
	private final static int ADD_DATA_SOURCE = 2;
	protected static final int DISCOVERY_MODEMS = 3;
	
	// MODEL VARIABLES
	private JFrame frame;
	private JComboBox comboSMSDevice;
	private JTextField textFieldPhoneNumber;
	private JTextField textFieldDataSource;
	private JComboBox comboTables;
	private JTextArea textAreaConsole;
	private JButton buttonSynchronize;
	private JButton buttonCancel;
	private JButton buttonClean;
	private JScrollPane scrollPaneConsole;
	private JTextArea textAreaStatus;
	private JButton buttonModemDiscovery;
	private JButton buttonAddDataSource;
	private JButton buttonOpenFileDataSource;
	private ButtonGroup buttonGroup = new ButtonGroup();	
	private JTextField textFieldURL;
	private JLabel labelUrl;
	private JLabel labelSMSDevice;
	private JLabel labelPhoneNumber;
	private JRadioButton radioEndpointSMS;
	private JRadioButton radioEndpointHTTP;
	
	private EpiInfoConsoleNotification consoleNotification;
	
	private Modem modem = null;
	private FileNameResolver fileNameResolver;
	private MessageSyncEngine syncEngine;
	private boolean emulate = false;
	private int channel = SYNCHRONIZE_HTTP;
	
	private IIdentityProvider identityProvider = NullIdentityProvider.INSTANCE;
	private String baseDirectory;
	private int senderDelay;
	private int receiverDelay;
	private int readDelay;
	private int maxMessageLenght;
	private int channelDelay;
	private IMessageEncoding messageEncoding;
	private String portName;
	private int baudRate;
	private String defaultPhoneNumber;
	private String defaultDataSource;
	private String defaultTableName;
	private String defaultURL;
	
	// BUSINESS METHODS
	
	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EpiinfoUI window = new EpiinfoUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					Logger.error(e.getMessage(), e);
				}
			}
		});
	}

	public EpiinfoUI() throws Exception {
		this.initializeProperties();
		this.initializeModem();
		this.createUI();
		this.consoleNotification = new EpiInfoConsoleNotification(this.textAreaConsole, this.textAreaStatus);
		this.initializeSyncEngine();
	}

	private void initializeModem() {
		if(portName.length() > 0 && baudRate > 0){
			modem = ModemHelper.getModem(portName, baudRate);
		}
	}

	private void initializeProperties() throws Exception {
		PropertiesProvider propertiesProvider = new PropertiesProvider();
		
		this.baseDirectory = propertiesProvider.getBaseDirectory();
		this.senderDelay = propertiesProvider.getInt("default.sms.sender.delay");
		this.receiverDelay = propertiesProvider.getInt("default.sms.receiver.delay");
		this.readDelay = propertiesProvider.getInt("default.sms.read.delay");
		this.maxMessageLenght = propertiesProvider.getInt("default.sms.max.message.lenght");
		this.channelDelay = propertiesProvider.getInt("default.sms.channel.delay");
		this.identityProvider = propertiesProvider.getIdentityProvider();
		this.messageEncoding = (IMessageEncoding) propertiesProvider.getInstance("default.sms.compress.method", NonMessageEncoding.INSTANCE);
		this.portName = propertiesProvider.getString("default.sms.port");
		this.baudRate = propertiesProvider.getInt("default.sms.baud.rate");
		this.defaultPhoneNumber = propertiesProvider.getDefaultPhoneNumber();
		this.defaultDataSource = propertiesProvider.getDefaultDataSource();
		this.defaultTableName = propertiesProvider.getDefaultTable();
		this.defaultURL = propertiesProvider.getString("default.url");
	}
	
	private void initializeSyncEngine() throws Exception {
		if(modem != null){
			String modemDirectory = baseDirectory+"/"+modem.toString()+"/";
			this.fileNameResolver = new FileNameResolver(modemDirectory+"myFiles.properties");
			this.syncEngine = SyncEngineUtil.createSyncEngine(fileNameResolver, modem, baseDirectory, senderDelay, receiverDelay, readDelay, maxMessageLenght, channelDelay,
				identityProvider, messageEncoding, consoleNotification, consoleNotification);  
		}
				
		if(this.syncEngine == null){
			String emulatorDirectory = baseDirectory+"/demo/";
			this.fileNameResolver = new FileNameResolver(emulatorDirectory+"myFiles.properties");
			this.syncEngine = SyncEngineUtil.createEmulator(fileNameResolver, consoleNotification, consoleNotification, "demo", messageEncoding, identityProvider, baseDirectory, senderDelay, receiverDelay, readDelay, channelDelay, maxMessageLenght);
			this.emulate = true;
		}
	}

	private void createUI() {
		frame = new JFrame();
		
		WindowAdapter windowAdapter = new WindowAdapter() {
			public void windowClosed(final WindowEvent e) {
				shutdown();
			}
		};
		
		frame.addWindowListener(windowAdapter);
		frame.setIconImage(SwingResourceManager.getImage(EpiinfoUI.class, "/cdc.gif"));
		frame.getContentPane().setLayout(new FormLayout(
			new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("407dlu"),
				FormFactory.RELATED_GAP_COLSPEC},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("108dlu"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("153dlu"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("94dlu"),
				FormFactory.RELATED_GAP_ROWSPEC}));
		frame.setResizable(false);
		frame.setTitle(EpiInfoUITranslator.getTitle());
		frame.setBounds(100, 100, 834, 773);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JPanel panelCommunications = new JPanel();
		panelCommunications.setFocusCycleRoot(true);
		panelCommunications.setBorder(new TitledBorder(new BevelBorder(BevelBorder.RAISED), EpiInfoUITranslator.getGroupCommunications(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
		panelCommunications.setLayout(new FormLayout(
			new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("44dlu"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("305dlu"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("36dlu"),
				FormFactory.RELATED_GAP_COLSPEC},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("12dlu"),
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				RowSpec.decode("9dlu"),
				RowSpec.decode("11dlu"),
				RowSpec.decode("14dlu"),
				FormFactory.RELATED_GAP_ROWSPEC}));
		frame.getContentPane().add(panelCommunications, new CellConstraints(2, 2, CellConstraints.FILL, CellConstraints.FILL));

		textFieldPhoneNumber = new JTextField();
		textFieldPhoneNumber.setEnabled(false);
		textFieldPhoneNumber.setToolTipText(EpiInfoUITranslator.getToolTipPhoneNumber());
		panelCommunications.add(textFieldPhoneNumber, new CellConstraints(4, 5, 3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
		textFieldPhoneNumber.setText(this.defaultPhoneNumber);
		
		labelSMSDevice = DefaultComponentFactory.getInstance().createLabel("Device");
		labelSMSDevice.setEnabled(false);
		panelCommunications.add(labelSMSDevice, new CellConstraints(2, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));

		labelPhoneNumber = DefaultComponentFactory.getInstance().createLabel(EpiInfoUITranslator.getLabelPhoneNumber());
		labelPhoneNumber.setEnabled(false);
		panelCommunications.add(labelPhoneNumber, new CellConstraints(2, 5, CellConstraints.RIGHT, CellConstraints.DEFAULT));

		comboSMSDevice = new JComboBox();
		comboSMSDevice.setEnabled(false);
		comboSMSDevice.setModel(modem == null ? new DefaultComboBoxModel(new String[]{"demo"}) : new DefaultComboBoxModel(new String[]{modem.toString()}));
		panelCommunications.add(comboSMSDevice, new CellConstraints(4, 3, CellConstraints.FILL, CellConstraints.DEFAULT));

		ActionListener modemDiscoveryActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Task task = new Task(DISCOVERY_MODEMS);
				task.execute();
			}
		};	
		
		buttonModemDiscovery = new JButton();
		buttonModemDiscovery.setText("?");
		buttonModemDiscovery.setEnabled(false);
		buttonModemDiscovery.addActionListener(modemDiscoveryActionListener);
		panelCommunications.add(buttonModemDiscovery, new CellConstraints(6, 3, CellConstraints.FILL, CellConstraints.FILL));

		labelUrl = new JLabel();
		labelUrl.setText("URL");
		panelCommunications.add(labelUrl, new CellConstraints(2, 8, CellConstraints.RIGHT, CellConstraints.DEFAULT));

		textFieldURL = new JTextField();
		textFieldURL.setText(this.defaultURL);
		panelCommunications.add(textFieldURL, new CellConstraints(4, 8, 3, 1));

		ActionListener channelSMSActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				textFieldURL.setEnabled(false);
				labelUrl.setEnabled(false);
				
				textFieldPhoneNumber.setEnabled(true);
				comboSMSDevice.setEnabled(true);
				buttonModemDiscovery.setEnabled(true);
				labelSMSDevice.setEnabled(true);
				labelPhoneNumber.setEnabled(true);
				
				channel = SYNCHRONIZE_SMS;
			}
		};
		
		radioEndpointSMS = new JRadioButton();
		buttonGroup.add(radioEndpointSMS);
		radioEndpointSMS.setText("SMS");
		radioEndpointSMS.addActionListener(channelSMSActionListener);
		panelCommunications.add(radioEndpointSMS, new CellConstraints(2, 2));

		ActionListener channelHTTPActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				textFieldURL.setEnabled(true);
				labelUrl.setEnabled(true);
				
				textFieldPhoneNumber.setEnabled(false);
				comboSMSDevice.setEnabled(false);
				buttonModemDiscovery.setEnabled(false);
				labelSMSDevice.setEnabled(false);
				labelPhoneNumber.setEnabled(false);
				 
				channel = SYNCHRONIZE_HTTP;
			}
		};
		
		radioEndpointHTTP = new JRadioButton();
		buttonGroup.add(radioEndpointHTTP);
		radioEndpointHTTP.setText("Web");
		radioEndpointHTTP.setSelected(true);
		radioEndpointHTTP.addActionListener(channelHTTPActionListener);

		panelCommunications.add(radioEndpointHTTP, new CellConstraints(2, 7));
		panelCommunications.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] {labelSMSDevice, labelPhoneNumber, textFieldPhoneNumber, buttonSynchronize}));

		final JPanel panelDataSource = new JPanel();
		panelDataSource.setBorder(new TitledBorder(new BevelBorder(BevelBorder.RAISED), EpiInfoUITranslator.getLabelDataSource(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
		panelDataSource.setLayout(new FormLayout(
			new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("335dlu"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("35dlu"),
				FormFactory.RELATED_GAP_COLSPEC},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC}));
		frame.getContentPane().add(panelDataSource, new CellConstraints(2, 4, CellConstraints.FILL, CellConstraints.FILL));

		textFieldDataSource = new JTextField();
		textFieldDataSource.setToolTipText(EpiInfoUITranslator.getToolTipDataSource());
		textFieldDataSource.setText(this.defaultDataSource);
		panelDataSource.add(textFieldDataSource, new CellConstraints(2, 2, 3, 1, CellConstraints.FILL, CellConstraints.FILL));

		ActionListener fileChooserFileActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String selectedFileName = openFileDialog(textFieldDataSource.getText());
				if(selectedFileName != null){
					textFieldDataSource.setText(selectedFileName);
				}
			}
		};
		
		buttonOpenFileDataSource = new JButton();
		buttonOpenFileDataSource.setToolTipText(EpiInfoUITranslator.getToolTipFileChooser());
		buttonOpenFileDataSource.setText(EpiInfoUITranslator.getLabelFileChooser());
		buttonOpenFileDataSource.addActionListener(fileChooserFileActionListener);
		panelDataSource.add(buttonOpenFileDataSource, new CellConstraints(6, 2, CellConstraints.FILL, CellConstraints.FILL));

		final JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new FormLayout(
			new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC},
			new RowSpec[] {
				}));
		panelDataSource.add(panelButtons, new CellConstraints(2, 6, 5, 1));

		final JLabel labelTable = new JLabel();
		labelTable.setText(EpiInfoUITranslator.getLabelTable());
		panelDataSource.add(labelTable, new CellConstraints(2, 4));

		comboTables = new JComboBox();
		comboTables.setModel(this.getDataSourceTableModel());
		panelDataSource.add(comboTables, new CellConstraints(4, 4));

		ActionListener addDataSourceActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Task task = new Task(ADD_DATA_SOURCE);
				task.execute();
			}
		};	
		
		buttonAddDataSource = new JButton();
		buttonAddDataSource.setText("+");
		buttonAddDataSource.addActionListener(addDataSourceActionListener);
		panelDataSource.add(buttonAddDataSource, new CellConstraints(6, 4));
		
		scrollPaneConsole = new JScrollPane();
		scrollPaneConsole.setBorder(new BevelBorder(BevelBorder.RAISED));
		scrollPaneConsole.setAutoscrolls(true);
		frame.getContentPane().add(scrollPaneConsole, new CellConstraints("2, 8, 1, 1, fill, fill"));

		textAreaConsole = new JTextArea();
		scrollPaneConsole.setViewportView(textAreaConsole);
		textAreaConsole.setFocusAccelerator('\b');
		textAreaConsole.setAutoscrolls(true);
		textAreaConsole.setWrapStyleWord(false);
		textAreaConsole.setLineWrap(false);
		textAreaConsole.setOpaque(false);
		textAreaConsole.setToolTipText("");
		textAreaConsole.setName("");
		textAreaConsole.setEditable(false);

		final JPanel panelFooter = new JPanel();
		panelFooter.setLayout(new FormLayout(
			new ColumnSpec[] {
				ColumnSpec.decode("54dlu"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("167dlu"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("85dlu"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("87dlu"),
				FormFactory.RELATED_GAP_COLSPEC},
			new RowSpec[] {
				RowSpec.decode("96dlu")}));
		frame.getContentPane().add(panelFooter, new CellConstraints(2, 10, CellConstraints.DEFAULT, CellConstraints.FILL));

		ActionListener synchronizeActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Task task = new Task(channel);
				task.execute();
			}
		};	
		
		ActionListener cancelActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Task task = new Task(CANCEL_SYNC);
				task.execute();
			}
		};	
		
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setAutoscrolls(true);
		panelFooter.add(scrollPane, new CellConstraints("3, 1, 3, 1, fill, fill"));

		textAreaStatus = new JTextArea();
		textAreaStatus.setLineWrap(true);
		scrollPane.setViewportView(textAreaStatus);
		textAreaStatus.setBorder(new BevelBorder(BevelBorder.RAISED));
		textAreaStatus.setWrapStyleWord(true);
		textAreaStatus.setEditable(false);

		final JLabel labelLogoInstedd = new JLabel();
		labelLogoInstedd.setIcon(SwingResourceManager.getIcon(EpiinfoUI.class, "/logo-instedd.png"));
		labelLogoInstedd.setText("");
		panelFooter.add(labelLogoInstedd, new CellConstraints(7, 1, CellConstraints.FILL, CellConstraints.CENTER));

		final JLabel labelLogoEpiinfo = new JLabel();
		labelLogoEpiinfo.setIcon(SwingResourceManager.getIcon(EpiinfoUI.class, "/Epi2002.jpg"));
		labelLogoEpiinfo.setText("");
		panelFooter.add(labelLogoEpiinfo, new CellConstraints(1, 1, CellConstraints.FILL, CellConstraints.CENTER));

		final JPanel panelSyncButtons = new JPanel();
		panelSyncButtons.setLayout(new FormLayout(
			new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("88dlu"),
				ColumnSpec.decode("175dlu"),
				ColumnSpec.decode("88dlu"),
				FormFactory.RELATED_GAP_COLSPEC},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC}));
		frame.getContentPane().add(panelSyncButtons, new CellConstraints(2, 6));
		
		buttonSynchronize = new JButton();
		panelSyncButtons.add(buttonSynchronize, new CellConstraints(1, 1, CellConstraints.FILL, CellConstraints.FILL));
		buttonSynchronize.setText(EpiInfoUITranslator.getSynchronize());
		buttonSynchronize.addActionListener(synchronizeActionListener);

		buttonCancel = new JButton();
		panelSyncButtons.add(buttonCancel, new CellConstraints(3, 1, CellConstraints.FILL, CellConstraints.FILL));
		buttonCancel.setText(EpiInfoUITranslator.getCancel());
		buttonCancel.addActionListener(cancelActionListener);
		
		ActionListener cleanConsoleActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				textAreaConsole.setText("");
			}
		};	
		
		buttonClean = new JButton();
		buttonClean.setText(EpiInfoUITranslator.getLabelCleanConsole());
		buttonClean.addActionListener(cleanConsoleActionListener);
		panelSyncButtons.add(buttonClean, new CellConstraints(5, 1, CellConstraints.RIGHT, CellConstraints.FILL));

	}

	private ComboBoxModel getDataSourceTableModel() {
		try{
			Set<String> tableNames = MsAccessHelper.getTableNames(this.defaultDataSource);
			ComboBoxModel model = new DefaultComboBoxModel(tableNames.toArray());
			model.setSelectedItem(this.defaultTableName);
			return model;
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			return new DefaultComboBoxModel();
		}
	}

	private void shutdown(){
		try{
			this.syncEngine.getChannel().shutdown();
		} catch(Throwable e){
			Logger.error(e.getMessage(), e);
		}
	}
	
	private class Task extends SwingWorker<Void, Void> {
		 
		// MODEL VARIABLES
		private int action = 0;
		 
		// BUSINESS METHODS
	    public Task(int action) {
			super();
			this.action = action;
		}
	
		@Override
	    public Void doInBackground() {
	    	disableAllButtons();
	    	try{
	    		if(action == SYNCHRONIZE_SMS){
	    			textAreaConsole.setText("");
	    			
	    			SyncEngineUtil.addDataSource(fileNameResolver, textFieldDataSource.getText());
	    			
	    			if(emulate){	    				
	    				SyncEngineUtil.registerNewEndpointToEmulator(syncEngine, textFieldPhoneNumber.getText(), messageEncoding, 
	    					identityProvider, baseDirectory, senderDelay, receiverDelay, readDelay, channelDelay, maxMessageLenght);
	    			}	
    				SyncEngineUtil.synchronize(syncEngine, textFieldPhoneNumber.getText(), 
	    					textFieldDataSource.getText(), (String)comboTables.getSelectedItem(), 
	    					identityProvider, baseDirectory);
	    		} else if(action == SYNCHRONIZE_HTTP){
	    			consoleNotification.beginSync(textFieldURL.getText(), textFieldDataSource.getText(), (String)comboTables.getSelectedItem());
	    			List<Item> conflicts = SyncEngineUtil.synchronize(textFieldURL.getText(), textFieldDataSource.getText(), (String)comboTables.getSelectedItem(), identityProvider, baseDirectory);
	    			consoleNotification.endSync(textFieldURL.getText(), textFieldDataSource.getText(), (String)comboTables.getSelectedItem(), conflicts);
	    		} else if(action == CANCEL_SYNC){
	    			SyncEngineUtil.cancelSynchronize(syncEngine, textFieldPhoneNumber.getText(), textFieldDataSource.getText(), (String)comboTables.getSelectedItem());
	    		} else if(action == ADD_DATA_SOURCE){
	    			SyncEngineUtil.addDataSource(fileNameResolver, textFieldDataSource.getText());
	    		} else if(action == DISCOVERY_MODEMS){
					comboSMSDevice.setModel(new DefaultComboBoxModel(SyncEngineUtil.getAvailableModems()));
	    		}
    		} catch(Throwable t){
    			consoleNotification.logError(t, EpiInfoUITranslator.getLabelFailed());
    		}
	        return null;
	    }

		@Override
	    public void done() {
	        enableAllButtons();
	    }
	}

	 private void enableAllButtons(){
		 this.buttonCancel.setEnabled(true);
		 this.buttonSynchronize.setEnabled(true);
		 this.buttonClean.setEnabled(true);
		 
		 this.radioEndpointHTTP.setEnabled(true);
		 this.radioEndpointSMS.setEnabled(true);
		 
		 if(channel == SYNCHRONIZE_SMS){
			 this.labelSMSDevice.setEnabled(true);
			 this.labelPhoneNumber.setEnabled(true);
			 this.comboSMSDevice.setEnabled(true);
			 this.buttonModemDiscovery.setEnabled(true);
			 this.textFieldPhoneNumber.setEnabled(true);
		 }
		 
		 this.textFieldDataSource.setEnabled(true);
		 this.comboTables.setEnabled(true);
		 this.buttonAddDataSource.setEnabled(true);
		 this.buttonOpenFileDataSource.setEnabled(true);

		 if(channel == SYNCHRONIZE_HTTP){
			 this.labelUrl.setEnabled(true);
			 this.textFieldURL.setEnabled(true);
		 }

	 }
	 
	 private void disableAllButtons(){
		 this.buttonSynchronize.setEnabled(false);
		 this.buttonClean.setEnabled(false);
		 this.buttonCancel.setEnabled(false);

		 this.radioEndpointHTTP.setEnabled(false);
		 this.radioEndpointSMS.setEnabled(false);
		 
		 this.labelSMSDevice.setEnabled(false);
		 this.labelPhoneNumber.setEnabled(false);
		 this.textFieldPhoneNumber.setEnabled(false);
		 this.buttonModemDiscovery.setEnabled(false);
		 this.comboSMSDevice.setEnabled(false);
		 
		 this.textFieldDataSource.setEnabled(false);
		 this.comboTables.setEnabled(false);
		 this.buttonAddDataSource.setEnabled(false);
		 this.buttonOpenFileDataSource.setEnabled(false);

		 this.labelUrl.setEnabled(false);
		 this.textFieldURL.setEnabled(false);
	 }

	 
		private String openFileDialog(String fileName){
			String fileNameSelected = openFileDialog(fileName, new FileNameExtensionFilter(EpiInfoUITranslator.getLabelDataSourceFileExtensions(), "mdb"));
			return fileNameSelected;
		}
		
		private String openFileDialog(String fileName, FileNameExtensionFilter filter){
			JFileChooser chooser = new JFileChooser();
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileFilter(filter);
			
			if(fileName != null && fileName.trim().length() > 0){
				File file = new File(fileName);
				chooser.setSelectedFile(file);
			}
			
			int returnVal = chooser.showOpenDialog(this.frame);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				return chooser.getSelectedFile().getAbsolutePath();
			} else{
				return null;
			}
		}
}
