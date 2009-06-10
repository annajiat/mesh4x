package org.mesh4j.ektoo.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mesh4j.ektoo.tasks.IErrorListener;
import org.mesh4j.ektoo.tasks.OpenFileTask;
import org.mesh4j.ektoo.ui.image.ImageManager;
import org.mesh4j.ektoo.ui.translator.EktooUITranslator;

/**
 * @author Bhuiyan Mohammad Iklash
 * 
 */
public abstract class TableUI extends AbstractUI {

	private final static Log LOGGER = LogFactory.getLog(TableUI.class);
	private static final long serialVersionUID = 6283863045120436837L;
	
	// MODEL VARIABLES
	private JFileChooser fileChooser = new JFileChooser();

	private String table = null;
	private String column = null;

	private JLabel labelFile = null;
	private JTextField txtFileName = null;

	private JLabel labelTable = null;
	private JComboBox listTable = null;

	private JLabel labelColumn = null;
	private JComboBox listColumn = null;

	private JButton btnFile = null;
	private JButton btnView = null;
	
	private JTextField txtMessages = null;

	// BUSINESS METHODS
	public TableUI() {
		super();
		initialize();
	}

	public TableUI(String fileLabel, String tableLable, String fieldLabel) {
		super();
		initialize();
		setLabelFile(fileLabel);
		setLabelTable(tableLable);
		setLabelColumn(fieldLabel);

	}

	protected void initialize() 
	{
		this.setLayout(null);
		this.setBackground(Color.WHITE);
		this.add(getLabelFile(), null);
		this.add(getTxtFile(), null);
		this.add(getBtnFile(), null);
		this.add(getBtnView(), null);

		this.add(getlabelTable(), null);
		this.add(getTableList(), null);
		this.add(getLabelColumn(), null);
		this.add(getColumnList(), null);
		this.add(getMessagesText(), null);
	}

	public abstract void setList(String fileName);
	public abstract void setList(String fileName, String tableName);
	public abstract void setList(String fileName, String tableName, String columnName);
	
	

	private JLabel getLabelFile() {
		if (labelFile == null) {
			labelFile = new JLabel();
			labelFile.setText(EktooUITranslator.getFileLabel());
			labelFile.setSize(new Dimension(85, 16));
			labelFile.setPreferredSize(new Dimension(85, 16));
			labelFile.setLocation(new Point(8, 11));
		}
		return labelFile;
	}

	public void setLabelFile(String label) {
		if (labelFile != null) {
			labelFile.setText(label);
		}
	}

