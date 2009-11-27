package org.mesh4j.meshes.ui.wizard;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

public class CreateMeshStepThreeView extends JPanel {

	private static final long serialVersionUID = -5773369351266179486L;
	
	private WizardPanelDescriptor descriptor;
	
	private JToggleButton tableButton;
	private JToggleButton mapButton;
	private JToggleButton filesButton;
	private ButtonGroup buttonGroup;
	
	public CreateMeshStepThreeView(WizardPanelDescriptor descriptor) {
		super();
		this.descriptor = descriptor;
		initComponents();
	}

	private void initComponents() {
		setLayout(new MigLayout("insets 10"));
		setSize(550, 350);
		
		JLabel titleLabel = new JLabel("Create a new data source");
		add(titleLabel, "span 2, wrap 20");
		
		JLabel subTitleLabel = new JLabel();
		subTitleLabel.setText("<html>When you add a data surce to your mesh, you can see it mobile devices, " +
							  "maps, or applications. You can even allow other applications to update the data</html>");
		add(subTitleLabel, "span 2, wrap 20");
		
		JLabel dataSourceQuestion = new JLabel();
		dataSourceQuestion.setText("What data source would you like to add?");
		add(dataSourceQuestion, "span 2, wrap 5");
		
		tableButton = new JToggleButton();
		tableButton.setText("A table of data");
		// TODO add icon to button
		JLabel tableLabel = new JLabel();
		tableLabel.setText("You can add Access Databases, EpiInfo surveys, Excel Spreadsheets, JavaROSA Forms, or Google Spreadsheets to your mesh");
		add(tableButton, "gapright 10");
		add(tableLabel, "growx, wrap");
		
		mapButton = new JToggleButton();
		mapButton.setText("A map");
		// TODO add icon to button
		JLabel mapLabel = new JLabel();
		mapLabel.setText("Share maps including pushpins, polygons, lines, icons and other information");
		add(mapButton, "gapright 10");
		add(mapLabel, "growx, wrap");
		
		filesButton = new JToggleButton();
		filesButton.setText("Files");
		// TODO add icon to button
		JLabel filesLabel = new JLabel();
		filesLabel.setText("Share a set of files");
		add(filesButton, "gapright 10");
		add(filesLabel, "growx, wrap");
		
		buttonGroup = new ButtonGroup();
		buttonGroup.add(tableButton);
		buttonGroup.add(mapButton);
		buttonGroup.add(filesButton);
	}

}
