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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mesh4j.ektoo.Event;
import org.mesh4j.ektoo.controller.CloudUIController;
import org.mesh4j.ektoo.tasks.IErrorListener;
import org.mesh4j.ektoo.tasks.OpenURLTask;
import org.mesh4j.ektoo.tasks.SchemaViewTask;
import org.mesh4j.ektoo.ui.translator.EktooUITranslator;
import org.mesh4j.ektoo.ui.validator.CloudUIValidator;

public class CloudUI extends AbstractUI {
	
	private static final long serialVersionUID = 101977159720664976L;
	private static final Log LOGGER = LogFactory.getLog(CloudUI.class);
	
	// MODEL VARIABLES
	private JLabel labelServerURL = null;
	private JTextField txtServerURL = null;
	
	private JLabel labelMesh = null;
	private JTextField txtMesh = null;

	private JLabel labelDataset = null;
	private JTextField txtDataset = null;

	private CloudUIController controller = null;
	
//	private JButton btnView = null;

	// BUSINESS METHODS
	public CloudUI(String baseURL, CloudUIController controller) {
		super();
		this.controller = controller;
		this.controller.addView(this);
		initialize();
		this.txtServerURL.setText(baseURL);
		this.setMessageText(baseURL);
	}

	private void initialize() {
		this.setLayout(null);
		this.setBackground(Color.WHITE);

		this.add(getServerURLLabel(), null);
		this.add(getServerURLText(), null);
		
		this.add(getMeshLabel(), null);
		this.add(getMeshText(), null);

		this.add(getDataSetLabel(), null);
		this.add(getDataSetText(), null);
		
		this.add(getMessagesText(), null);
		this.add(getBtnView(), null);
		this.add(getSchemaViewButton(), null);
	}