	public String getFilePath(){
		return getTxtFile().getText().trim();
	}
	public JTextField getTxtFile() {
		if (txtFileName == null) {
			txtFileName = new JTextField();
			txtFileName.setBounds(new Rectangle(99, 8, 149, 20));
			txtFileName.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						txtFileName.setToolTipText(txtFileName.getText());
						setList(txtFileName.getText());
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						// TODO Handle exception
					}
				}
			});
			txtFileName.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent evt) {
					try {
						txtFileName.setToolTipText(txtFileName.getText());
						setList(txtFileName.getText());						
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						// TODO Handle exception
					}
				}
			});		
		}
		return txtFileName;
	}

	// button??
	public JButton getBtnFile() {
		if (btnFile == null) {
			btnFile = new JButton();
			btnFile.setText(EktooUITranslator.getBrowseButtonLabel());
			if(this instanceof MsAccessUI){
				btnFile.setToolTipText(EktooUITranslator.getTooltipSeleceDataFile("Access"));
			}else{
				btnFile.setToolTipText(EktooUITranslator.getTooltipSeleceDataFile("Excel"));
			}
			
			btnFile.setBounds(new Rectangle(259, 8, 34, 20));
			btnFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getFileChooser().setSelectedFile(new File(txtFileName.getText()));
					int returnVal = getFileChooser().showOpenDialog(btnFile);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File selectedFile = getFileChooser().getSelectedFile();
						if (selectedFile != null) {
							try{
								txtFileName.setText(selectedFile.getCanonicalPath());
								txtFileName.setToolTipText(txtFileName.getText());
								setList(txtFileName.getText());
							} catch (Exception ex) {
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
					JFrame frame = TableUI.this.getRootFrame();
					IErrorListener errorListener = TableUI.this.getErrorListener();
					OpenFileTask task = new OpenFileTask(frame, errorListener, txtFileName.getText());
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
	
	protected IErrorListener getErrorListener(){
		return (IErrorListener)getRootFrame();
	}

	private JLabel getlabelTable() {
		if (labelTable == null) {
			labelTable = new JLabel();
			labelTable.setText(EktooUITranslator.getTableLabel());
			labelTable.setLocation(new Point(8, 38));
			labelTable.setSize(new Dimension(85, 16));
			labelTable.setPreferredSize(new Dimension(85, 16));
		}
		return labelTable;
	}

	public void setLabelTable(String label) {
		if (labelTable != null) {
			labelTable.setText(label);
		}
	}

	public JComboBox getTableList() {
		if (listTable == null) {
			listTable = new JComboBox();
			listTable.setBounds(new Rectangle(99, 36, 194, 20));
			if(this instanceof MsExcelUI){
				listTable.setToolTipText(EktooUITranslator.getTooltipSelectWorksheet());
			}else{
				listTable.setToolTipText(EktooUITranslator.getTooltipSelectTable());
			}
			
			listTable.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt){
					if(!listTable.isEnabled()){
						return;
					}
				  if (evt.getStateChange() == ItemEvent.SELECTED){
  				  int sheetIndex = listTable.getSelectedIndex();
  					if (sheetIndex != -1) {
  						table = (String) listTable.getSelectedItem();
  						setList(txtFileName.getText(), table);
  					}
				  }
				}
			});
		}
		return listTable;
	}

	private JLabel getLabelColumn() {
		if (labelColumn == null) {
			labelColumn = new JLabel();
			labelColumn.setText(EktooUITranslator.getFieldLabel());
			labelColumn.setSize(new Dimension(85, 16));
			labelColumn.setPreferredSize(new Dimension(85, 16));
			labelColumn.setLocation(new Point(8, 65));

		}
		return labelColumn;
	}

	public void setLabelColumn(String label) {
		if (labelColumn != null) {
			labelColumn.setText(label);
		}
	}

	public JComboBox getColumnList() {
		if (listColumn == null) {
			listColumn = new JComboBox();
			listColumn.setBounds(new Rectangle(99, 64, 194, 20));
			listColumn.setToolTipText(EktooUITranslator.getTooltipIdColumnName());
			listColumn.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) 
				{
				  if (evt.getStateChange() == ItemEvent.SELECTED) 
          {				
  				  int tableIndex = listTable.getSelectedIndex();
  					if (tableIndex != -1) {
  						table = (String) listTable.getSelectedItem();
  						int columnIndex = listColumn.getSelectedIndex();
  						if (columnIndex != -1) {
  							setList(txtFileName.getText(), table, (String) listColumn.getSelectedItem());
  						}
  					}
          }
				}
			});
		}
		return listColumn;
	}

	public void setFileChooser(JFileChooser fileChooser) {
		this.fileChooser = fileChooser;
	}

	public JFileChooser getFileChooser() {
		if (fileChooser == null)
			fileChooser = new JFileChooser();
		return fileChooser;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getTable() {
		return table;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getColumn() {
		if (column == null) {
			int index = getColumnList().getSelectedIndex();
			if (index != 1) {
				column = (String) getColumnList().getSelectedItem();
			}
		}
		return column;
	}

	public void showColumn(boolean bool) {
		getLabelColumn().setVisible(bool);
		getColumnList().setVisible(bool);
	}

	private JTextField getMessagesText() {
		if (txtMessages == null) {
			txtMessages = new JTextField();
			txtMessages.setBounds(new Rectangle(0, 140, 400, 20));
			txtMessages.setEditable(false);
		}
		return txtMessages;
	}
	
	protected void setMessage(String message){
		this.txtMessages.setText(message);
	}
}
