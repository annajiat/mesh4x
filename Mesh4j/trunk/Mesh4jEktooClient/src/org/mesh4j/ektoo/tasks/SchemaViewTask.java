package org.mesh4j.ektoo.tasks;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mesh4j.ektoo.controller.AbstractUIController;
import org.mesh4j.ektoo.ui.EktooFrame;
import org.mesh4j.ektoo.ui.SchemaViewUI;
import org.mesh4j.ektoo.ui.component.RoundBorder;
import org.mesh4j.ektoo.ui.translator.EktooUITranslator;
import org.mesh4j.sync.payload.schema.rdf.IRDFSchema;

public class SchemaViewTask extends SwingWorker<String, Void>{

	private static Log Logger = LogFactory.getLog(SchemaViewTask.class);
	private EktooFrame ui;
	private IErrorListener errorListener;
	private AbstractUIController controller;
	

	public SchemaViewTask(EktooFrame ui,AbstractUIController controller, IErrorListener errorListener){
		this.ui = ui;
		this.errorListener = errorListener;
		this.controller = controller;
	}
	//TODO (raju) implement the listener and log the error
	@Override
	protected String doInBackground() throws Exception {
		ui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		HashMap<IRDFSchema , String> shcemaList = controller.fetchSchema(controller.createAdapter());
		showSchemaInPopup(createSchemaView(shcemaList));
		return null;
	}
	@Override
	public void done(){
		ui.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	
	
	private JScrollPane createSchemaView(HashMap<IRDFSchema , String> shcemaList){
		SchemaViewUI schemaViewUI = null;
		JPanel schemaPanel = new JPanel();
		int row = shcemaList.size();
		schemaPanel.setLayout(new GridLayout(row,0,0,0));
		for(Map.Entry<IRDFSchema, String>  entry :  shcemaList.entrySet()){
			IRDFSchema schema = entry.getKey();
			schemaViewUI = new SchemaViewUI(schema);
			schemaViewUI.setBorder(BorderFactory.createTitledBorder( new RoundBorder(Color.LIGHT_GRAY), 
					schema.getOntologyClassName()));
			schemaPanel.add(schemaViewUI);
		}
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(schemaPanel);
		
		return scrollPane;
	}
	
	private void showSchemaInPopup(JScrollPane schemaUI){
		this.ui.showViewInPopup(EktooUITranslator.getTitleOfSchemaViewPopUp(),schemaUI);
	}
	
}
