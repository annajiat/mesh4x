package org.mesh4j.ektoo.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mesh4j.ektoo.controller.MySQLUIController;
import org.mesh4j.ektoo.tasks.IErrorListener;
import org.mesh4j.ektoo.tasks.OpenMySqlFeedTask;
import org.mesh4j.ektoo.ui.image.ImageManager;
import org.mesh4j.ektoo.ui.translator.EktooUITranslator;
import org.mesh4j.ektoo.ui.validator.MySQLConnectionValidator;
import org.mesh4j.ektoo.validator.IValidationStatus;
import org.mesh4j.sync.adapters.hibernate.HibernateSyncAdapterFactory;

/**
 * @author Bhuiyan Mohammad Iklash
 * 
 */
public class MySQLUI extends AbstractUI implements IValidationStatus {
	
	private static final long serialVersionUID = 2622575852343500622L;
	private static final Log LOGGER = LogFactory.getLog(MySQLUI.class);

	// MODEL VARIABLES
	private JLabel labelUser = null;
	private JTextField txtUser = null;

	private JLabel labelPass = null;
	private JPasswordField txtPass = null;

	private JLabel labelHost = null;
	private JTextField txtHost = null;

	private JLabel labelPort = null;
	private JTextField txtPort = null;

	private JLabel labelDatabase = null;
	private JTextField txtDatabase = null;

	private JLabel labelTable = null;
	private JComboBox listTable = null;

	private JButton btnConnect = null;

	private MySQLUIController controller = null;

	private JButton btnView = null;
	
	private JTextField txtMessages = null;
	
	// BUSINESS METHODS
	public MySQLUI(MySQLUIController controller) {
		super();
		this.controller = controller;
		this.controller.addView(this);
		initialize();
	}

	private void initialize() {
		this.setLayout(null);
		this.setBackground(Color.WHITE);

		this.add(getUserLabel(), null);
		this.add(getUserText(), null);

		this.add(getPassLabel(), null);
		this.add(getPassText(), null);

		this.add(getHostLabel(), null);
		this.add(getHostText(), null);

		this.add(getPortLabel(), null);
		this.add(getPortText(), null);

		this.add(getDatabaseLabel(), null);
		this.add(getDatabaseText(), null);

		this.add(getConnectButton(), null);

		this.add(getTableLabel(), null);
		this.add(getTableList(), null);

		this.add(getBtnView(), null);
		
		this.add(getMessagesText(), null);
		setDefaultValues();
	}

	private void setDefaultValues() {
		String hostName = controller.getDefaultMySQLHost();
		if (hostName == null) {
			hostName = "";
		}
		controller.changeHostName(hostName);

		String portNo = controller.getDefaultMySQLPort();
		if (portNo == null) {
			portNo = "";
		}

		controller.changePortNo( Integer.parseInt(portNo) );
	}

	private JLabel getUserLabel() {
		if (labelUser == null) {
			labelUser = new JLabel();
			labelUser.setText(EktooUITranslator.getMySQLUserLabel());
			labelUser.setSize(new Dimension(85, 16));
			labelUser.setPreferredSize(new Dimension(85, 16));
			labelUser.setLocation(new Point(8, 9));
		}
		return labelUser;
	}

