package org.mesh4j.meshes.ui.component;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mesh4j.meshes.model.MsAccessDataSource;
import org.mesh4j.sync.adapters.epiinfo.EpiInfoSyncAdapterFactory;

@SuppressWarnings("serial")
public class MsAccessDataSourceView extends JComponent {

	private final MsAccessDataSource dataSource;
	private JTextField fileNameField;
	private JComboBox tableComboBox;

	public MsAccessDataSourceView(MsAccessDataSource dataSource) {
		this.dataSource = dataSource;
		addViewComponents();
	}

	private void addViewComponents() {
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 10, 5, 10);
		//c.anchor = GridBagConstraints.NORTH;
		c.gridy = GridBagConstraints.RELATIVE;
		c.gridx = 0;
		
		// Labels
		add(new JLabel("MS Access File: "), c);
		add(new JLabel("Data Table: "), c);
		
		// Controls
		c.gridx = 1;
		c.weightx = 1;
		fileNameField = new JTextField(dataSource.getFileName());
		fileNameField.setEnabled(false);
		add(fileNameField, c);
		
		List<String> tableNames = EpiInfoSyncAdapterFactory.getTableNames(dataSource.getFileName());
		tableComboBox = new JComboBox(tableNames.toArray());
		tableComboBox.setSelectedItem(dataSource.getTableName());
		add(tableComboBox, c);
		
		// Fillers
		c.gridx = 0; c.gridy = 4; c.weightx = 0; c.weighty = 10;
		add(new JPanel(), c);
	}
	
}
