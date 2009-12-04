package org.mesh4j.meshes.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

import org.mesh4j.meshes.controller.CreateMeshWizardController;

public class CreateMeshStepFourView extends BaseWizardPanel {

	private static final long serialVersionUID = -5773369351266179486L;
	private static String ID = "STEP_FOUR";
	
	private CreateMeshWizardController controller;
	
	private ButtonGroup buttonGroup;
	
	public CreateMeshStepFourView(CreateMeshWizardController controller) {
		super();
		this.controller = controller;
		initComponents();
	}

	private void initComponents() {
		setLayout(new MigLayout("insets 10"));
		setSize(550, 350);
		
		JLabel titleLabel = new JLabel("Add Table Data");
		add(titleLabel, "span 2, wrap 40");
		
		JLabel dataSourceQuestion = new JLabel();
		dataSourceQuestion.setText("What data source would you like to add?");
		add(dataSourceQuestion, "span 2, wrap 10");
		
		JToggleButton msAccessButton = new JToggleButton();
		msAccessButton.setText("Microsoft Access Table");
		add(msAccessButton);
		
		msAccessButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				msAccessButtonActionPerformed(e);
			}
		});
		
		JToggleButton msExcelButton = new JToggleButton();
		msExcelButton.setText("Excel Spreadsheet");
		add(msExcelButton, "wrap");
		
		msExcelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				msExcelButtonActionPerformed(e);
			}
		});
		
		JToggleButton googleSpreadsheetButton = new JToggleButton();
		googleSpreadsheetButton.setText("Google Spreadsheet");
		add(googleSpreadsheetButton);
		
		googleSpreadsheetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				googleSpreadsheetButtonActionPerformed(e);
			}
		});
		
		buttonGroup = new ButtonGroup();
		buttonGroup.add(msAccessButton);
		buttonGroup.add(msExcelButton);
		buttonGroup.add(googleSpreadsheetButton);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
	}
	
	private void msAccessButtonActionPerformed(ActionEvent e) {
		controller.setMsAccessDataSource();
	}
	
	private void msExcelButtonActionPerformed(ActionEvent e) {
		controller.setMsExcelDataSource();
	}

	private void googleSpreadsheetButtonActionPerformed(ActionEvent e) {
		controller.setGSSheetDataSource();
	}

	@Override
	public String getId() {
		return ID;
	}

}