	public JTextField getUserText() {
		if (txtUser == null) {
			txtUser = new JTextField();
			txtUser.setBounds(new Rectangle(101, 5, 183, 20));
			txtUser.setToolTipText( EktooUITranslator.getMySQLUserNameFieldTooltip());
			txtUser.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						getController().changeUserName(txtUser.getText());
					} catch (Exception e) {
						// TODO Handle exception
						LOGGER.error(e.getMessage(), e);
					}
				}
			});
			txtUser.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent evt) {
					try {
						getController().changeUserName(txtUser.getText());
					} catch (Exception e) {
						// TODO Handle exception
						LOGGER.error(e.getMessage(), e);
					}
				}
			});
		}
		return txtUser;
	}

	private JLabel getPassLabel() {
		if (labelPass == null) {
			labelPass = new JLabel();
			labelPass.setText(EktooUITranslator.getMySQLPasswordLabel());
			labelPass.setSize(new Dimension(85, 16));
			labelPass.setPreferredSize(new Dimension(85, 16));
			labelPass.setLocation(new Point(8, 34));
		}
		return labelPass;
	}

	public JPasswordField getPassText() {
		if (txtPass == null) {
			txtPass = new JPasswordField();
			txtPass.setBounds(new Rectangle(101, 30, 183, 20));
			txtPass.setToolTipText(EktooUITranslator.getMySQLUserPasswordFieldTooltip());
			txtPass.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						getController().changeUserPassword(
								new String(txtPass.getPassword()));
					} catch (Exception e) {
						// TODO Handle exception
						LOGGER.error(e.getMessage(), e);
					}
				}
			});
			txtPass.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent evt) {
					try {
						getController().changeUserPassword(
								new String(txtPass.getPassword()));
					} catch (Exception e) {
						// TODO Handle exception
						LOGGER.error(e.getMessage(), e);
					}
				}
			});
		}
		return txtPass;
	}

	private JLabel getHostLabel() {
		if (labelHost == null) {
			labelHost = new JLabel();
			labelHost.setText(EktooUITranslator.getMySQLHostLabel());
			labelHost.setSize(new Dimension(85, 16));
			labelHost.setPreferredSize(new Dimension(85, 16));
			labelHost.setLocation(new Point(8, 59));
		}
		return labelHost;
	}

	public JTextField getHostText() {
		if (txtHost == null) {
			txtHost = new JTextField();
			txtHost.setBounds(new Rectangle(101, 55, 125, 20));
			txtHost.setToolTipText(EktooUITranslator.getMySQLHostFieldTooltip());
			txtHost.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						getController().changeHostName(txtHost.getText());
					} catch (Exception e) {
						// TODO Handle exception
						LOGGER.error(e.getMessage(), e);
					}
				}
			});

			txtHost.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent evt) {
					try {
						getController().changeHostName(txtHost.getText());
					} catch (Exception e) {
						// TODO Handle exception
						LOGGER.error(e.getMessage(), e);
					}
				}
			});
		}
		return txtHost;
	}

	private JLabel getPortLabel() {
		if (labelPort == null) {
			labelPort = new JLabel();
			labelPort.setText(EktooUITranslator.getMySQLPortLabel());
			labelPort.setSize(new Dimension(85, 16));
			labelPort.setPreferredSize(new Dimension(85, 16));
			labelPort.setLocation(new Point(228, 55));
		}
		return labelPort;
	}

	public JTextField getPortText() {
		if (txtPort == null) {
			txtPort = new JTextField();
			txtPort.setBounds(new Rectangle(234, 55, 50, 20));
			txtPort.setToolTipText(EktooUITranslator.getMySQLPortFieldTooltip());
			txtPort.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						//TODO (raju) please avoid the exception or let the user
						//know the message
						getController().changePortNo(
								Integer.parseInt(txtPort.getText()));
					} catch (Exception e) {
						// TODO Handle exception
						LOGGER.error(e.getMessage(), e);
					}
				}
			});

			txtPort.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent evt) {
					try {
						//TODO (raju) please avoid the exception or let the user
						//know the message
						getController().changePortNo(
								Integer.parseInt(txtPort.getText()));
					} catch (Exception e) {
						// TODO Handle exception
						LOGGER.error(e.getMessage(), e);
					}
				}
			});
		}
		return txtPort;
	}

	private JLabel getDatabaseLabel() {
		if (labelDatabase == null) {
			labelDatabase = new JLabel();
			labelDatabase.setText(EktooUITranslator.getMySQLDatabaseLabel());
			labelDatabase.setSize(new Dimension(85, 16));
			labelDatabase.setPreferredSize(new Dimension(85, 16));
			labelDatabase.setLocation(new Point(8, 84));
		}
		return labelDatabase;
	}

	public JTextField getDatabaseText() {
		if (txtDatabase == null) {
			txtDatabase = new JTextField();
			txtDatabase.setBounds(new Rectangle(101, 80, 155, 20));
			txtDatabase.setToolTipText(EktooUITranslator.getMySQLDatabaseFieldTooltip());
			txtDatabase.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						getController().changeDatabaseName(
								txtDatabase.getText());
					} catch (Exception e) {
						// TODO Handle exception
						LOGGER.error(e.getMessage(), e);
					}
				}
			});

			txtDatabase.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent evt) {
					try {
						getController().changeDatabaseName(
								txtDatabase.getText());
					} catch (Exception e) {
						// TODO Handle exception
						LOGGER.error(e.getMessage(), e);
					}
				}
			});
		}
		return txtDatabase;
	}

	private JButton getConnectButton() {
		if (btnConnect == null) {
			btnConnect = new JButton();
			btnConnect.setBounds(new Rectangle(260, 80, 22, 20));
			btnConnect.setIcon(ImageManager.getDatabaseConnectionIcon());
			btnConnect.setToolTipText(EktooUITranslator.getDatabaseConnectionTooltip());
			btnConnect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					
					List<JComponent> uiFieldListForValidation = new ArrayList<JComponent>();
					uiFieldListForValidation.add(getUserText());
					uiFieldListForValidation.add(getPassText());
					uiFieldListForValidation.add(getHostText());
					uiFieldListForValidation.add(getPortText());
					uiFieldListForValidation.add(getDatabaseText());
					
					boolean valid = (new MySQLConnectionValidator(MySQLUI.this,
							controller.getModel(), uiFieldListForValidation)).verify();
					if (valid) {

						SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
							public Void doInBackground() {
								setCursor(Cursor
										.getPredefinedCursor(Cursor.WAIT_CURSOR));
								setList(getUser(), getPass(), getHost(),
										getPort(), txtDatabase.getText());
								return null;
							}

							public void done() {
								setCursor(Cursor
										.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							}
						};
						worker.execute();
					}
				}
			});
		}
		return btnConnect;
	}

	
	
	private JLabel getTableLabel() {
		if (labelTable == null) {
			labelTable = new JLabel();
			labelTable.setText(EktooUITranslator.getMySQLTableLabel());
			labelTable.setLocation(new Point(8, 109));
			labelTable.setSize(new Dimension(85, 16));
			labelTable.setPreferredSize(new Dimension(85, 16));
		}
		return labelTable;
	}

	public JComboBox getTableList() {
		if (listTable == null) {
			listTable = new JComboBox();
			listTable.setBounds(new Rectangle(101, 105, 183, 20));
			listTable.setToolTipText(EktooUITranslator.getTooltipSelectTable());
			listTable.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					getController().changeTableName(
							(String) listTable.getSelectedItem());
				}
			});
		}
		return listTable;
	}

	public JButton getBtnView() {
		if (btnView == null) {
			btnView = new JButton();
			btnView.setIcon(ImageManager.getViewIcon());
			btnView.setContentAreaFilled(false);
			btnView.setBorderPainted(false);
			btnView.setBorder(new EmptyBorder(0, 0, 0, 0));
			btnView.setBackground(Color.WHITE);
			btnView.setText("");
			btnView.setToolTipText(EktooUITranslator.getTooltipView());
			btnView.setBounds(new Rectangle(299, 8, 34, 40));
			btnView.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFrame frame = MySQLUI.this.getRootFrame();
					OpenMySqlFeedTask task = new OpenMySqlFeedTask(frame, (IErrorListener)frame, MySQLUI.this.controller);
					task.execute();
				}
			});
		}
		return btnView;
	}

	// TODO (nobel) improve it
	protected JFrame getRootFrame() {
		return (JFrame)this.getParent().getParent().getParent().getParent().getParent().getParent().getParent();
	}
	
	public void setList(String user, String pass, String host, int port, String schema) {
		JComboBox tableList = getTableList();
		tableList.removeAllItems();
		
		Set<String> tableNames = HibernateSyncAdapterFactory.getMySqlTableNames(host, port, schema, user, pass);
		for (String tableName : tableNames) {
			tableList.addItem(tableName);
		}
	}

	public void setList(String user, String pass, String host, int port,
			String databaseName, String tableName) {

	}

	public void setController(MySQLUIController controller) {
		this.controller = controller;
	}

	public MySQLUIController getController() {
		return controller;
	}

	public String getUser() {
		return getUserText().getText();
	}

	public String getPass() {
		return new String(getPassText().getPassword());
	}

	public String getHost() {
		return getHostText().getText();
	}

	public int getPort() {
		return Integer.parseInt(getPortText().getText());
	}

	public String getTable() {
		return (String) getTableList().getSelectedItem();
	}

  
	@Override
	public void validationFailed(Hashtable<Object, String> errorTable) {
		((SyncItemUI)this.getParent().getParent()).openErrorPopUp(errorTable);
	}

	
	@Override
	public void validationPassed() {
		// TODO (Nobel)
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals(MySQLUIController.USER_NAME_PROPERTY)) {
			String newStringValue = evt.getNewValue().toString();
			if (!getUserText().getText().equals(newStringValue))
				getUserText().setText(newStringValue);
		} else if (evt.getPropertyName().equals(
				MySQLUIController.USER_PASSWORD_PROPERTY)) {
			String newStringValue = evt.getNewValue().toString();
			if (!new String(getPassText().getPassword()).equals(newStringValue))
				getPassText().setText(newStringValue);
		} else if (evt.getPropertyName().equals(
				MySQLUIController.HOST_NAME_PROPERTY)) {
			String newStringValue = evt.getNewValue().toString();
			if (!getHostText().getText().equals(newStringValue))
				getHostText().setText(newStringValue);
		} else if (evt.getPropertyName().equals(
				MySQLUIController.PORT_NO_PROPERTY)) {
			String newStringValue = evt.getNewValue().toString();
			if (!getPortText().getText().equals(newStringValue))
				getPortText().setText(newStringValue);
		} else if (evt.getPropertyName().equals(
				MySQLUIController.DATABASE_NAME_PROPERTY)) {
			String newStringValue = evt.getNewValue().toString();
			if (!getDatabaseText().getText().equals(newStringValue))
				getDatabaseText().setText(newStringValue);
		} else if (evt.getPropertyName().equals(
				MySQLUIController.TABLE_NAME_PROPERTY)) {
			String newStringValue = evt.getNewValue().toString();
			if (!((String) getTableList().getSelectedItem())
					.equals(newStringValue))
				getTableList().setSelectedItem(newStringValue);
		}

	}

	@Override
	public boolean verify() {
		boolean valid = (new MySQLConnectionValidator(MySQLUI.this,
				controller.getModel(), null)).verify();
		return valid;
	}

	private JTextField getMessagesText() {
		if (txtMessages == null) {
			txtMessages = new JTextField();
			txtMessages.setBounds(new Rectangle(0, 140, 400, 20));
			txtMessages.setEditable(false);
		}
		return txtMessages;
	}
}