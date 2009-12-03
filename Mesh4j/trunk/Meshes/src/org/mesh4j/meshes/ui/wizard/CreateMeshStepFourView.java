package org.mesh4j.meshes.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

import org.mesh4j.meshes.controller.CreateMeshWizardController;

public class CreateMeshStepFourView extends BaseWizardPanel {

	private static final long serialVersionUID = -5773369351266179486L;
	private static String ID = "STEP_FOUR";
	
	private CreateMeshWizardController controller;
	
	private List<AbstractButton> buttons;
	private ButtonGroup buttonGroup;
	
	public CreateMeshStepFourView(CreateMeshWizardController controller) {
		super();
		this.controller = controller;
		this.buttons = new ArrayList<AbstractButton>();
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
		
		buttonGroup = new ButtonGroup();
		
		createButtons();
	}
	
	private void createButtons() {
		JToggleButton msAccessButton = new JToggleButton();
		msAccessButton.setText("Microsoft Access Table");
		buttons.add(msAccessButton);
		buttonGroup.add(msAccessButton);
		add(msAccessButton);
		
		JToggleButton excelSpreadsheetButton = new JToggleButton();
		excelSpreadsheetButton.setText("Excel Spreadsheet");
		buttons.add(excelSpreadsheetButton);
		buttonGroup.add(excelSpreadsheetButton);
		add(excelSpreadsheetButton, "wrap");
		
		JToggleButton googleSpreadsheetButton = new JToggleButton();
		googleSpreadsheetButton.setText("Google Spreadsheet");
		buttons.add(googleSpreadsheetButton);
		buttonGroup.add(googleSpreadsheetButton);
		add(googleSpreadsheetButton);
		
		JToggleButton epiInfoButton = new JToggleButton();
		epiInfoButton.setText("CDC EpiInfo");
		buttons.add(epiInfoButton);
		buttonGroup.add(epiInfoButton);
		add(epiInfoButton, "wrap");
	}
	
	private void clearButtons() {
		for (AbstractButton button : buttons) {
			remove(button);
			buttonGroup.remove(button);
		}
		buttons.clear();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
	}

	@Override
	public String getId() {
		return ID;
	}

}
