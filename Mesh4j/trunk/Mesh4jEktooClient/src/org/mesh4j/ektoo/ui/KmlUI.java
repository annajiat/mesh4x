package org.mesh4j.ektoo.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mesh4j.ektoo.controller.KmlUIController;
import org.mesh4j.ektoo.tasks.IErrorListener;
import org.mesh4j.ektoo.tasks.OpenFileTask;
import org.mesh4j.ektoo.ui.component.messagedialog.MessageDialog;
import org.mesh4j.ektoo.ui.image.ImageManager;
import org.mesh4j.ektoo.ui.translator.EktooUITranslator;
import org.mesh4j.ektoo.ui.validator.KmlUIValidator;
import org.mesh4j.ektoo.validator.IValidationStatus;

/**
 * @author Bhuiyan Mohammad Iklash
 * 
 */
public class KmlUI extends AbstractUI implements IValidationStatus {

	private static final long serialVersionUID = 3586406415288503774L;
	private static final Log LOGGER = LogFactory.getLog(KmlUI.class);
	
	// MODEL VARIABLES
	private JLabel labelFileName = null;
	private JTextField txtFileName = null;
	private JButton btnFile = null;
	private JButton btnView = null;
	
	private KmlUIController controller;
	private JFileChooser fileChooser = null;
	private File file = null;

	// BUSINESS METHODS
	public KmlUI(String fileName, KmlUIController controller) {
		super();
		this.controller = controller;
		this.controller.addView(this);
		this.initialize();
		this.file = new File(fileName);
		this.txtFileName.setText(this.file.getName());
	}

	private void initialize() 
	{
		this.setLayout(null);
		this.setBackground(Color.WHITE);
		this.add(getFileNameLabel(), null);
		this.add(getFileNameText(), null);
		this.add(getBtnFile(), null);
		this.add(getBtnView(), null);
	}

	private JLabel getFileNameLabel() {
		if (labelFileName == null) {
			labelFileName = new JLabel();
			labelFileName.setText(EktooUITranslator.getKmlFileNameLabel());
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
		}
		return txtFileName;
	}

	public JButton getBtnFile() {
		if (btnFile == null) {
			btnFile = new JButton();
			btnFile.setText(EktooUITranslator.getBrowseButtonLabel());
			btnFile.setBounds(new Rectangle(259, 8, 34, 20));
			btnFile.setToolTipText(EktooUITranslator.getTooltipSeleceDataFile("KML"));
			btnFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getFileChooser().setSelectedFile(file);
					int returnVal = getFileChooser().showOpenDialog(btnFile);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						// System.out.println("You chose to open this file: " +
						// getFileChooser().getSelectedFile().getName());
						File selectedFile = getFileChooser().getSelectedFile();
						if (selectedFile != null) {
							try{
								controller.changeFileName(selectedFile.getCanonicalPath());
								txtFileName.setText(selectedFile.getName());
								setFile(selectedFile);
							} catch (Exception ex) {
								LOGGER.error(ex.getMessage(), ex);
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
					JFrame frame = KmlUI.this.getRootFrame();
					OpenFileTask task = new OpenFileTask(frame, (IErrorListener)frame, file.getAbsolutePath());
					task.execute();
				}
			});
		}
		return btnView;
	}
	
	// TODO (nobel) improve it
	protected JFrame getRootFrame() {
		return (JFrame)this.getParent().getParent().getParent().getParent().getParent().getParent();
	}
	
	public String getFileName() {
		try {
			return this.file.getCanonicalPath();
		} catch (IOException e) {
            LOGGER.debug(e.getMessage());
			// nothing to do
			return null;
		}
	}
	
	public KmlUIController getController() {
		return controller;
	}
	
	public JFileChooser getFileChooser() {
		if (fileChooser == null){
			fileChooser = new JFileChooser();
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setFileFilter(new FileNameExtensionFilter(EktooUITranslator.getKMLFileSelectorTitle(), "kml", "kmz", "KML", "KMZ"));
		}		
		return fileChooser;
	}
	
	public void setFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

    @Override
    public void modelPropertyChange(final PropertyChangeEvent evt)
    {
      if ( evt.getPropertyName().equals( KmlUIController.FILE_NAME_PROPERTY))
      {
        String newStringValue = evt.getNewValue().toString();
        if (!  getFileNameText().getText().equals(newStringValue))
          getFileNameText().setText(newStringValue);
      }
    }

	@Override
	public void validationFailed(Hashtable<Object, String> errorTable) {
		Object key = null;
		StringBuffer err = new StringBuffer();
		Enumeration<Object> keys = errorTable.keys();
		while (keys.hasMoreElements()) {
			key = keys.nextElement(); 
			err.append(errorTable.get(key) + "\n");
		}
		MessageDialog.showErrorMessage(JOptionPane.getRootFrame(), err.toString());
	}

	
	@Override
	public void validationPassed() {
		// TODO (Nobel)
	}

	@Override
	public boolean verify() {
		boolean valid = (new KmlUIValidator(this,
				controller.getModel(), null)).verify();
		return valid;
	}

}