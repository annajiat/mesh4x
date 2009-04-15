/**
 *
 */
package org.mesh4j.ektoo.ui;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import org.mesh4j.ektoo.controller.EktooUIController;


/**
 * @author Bhuiyan Mohammad Iklash
 *
 */
public class EktooUI extends JFrame {

	private final static long serialVersionUID = 1L;
	
	SyncItemUI sourceItem = null;
	SyncItemUI targetItem = null;

	private JPanel panel = null;
	private JPanel viaPane = null;
	private JPanel typePane = null;
	private JButton btnSync = null;

	private JRadioButton rbWeb = null;
	private JRadioButton rbSMS = null;
	private JRadioButton rbFile = null;
	private JRadioButton rbSent = null;
	private JRadioButton rbReceive = null;
	private JRadioButton rbSendReceive = null;

	private ButtonGroup btngSyncVia = new ButtonGroup();
	private ButtonGroup btngSyncType = new ButtonGroup();
	private JLabel txtConsole = null;


	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
        this.setSize(new Dimension(564, 511));
        this.setContentPane(getJPanel());
	}

	/**
	 * This method initializes JPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel()
	{
		if (panel == null) {
			txtConsole = new JLabel();
			txtConsole.setBounds(new Rectangle(15, 389, 525, 16));
			txtConsole.setText("");
			panel = new JPanel();
			panel.setLayout(null);
			panel.add(getSourcePane(), null);
			panel.add(getTargetPane(), null);
			panel.add(getViaPane(), null);
			panel.add(getTypePane(), null);
			panel.add(getBtnSync(), null);
			panel.add(txtConsole, null);
		}
		return panel;
	}

	/**
	 * This method initializes sourcePane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getSourcePane() 
	{
		if (sourceItem == null) 
		{
			sourceItem = new SyncItemUI("Source");
			sourceItem.setSize(new Dimension(350, 175));
			sourceItem.setLocation(new Point(10, 10));
		}
		
		return sourceItem;
	}


	/**
	 * This method initializes targetPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getTargetPane()
	{
		if(targetItem == null)
		{
			targetItem = new SyncItemUI("Target");
			targetItem.setSize(new Dimension(350, 175));
			targetItem.setLocation(new Point(10, 200));
		}
		
		return targetItem;
	}

	/**
	 * This method initializes viaPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getViaPane() {
		if (viaPane == null) {
			viaPane = new JPanel();
			//viaPane.setLayout(new GridBagLayout());
			viaPane.setBorder(BorderFactory.createTitledBorder(
		    "Sync Via"));
			viaPane.setSize(new Dimension(150, 175));
			viaPane.setLocation(new Point(390, 10));
			viaPane.add(getRbWeb(), null);
			viaPane.add(getRbSMS(), null);
			viaPane.add(getRbFile(), null);


		}
		return viaPane;
	}

	/**
	 * This method initializes typePane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getTypePane() {
		if (typePane == null) {
			typePane = new JPanel();
			typePane.setBorder(BorderFactory.createTitledBorder(
		    "Sync Type"));
			typePane.setSize(new Dimension(150, 175));
			typePane.setLocation(new Point(387, 200));
			typePane.add(getRbSent(), null);
			typePane.add(getRbReceive(), null);
			typePane.add(getRbSendReceive(), null);
		}
		return typePane;
	}

	/**
	 * This method initializes btnSync
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getBtnSync() {
		if (btnSync == null) {
			btnSync = new JButton();
			btnSync.setBounds(new Rectangle(315, 427, 127, 28));
			btnSync.setText("Sync Now");
			btnSync.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()


					txtConsole.setText("");
					String result = null;
					if (sourceItem.getSyncType().equals("TABLE"))
					{
						File sourceFile = sourceItem.getFile();
						String sourceTable = sourceItem.getTable();
						String sourceColumn = sourceItem.getColumn();
						
						File targetFile = targetItem.getFile();
						String targetTable = targetItem.getTable();
						String targetColumn = targetItem.getColumn();

						result = new EktooUIController().sync( sourceFile, sourceTable, sourceColumn,
								targetFile, targetTable, targetColumn);

					}
					else if (sourceItem.getSyncType().equals("URI"))
					{
						String sourceUri = sourceItem.getUri();
						String targetUri = targetItem.getUri();
						//result = new EktooUIController().sync( sourceUri, targetUri );
					}
					System.out.println("Calling Sync...");
					txtConsole.setText(result);
				}
			});
		}
		return btnSync;
	}

	

	/**
	 * This method initializes rbWeb
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getRbWeb() {
		if (rbWeb == null) {
			rbWeb = new JRadioButton();
			rbWeb.setBounds(new Rectangle(18, 26, 95, 24));
			rbWeb.setText("Web");
			btngSyncVia.add(rbWeb);
		}
		return rbWeb;
	}

	/**
	 * This method initializes rbSMS
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getRbSMS() {
		if (rbSMS == null) {
			rbSMS = new JRadioButton();
			rbSMS.setBounds(new Rectangle(18, 49, 92, 24));
			rbSMS.setText("SMS");
			btngSyncVia.add(rbSMS);
		}
		return rbSMS;
	}

	/**
	 * This method initializes rbFile
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getRbFile() {
		if (rbFile == null) {
			rbFile = new JRadioButton();
			rbFile.setBounds(new Rectangle(19, 77, 107, 24));
			rbFile.setText("File");
			rbFile.setSelected(true);
			btngSyncVia.add(rbFile);
		}
		return rbFile;
	}

	/**
	 * This method initializes rbSent
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getRbSent() {
		if (rbSent == null) {
			rbSent = new JRadioButton();
			rbSent.setBounds(new Rectangle(13, 28, 124, 21));
			rbSent.setText("Send");
			btngSyncType.add(rbSent);
		}
		return rbSent;
	}

	/**
	 * This method initializes rbReceive
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getRbReceive() {
		if (rbReceive == null) {
			rbReceive = new JRadioButton();
			rbReceive.setBounds(new Rectangle(13, 53, 124, 21));
			rbReceive.setText("Receive");
			btngSyncType.add(rbReceive);
		}
		return rbReceive;
	}

	/**
	 * This method initializes rbSendReceive
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getRbSendReceive() {
		if (rbSendReceive == null) {
			rbSendReceive = new JRadioButton();
			rbSendReceive.setBounds(new Rectangle(13, 78, 124, 21));
			rbSendReceive.setText("Send & Receive");
			rbSendReceive.setSelected(true);
			btngSyncType.add(rbSendReceive);
		}
		return rbSendReceive;
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame thisClass = new EktooUI();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public EktooUI() {
		super();
		initialize();
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
