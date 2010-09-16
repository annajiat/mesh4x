package org.mesh4j.meshes.ui.component;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.mesh4j.meshes.model.DataSet;
import org.mesh4j.meshes.model.DataSource;
import org.mesh4j.meshes.model.Mesh;
import org.mesh4j.meshes.ui.resource.ResourceManager;

public class MeshesTreeCellRenderer extends DefaultTreeCellRenderer {
	
	private static final long serialVersionUID = 7286261949126552775L;
	
	private static Icon meshIcon = ResourceManager.getIcon("mesh.png");
	private static Icon dataSetIcon = ResourceManager.getIcon("dataset.png");
	private static Icon dataSetSynchronizingIcon = ResourceManager.getIcon("dataset_synchronizing.png");
	private static Icon dataSetFailedIcon = ResourceManager.getIcon("dataset_failed.png");
	private static Icon dataSourceIcon = ResourceManager.getIcon("datasource.png");
	private static Icon dataSourceConflictsIcon = ResourceManager.getIcon("datasource_conflicts.png");
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		if (value instanceof DefaultMutableTreeNode) {
			Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
			if (userObject instanceof DataSet) {
				DataSet dataSet = (DataSet) userObject;
				setText(getDataSetLabel(dataSet));
				setIcon(getDataSetIcon(dataSet));
			} else if (userObject instanceof DataSource) {				
				DataSource dataSource = (DataSource) userObject;
				setText(getDataSourceLabel(dataSource));
				setIcon(getDataSourceIcon(dataSource));
			} else if (userObject instanceof Mesh) {
				Mesh mesh = (Mesh)userObject;
				setText(mesh.getName());
				setIcon(meshIcon);
			}
		}
		
		return this;
	}

	private String getDataSetLabel(DataSet dataSet) {
		return dataSet.getName();
	}
	
	private Icon getDataSetIcon(DataSet dataSet) {
		switch(dataSet.getState()) {
		case FAILED:
			return dataSetFailedIcon;
		case SYNC:
			return dataSetSynchronizingIcon;
		}
		return dataSetIcon;
	}
	
	private String getDataSourceLabel(DataSource dataSource) {
		return dataSource.toString();
	}
	
	private Icon getDataSourceIcon(DataSource dataSource) {
		if (dataSource.hasConflicts()) {
			return dataSourceConflictsIcon;
		} else {
			return dataSourceIcon;
		}
	}
	
}