	private JButton getSchemaViewButton(){
		getSchemaButton().addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(CloudUI.this.verify()){
					CloudUI.this.controller.setCurrentEvent(Event.schema_view_event);
					EktooFrame ektooFrame = ((EktooFrame)CloudUI.this.getRootFrame());
					SchemaViewTask task = new SchemaViewTask(ektooFrame,CloudUI.this.controller,ektooFrame);
					task.execute();	
				}
			}
		});
		return getSchemaButton();
	}
	
	public JTextField getServerURLText(){
		if (this.txtServerURL == null) {
			this.txtServerURL = new JTextField();
			this.txtServerURL.setBounds(new Rectangle(101, 5, 183, 20));
//			this.txtServerURL.setBounds(new Rectangle(101, 59, 183, 20));
			this.txtServerURL.setToolTipText(EktooUITranslator.getTooltipCloudSyncServerURI());
			this.txtServerURL.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						getController().changeSyncServerUri(txtServerURL.getText());
						setResultURL();
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						// TODO Handle exception
					}
				}
			});
			this.txtServerURL.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent evt) {
					try {
						getController().changeSyncServerUri(txtServerURL.getText());
						setResultURL();
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						// TODO Handle exception
					}
				}
			});		
		}
		return this.txtServerURL;
	}
	private JLabel getServerURLLabel() {
		
		if (this.labelServerURL == null) {
			this.labelServerURL = new JLabel();
			this.labelServerURL.setText( EktooUITranslator.getSyncURILabel());
			
			labelServerURL.setSize(new Dimension(85, 16));
			labelServerURL.setPreferredSize(new Dimension(85, 16));
			labelServerURL.setLocation(new Point(8, 9));
//			this.labelServerURL.setSize(new Dimension(85, 16));
//			this.labelServerURL.setPreferredSize(new Dimension(85, 16));
//			this.labelServerURL.setLocation(new Point(8, 59));
		}
		return this.labelServerURL;
	}
	
	private JLabel getMeshLabel() {
		if (labelMesh == null) {
			labelMesh = new JLabel();
			labelMesh.setText( EktooUITranslator.getMeshNameFieldLabel());
			
			labelMesh.setSize(new Dimension(85, 16));
			labelMesh.setPreferredSize(new Dimension(85, 16));
			labelMesh.setLocation(new Point(8, 34));
		
//			labelMesh.setSize(new Dimension(85, 16));
//			labelMesh.setPreferredSize(new Dimension(85, 16));
//			labelMesh.setLocation(new Point(8, 9));
		}
		return labelMesh;
	}

	public JTextField getMeshText() {
		if (txtMesh == null) {
			txtMesh = new JTextField();
			txtMesh.setBounds(new Rectangle(101, 30, 183, 20));
//			txtMesh.setBounds(new Rectangle(101, 5, 183, 20));
			txtMesh.setToolTipText(EktooUITranslator.getTooltipCloudMeshname());
			txtMesh.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						getController().changeMeshName(txtMesh.getText());
						setResultURL();
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						// TODO Handle exception
					}
				}
			});
			txtMesh.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent evt) {
					try {
						getController().changeMeshName(txtMesh.getText());
						setResultURL();
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						// TODO Handle exception
					}
				}
			});
		}
		return txtMesh;
	}

	protected JLabel getDataSetLabel() {
		if (labelDataset == null) {
			labelDataset = new JLabel();
			labelDataset.setText( EktooUITranslator.getMeshDataSetFieldLabel() );

			labelDataset.setSize(new Dimension(85, 16));
			labelDataset.setPreferredSize(new Dimension(85, 16));
			labelDataset.setLocation(new Point(8, 59));			
//			labelDataset.setSize(new Dimension(85, 16));
//			labelDataset.setPreferredSize(new Dimension(85, 16));
//			labelDataset.setLocation(new Point(8, 34));
		}
		return labelDataset;
	}

	public JTextField getDataSetText() {
		if (txtDataset == null) {
			txtDataset = new JTextField();
			txtDataset.setBounds(new Rectangle(101, 59, 183, 20));
//			txtDataset.setBounds(new Rectangle(101, 30, 183, 20));
			txtDataset.setToolTipText(EktooUITranslator.getTooltipCloudDatasetname());
			txtDataset.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						getController().changeDatasetName(txtDataset.getText());
						setResultURL();
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						// TODO Handle exception
					}
				}
			});
			txtDataset.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent evt) {
					try {
						getController().changeDatasetName(txtDataset.getText());
						setResultURL();
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						// TODO Handle exception
					}
				}
			});
		}
		return txtDataset;
	}
	
	private JButton getBtnView() {
			getViewButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					List<JComponent> uiFieldListForValidation = new ArrayList<JComponent>();
					uiFieldListForValidation.add(getMeshText());
					uiFieldListForValidation.add(getDataSetText());
					uiFieldListForValidation.add(getServerURLText());
					boolean valid = (new CloudUIValidator(CloudUI.this, controller.getModel(), uiFieldListForValidation, false)).verify();
					if(valid){
						JFrame frame = CloudUI.this.getRootFrame();
						String url = getController().getUri();
						OpenURLTask task = new OpenURLTask(frame, (IErrorListener)frame, url);
						task.execute();
					}
				}
			});
		return getViewButton();
	}
	
	public CloudUIController getController() {
		return controller;
	}

  @Override
  public void modelPropertyChange(final PropertyChangeEvent evt)
  {
    if ( evt.getPropertyName().equals( CloudUIController.MESH_NAME_PROPERTY))
    {
      String newStringValue = evt.getNewValue().toString();
      if (! getMeshText().getText().equals(newStringValue))
        getMeshText().setText(newStringValue);
    }
    else if ( evt.getPropertyName().equals( CloudUIController.DATASET_NAME_PROPERTY))
    {
      String newStringValue = evt.getNewValue().toString();
      if (! getDataSetText().getText().equals(newStringValue))
        getDataSetText().setText(newStringValue);
    }    
  }

	@Override
	public boolean verify() {
		List<JComponent> uiFieldListForValidation = new ArrayList<JComponent>();
		if(EktooFrame.multiModeSync){
			uiFieldListForValidation.add(getServerURLText());
			uiFieldListForValidation.add(getMeshText());
		}else{
			uiFieldListForValidation = null;
		}
		boolean valid = (new CloudUIValidator(CloudUI.this, controller.getModel(), uiFieldListForValidation)).verify();
		return valid;
	}

	private void setResultURL(){
		this.setMessageText(this.getController().getUri());
	}

}
