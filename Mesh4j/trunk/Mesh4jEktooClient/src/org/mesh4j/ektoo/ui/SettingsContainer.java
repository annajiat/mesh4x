package org.mesh4j.ektoo.ui;
import static org.mesh4j.translator.MessageProvider.translate;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.mesh4j.ektoo.ui.component.RoundBorder;
import org.mesh4j.ektoo.ui.settings.CloudSettingsModel;
import org.mesh4j.ektoo.ui.settings.CloudSettingsUI;
import org.mesh4j.ektoo.ui.settings.GSSSettingsModel;
import org.mesh4j.ektoo.ui.settings.GSSSettingsUI;
import org.mesh4j.ektoo.ui.settings.GeneralSettingsModel;
import org.mesh4j.ektoo.ui.settings.GeneralSettingsUI;
import org.mesh4j.ektoo.ui.settings.MySqlSettingsModel;
import org.mesh4j.ektoo.ui.settings.MySqlSettingsUI;
import org.mesh4j.ektoo.ui.settings.SettingsController;
import org.mesh4j.ektoo.ui.settings.SettingsNotificationTask;
import org.mesh4j.translator.MessageNames;



public class SettingsContainer extends JPanel{

	private static final long serialVersionUID = -2277428339975245711L;
	
	private JPanel parentSettingsPanel;
	private SettingsController controller;
	private final static String SETTINGS_GENERAL = "General";
	private final static String SETTINGS_CLOUD = "Cloud";
	private final static String SETTINGS_MYSQL = "Mysql";
	private final static String SETTINGS_GOOGLE = "Google Spreadsheet";
	private EktooFrame ektooFrame = null;

	private JCheckBox updateUICheckBox;
	
	public SettingsContainer(SettingsController controller,EktooFrame ektooFrame){
		this.controller = controller;
		this.ektooFrame = ektooFrame;
		this.setLayout(new BorderLayout());
		initComponents();
		controller.loadSettings();
	}

	private void initComponents(){
		this.add(createTreeMenuPane(),BorderLayout.WEST);
		this.add(createSettingsComponentPane(),BorderLayout.CENTER);
		this.add(createFooterPane(),BorderLayout.SOUTH);
	}
	
	
	private JPanel createTreeMenuPane(){
		JPanel treeItemPanel = new JPanel(new BorderLayout());
		treeItemPanel.setPreferredSize(new Dimension(150,100));
		treeItemPanel.setBorder(BorderFactory.createTitledBorder( new RoundBorder(Color.LIGHT_GRAY)));
		JScrollPane pane = new JScrollPane(createSettingsTree());
		treeItemPanel.add(pane,BorderLayout.CENTER);
		return treeItemPanel;
	}
	
	private JTree createSettingsTree(){
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Settings");
		
		DefaultMutableTreeNode generalNode = new DefaultMutableTreeNode(SETTINGS_GENERAL);
		root.add(generalNode);
	    DefaultMutableTreeNode cloudNode = new DefaultMutableTreeNode(SETTINGS_CLOUD);
	    root.add(cloudNode);
	    DefaultMutableTreeNode gssNode = new DefaultMutableTreeNode(SETTINGS_GOOGLE);
	    root.add(gssNode);
	    DefaultMutableTreeNode mysqlNode = new DefaultMutableTreeNode(SETTINGS_MYSQL);
	    root.add(mysqlNode);
    
        final JTree settingsTree = new JTree(root);
    	settingsTree.addTreeSelectionListener(new TreeSelectionListener(){
    		@Override
    		public void valueChanged(TreeSelectionEvent e) {
				  Object o = settingsTree.getLastSelectedPathComponent();
			      DefaultMutableTreeNode show = (DefaultMutableTreeNode) o;
			      String title = (String) show.getUserObject();
			      updateSettingsPane(title);
		}}
    	);
    	settingsTree.setRootVisible(false);
	return settingsTree;
	}
	
