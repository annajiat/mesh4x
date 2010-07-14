package org.mesh4j.meshes.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mesh4j.meshes.io.MeshMarshaller;
import org.mesh4j.meshes.model.DataSet;
import org.mesh4j.meshes.model.DataSource;
import org.mesh4j.meshes.model.Mesh;

@SuppressWarnings("serial")
public class ExportDataSourceConfigurationAction extends AbstractAction {

	private final DataSource dataSource;

	public ExportDataSourceConfigurationAction(DataSource dataSource) {
		super("Export configuration...");
		this.dataSource = dataSource;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setFileFilter(new FileNameExtensionFilter("Mesh Configuration File", "mesh"));
		fileChooser.setSelectedFile(new File(dataSource.getDataSet().getName() + ".mesh"));
		
		int returnVal = fileChooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			if (selectedFile != null) {
				
				DataSource copy = dataSource.copy();
				DataSet dataSet = dataSource.getDataSet().copy();
				dataSet.getDataSources().add(copy);
				Mesh mesh = dataSource.getDataSet().getMesh().copy();
				mesh.getDataSets().add(dataSet);
				
				MeshMarshaller.toXml(mesh, selectedFile);
			}
		}
	}

}
