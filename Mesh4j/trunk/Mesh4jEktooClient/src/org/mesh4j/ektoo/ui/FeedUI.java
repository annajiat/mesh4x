package org.mesh4j.ektoo.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mesh4j.ektoo.controller.FeedUIController;
import org.mesh4j.ektoo.tasks.IErrorListener;
import org.mesh4j.ektoo.tasks.OpenFileTask;
import org.mesh4j.ektoo.ui.image.ImageManager;
import org.mesh4j.ektoo.ui.translator.EktooUITranslator;
import org.mesh4j.ektoo.ui.validator.FeedUIValidator;
import org.mesh4j.ektoo.validator.IValidationStatus;

public class FeedUI extends AbstractUI  implements IValidationStatus {

	private static final long serialVersionUID = 2457237653577593698L;

	private static final Log LOGGER = LogFactory.getLog(FeedUI.class);

	// MODEL VARIABLES
	private JLabel labelFileName = null;
	private JTextField txtFileName = null;
	
	private JButton btnFile = null;
	private JButton btnView = null;

	private FeedUIController controller;
	private JFileChooser fileChooser = null;

	private JTextField txtMessages = null;
	
	// BUSINESS METHODS
	public FeedUI(String fileName, FeedUIController controller) {
		super();
		this.controller = controller;
		this.controller.addView(this);
		this.initialize();
		this.txtFileName.setText(fileName);
		this.txtFileName.setToolTipText(fileName);
		this.txtMessages.setText("");
	}

	private void initialize() {
		this.setLayout(null);
		this.setBackground(Color.WHITE);
		this.add(getFileNameLabel(), null);
		this.add(getFileNameText(), null);
		this.add(getBtnFile(), null);
		this.add(getBtnView(), null);
		this.add(getMessagesText(), null);
	}
	
	private JTextField getMessagesText() {
		if (txtMessages == null) {
			txtMessages = new JTextField();
			txtMessages.setBounds(new Rectangle(0, 140, 400, 20));
			txtMessages.setEditable(false);
		}
		return txtMessages;
	}

	private JLabel getFileNameLabel() {
		if (labelFileName == null) {
			labelFileName = new JLabel();
			labelFileName.setText(EktooUITranslator.getFeedFileNameLabel());
			labelFileName.setSize(new Dimension(85, 16));
			labelFileName.setPreferredSize(new Dimension(85, 16));
			labelFileName.setLocation(new Point(8, 9));
		}
		return labelFileName;
	}

	public JTextField getFileNameText() {
		if (txtFileName == null) {
			txtFileName = new JTextField();
			txtFileName.setBounds(new Rectangle(99, 8, 149, 20));
			txtFileName.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					fileNameChanged(txtFileName.getText());
				}
			});
			txtFileName.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent evt) {
					fileNameChanged(txtFileName.getText());		
				}
			});		
		}
		return txtFileName;
	}

	public JButton getBtnFile() {
		if (btnFile == null) {
			btnFile = new JButton();
			btnFile.setText(EktooUITranslator.getBrowseButtonLabel());
			btnFile.setBounds(new Rectangle(259, 8, 34, 20));
			btnFile.setToolTipText(EktooUITranslator.getTooltipSeleceDataFile("Feed"));
			btnFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getFileChooser().setSelectedFile(new File(txtFileName.getText()));
					int returnVal = getFileChooser().showOpenDialog(btnFile);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File selectedFile = getFileChooser().getSelectedFile();
						if (selectedFile != null) {
							try{
								txtFileName.setText(selectedFile.getCanonicalPath());
								fileNameChanged(txtFileName.getText());
							}catch (Exception ex) {
								LOGGER.debug(ex.getMessage(), ex);
							}	
							
						}
					}
				}
			});
		}
		return btnFile;
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
					JFrame frame = FeedUI.this.getRootFrame();
					OpenFileTask task = new OpenFileTask(frame, (IErrorListener)frame, txtFileName.getText());
					task.execute();
				}
			});
		}
		return btnView;
	}
	
	// TODO (raju) improve it
	protected JFrame getRootFrame() {
		return (JFrame)this.getParent().getParent().getParent().getParent().getParent().getParent().getParent();
	}

	public FeedUIController getController() {
		return controller;
	}

	public void setFileChooser(JFileChooser fileChooser) {
		this.fileChooser = fileChooser;
	}

	public JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setFileFilter(new FileNameExtensionFilter(
					EktooUITranslator.getXMLFileSelectorTitle(), "xml", "XML"));
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		return fileChooser;
	}

	@Override
	public void modelPropertyChange(final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(FeedUIController.FILE_NAME_PROPERTY)) {
			String newStringValue = evt.getNewValue().toString();
			if (!getFileNameText().getText().equals(newStringValue))
				getFileNameText().setText(newStringValue);
		}
	}

	@Override
	public void validationFailed(Hashtable<Object, String> errorTable) {
		((SyncItemUI)this.getParent().getParent()).openErrorPopUp(errorTable);
	}
	
	@Override
	public void validationPassed() {
		// TODO (raju)
	}

	@Override
	public boolean verify() {
		boolean valid = (new FeedUIValidator(this, controller.getModel(), null)).verify();
		return valid;
	}
	
	protected void fileNameChanged(String fileName) {
		txtFileName.setToolTipText(fileName);
		getController().changeFileName(fileName);
		
		File file = new File(fileName);
		if(!file.exists()){
			if(this.getController().acceptsCreateDataset()){
				this.txtMessages.setText(EktooUITranslator.getMessageNewFile());
			} else {
				this.txtMessages.setText(EktooUITranslator.getMessageUpdateFile());	
			}
		} else {
			this.txtMessages.setText(EktooUITranslator.getMessageUpdateFile());
		}
	}
}