	private void updateSettingsPane(String paneName){
		CardLayout cl = (CardLayout) (parentSettingsPanel.getLayout());
		cl.show(parentSettingsPanel, paneName);
	}
	
	private JPanel createSettingsComponentPane(){
		parentSettingsPanel = new JPanel(new CardLayout());
		
		GeneralSettingsUI generalSettingsUI = new GeneralSettingsUI(controller);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(generalSettingsUI);
		
		controller.addModel(new GeneralSettingsModel());
		controller.addView(generalSettingsUI);
		
		GSSSettingsUI gssSettingsUI = new GSSSettingsUI(controller);
		controller.addModel(new GSSSettingsModel());
		controller.addView(gssSettingsUI);
		
		CloudSettingsUI cloudSettingsUI = new CloudSettingsUI(controller);
		controller.addModel(new CloudSettingsModel());
		controller.addView(cloudSettingsUI);
		
		MySqlSettingsUI mysqlSettingsUI = new MySqlSettingsUI(controller);
		controller.addModel(new MySqlSettingsModel());
		controller.addView(mysqlSettingsUI);
		
		parentSettingsPanel.add(scrollPane,SETTINGS_GENERAL);
		parentSettingsPanel.add(gssSettingsUI,SETTINGS_GOOGLE);
		parentSettingsPanel.add(cloudSettingsUI,SETTINGS_CLOUD);
		parentSettingsPanel.add(mysqlSettingsUI,SETTINGS_MYSQL);
		
		return parentSettingsPanel;
	}
	
	private JPanel createFooterPane(){
		JPanel headerPanel = new JPanel(new BorderLayout(10,5));
		headerPanel.setBorder(BorderFactory.createTitledBorder( new RoundBorder(Color.LIGHT_GRAY)));
		headerPanel.add(getButtonPanel(),BorderLayout.LINE_END);
		return headerPanel;
	}
	
	private JPanel getButtonPanel(){
		JPanel headerPanel = new JPanel(new GridBagLayout());
		
		updateUICheckBox = new JCheckBox(translate(MessageNames.LABEL_CHECKBOX_APPLY_CHANGES));
		
		JButton okButton = new JButton(translate(MessageNames.LABEL_BUTTON_OK));
		okButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				processUpdateSettings();
		}});
		
		JButton cancelButton = new JButton(translate(MessageNames.LABEL_BUTTON_CANCEL));
		cancelButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}});

		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 0, 0, 5);
		headerPanel.add(updateUICheckBox,c);
		
		
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new Insets(0, 10, 0, 10);
		headerPanel.add(getSeperator(SwingConstants.VERTICAL),c);
		
		
		c.fill = GridBagConstraints.NONE;
		c.gridx = 2;
		c.gridy = 0;
		c.insets = new Insets(0, 0, 0, 5);
		headerPanel.add(okButton,c);
		
		c.fill = GridBagConstraints.NONE;
		c.gridx = 3;
		c.gridy = 0;
		c.insets = new Insets(0, 0, 0, 5);
		headerPanel.add(cancelButton,c);

		return headerPanel;
	}
	
	
	private JComponent getSeperator(int orientation){ 
		 JSeparator separator = null;
		 JPanel spePanel = new JPanel(new GridLayout(1,2,0,0));
		 spePanel.setOpaque(false);

		 separator = new JSeparator(orientation);
		 spePanel.add(separator);
		 
		 separator = new JSeparator(orientation);
		 spePanel.add(separator);
	    return spePanel;
	  }
	
	
	private void processUpdateSettings(){
		//TODO (raju) this also must goes to as thread task
		this.controller.save();
		if(updateUICheckBox.isSelected()){
			SettingsNotificationTask task = new SettingsNotificationTask(
					this.ektooFrame,this.controller);
			task.execute();
		}
		close();
	}
	
	private void close(){
		ektooFrame.closePopupViewWindow();
	}
}
