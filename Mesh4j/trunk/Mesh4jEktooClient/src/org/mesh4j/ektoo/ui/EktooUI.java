/**
 *
 */
package org.mesh4j.ektoo.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mesh4j.ektoo.controller.EktooUIController;
import org.mesh4j.ektoo.properties.PropertiesProvider;
import org.mesh4j.ektoo.tasks.IErrorListener;
import org.mesh4j.ektoo.tasks.OpenURLTask;
import org.mesh4j.ektoo.tasks.SynchronizeTask;
import org.mesh4j.ektoo.ui.image.ImageManager;
import org.mesh4j.ektoo.ui.translator.EktooUITranslator;

/**
 * @author Bhuiyan Mohammad Iklash
 * 
 */
public class EktooUI extends JFrame implements IErrorListener {
	private static final long serialVersionUID = -8703829301086394863L;
	private final Log LOGGER = LogFactory.getLog(EktooUI.class);

	// MODEL VARIABLES
	private SyncItemUI sourceItem = null;
	private SyncItemUI targetItem = null;

	private JPanel panel = null;
	private JButton btnSync = null;

	private JLabel labelConsole = null;

	private JPanel panelImage = null;

	private JLabel sourceImageLabel = null;
	private JLabel targetImageLabel = null;
	private JLabel directionImageLabel = null;
	private JLabel syncImageLabel = null;
	private JLabel poweredByLabel = null;

	private EktooUIController controller;

	// BUSINESS METHODS
	public EktooUI(EktooUIController controller) {
		super();
		initialize();
		this.controller = controller;
	}

	private void initialize() {
		this.setSize(new Dimension(800, 500));
		this.setContentPane(getJPanel());
		this.setIconImage(ImageManager.getLogoSmall());

		this.setTitle(EktooUITranslator.getTitle());
		this.filterCombobox();
		this.setResizable(false);
	}

	private JPanel getJPanel() {
		if (panel == null) {
			panel = new JPanel();

			panel.setBackground(Color.WHITE);
			GridBagLayout gridBagLayout = new GridBagLayout();

			panel.setLayout(gridBagLayout);
			GridBagConstraints c = new GridBagConstraints();

			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 2;
			c.weightx = .5;
			c.weighty = 1.0;
			panel.add(getImagePanel(), c);

			c.insets = new Insets(0, 5, 0, 5);

			// c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 1;
			panel.add(getSourcePane(), c);

			// c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			c.gridy = 1;
			panel.add(getTargetPane(), c);
			// panel.add(getViaPane(), null);
			// panel.add(getTypePane(), null);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 2;
			c.gridwidth = 2;
			panel.add(getConsole(), c);

			c.fill = GridBagConstraints.CENTER;
			c.gridx = 0;
			c.gridy = 3;
			c.gridwidth = 2;
			panel.add(getBtnSync(), c);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 4;
			c.gridwidth = 2;
			panel.add(getPoweredByLabel(), c);

		}
		return panel;
	}

	private JLabel getPoweredByLabel() {
		if (poweredByLabel == null) {
			poweredByLabel = new JLabel(EktooUITranslator.getPoweredByLabel(),
					ImageManager.getTrademarkIcon(), JLabel.RIGHT);
			poweredByLabel.setHorizontalTextPosition(JLabel.LEFT);
			poweredByLabel.setVerticalTextPosition(JLabel.CENTER);
			poweredByLabel.setIconTextGap(0);
			poweredByLabel.setForeground(Color.BLUE);
			poweredByLabel.setToolTipText(EktooUITranslator
					.getPoweredByLabelTooltip());
			poweredByLabel.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					OpenURLTask task = new OpenURLTask(EktooUI.this,
							EktooUI.this, new PropertiesProvider()
									.getMesh4xURL());
					task.execute();
				}
			});

		}
		return poweredByLabel;
	}

	private JPanel getImagePanel() {
		if (panelImage == null) {
			panelImage = new JPanel();
			panelImage.setOpaque(false);
			panelImage.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(-15, 17, 0, 17);

			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 3;

			JPanel tempPanel = new JPanel();
			tempPanel.setPreferredSize(new Dimension(428, 47));
			tempPanel.setOpaque(false);
			tempPanel.add(getSyncImageLabel());
			panelImage.add(tempPanel, c);

			// panelImage.add(getSyncImageLabel(), c);

			c.fill = GridBagConstraints.CENTER;
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 1;

			panelImage.add(getSourceImageLabel(), c);

			c.fill = GridBagConstraints.CENTER;
			c.gridx = 1;
			panelImage.add(getDirectionImageLabel(), c);

			c.fill = GridBagConstraints.CENTER;
			c.gridx = 2;
			panelImage.add(getTargetImageLabel(), c);
		}

		return panelImage;
	}

	private JLabel getSyncImageLabel() {
		if (syncImageLabel == null) {
			syncImageLabel = new JLabel(ImageManager.getSyncProcessIcon());
			syncImageLabel.setPreferredSize(new Dimension(428, 47));
			showSyncImageLabel(false);
		}
		return syncImageLabel;
	}

	public void showSyncImageLabel(boolean bool) {
		syncImageLabel.setVisible(bool);
	}

	private JLabel getSourceImageLabel() {
		if (sourceImageLabel == null) {
			sourceImageLabel = new JLabel(ImageManager.getUndefinedSourceIcon());
		}
		return sourceImageLabel;
	}

	private void setSourceIcon(Icon icon) {
		setIcon(sourceImageLabel, icon);
	}

	private void setTargetIcon(Icon icon) {
		setIcon(targetImageLabel, icon);
	}

	private void setIcon(JLabel label, Icon icon) {
		if (label != null || icon != null)
			label.setIcon(icon);
	}

	private JLabel getTargetImageLabel() {
		if (targetImageLabel == null) {
			targetImageLabel = new JLabel(ImageManager.getUndefinedSourceIcon());
		}
		return targetImageLabel;
	}

	private JLabel getDirectionImageLabel() {
		if (directionImageLabel == null) {
			directionImageLabel = new JLabel(ImageManager.getSyncModeIcon(true,
					true));
		}
		return directionImageLabel;
	}

	private JLabel getConsole() {
		if (labelConsole == null) {
			labelConsole = new JLabel();
			labelConsole.setPreferredSize(new Dimension(600, 24));
			labelConsole.setBorder(BorderFactory.createLineBorder(Color.gray));
		}

		return labelConsole;
	}

	private JPanel getSourcePane() {
		if (getSourceItem() == null) {
			setSourceItem(new SyncItemUI(EktooUITranslator
					.getSourceSyncItemSelectorTitle()));
			getSourceItem().setPreferredSize(new Dimension(350, 190));
			getSourceItem().getListType().addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						setSourceIcon(ImageManager.getSourceImage((String) evt
								.getItem(), false));
						filterCombobox();
					}
				}
			});
			setSourceIcon(ImageManager.getSourceImage((String) getSourceItem()
					.getListType().getSelectedItem(), false));
		}
		return getSourceItem();
	}

	private JPanel getTargetPane() {
		if (getTargetItem() == null) {
			setTargetItem(new SyncItemUI(EktooUITranslator
					.getTargetSyncItemSelectorTitle()));
			getTargetItem().setPreferredSize(new Dimension(350, 190));
			getTargetItem().getListType().addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						setTargetIcon(ImageManager.getSourceImage((String) evt
								.getItem(), false));
					}
				}
			});
			setTargetIcon(ImageManager.getSourceImage((String) getTargetItem()
					.getListType().getSelectedItem(), false));

		}

		return getTargetItem();
	}

	private JButton getBtnSync() {
		if (btnSync == null) {
			btnSync = new JButton();
			btnSync.setBounds(new Rectangle(315, 427, 127, 28));
			btnSync.setText(EktooUITranslator.getSyncLabel());
			btnSync.setToolTipText(EktooUITranslator.getSyncToolTip());
			btnSync.setFont(new Font("Arial", Font.PLAIN, 16));

			btnSync.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					log("actionPerformed()");
					setConsole("");
					showSyncImageLabel(true);
					SwingWorker<String, Void> task = new SynchronizeTask(
							EktooUI.this);
					task.execute();
					// showSyncImageLabel(false);
					log("Calling Sync...");
				}
			});

		}
		return btnSync;
	}

	private void log(String msg) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(msg);
		}
	}

	public void setController(EktooUIController controller) {
		this.controller = controller;
	}

	public EktooUIController getController() {
		return controller;
	}

	public void setSourceItem(SyncItemUI sourceItem) {
		this.sourceItem = sourceItem;
	}

	public SyncItemUI getSourceItem() {
		return sourceItem;
	}

	public void setTargetItem(SyncItemUI targetItem) {
		this.targetItem = targetItem;
	}

	public SyncItemUI getTargetItem() {
		return targetItem;
	}

	public void setConsole(String msg) {
		labelConsole.setText(msg);
	}

	// TODO (NBL) disables unsupported features from ui
	private void filterCombobox() {
		String item = (String) getSourceItem().getListType().getSelectedItem();
		if (item.equals(SyncItemUI.MS_EXCEL_PANEL)
				|| item.equals(SyncItemUI.RSS_FILE_PANEL)
				|| item.equals(SyncItemUI.ATOM_FILE_PANEL)) {
			getTargetItem().getListType().removeAllItems();
			getTargetItem().getListType().addItem(SyncItemUI.MS_EXCEL_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.MS_ACCESS_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.GOOGLE_SPREADSHEET_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.MYSQL_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.CLOUD_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.RSS_FILE_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.ATOM_FILE_PANEL);
		} else if (item.equals(SyncItemUI.MS_ACCESS_PANEL)) {
			getTargetItem().getListType().removeAllItems();
			getTargetItem().getListType().addItem(SyncItemUI.MS_ACCESS_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.MS_EXCEL_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.MYSQL_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.CLOUD_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.RSS_FILE_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.ATOM_FILE_PANEL);
		} else if (item.equals(SyncItemUI.GOOGLE_SPREADSHEET_PANEL)) {
			getTargetItem().getListType().removeAllItems();
			getTargetItem().getListType().addItem(SyncItemUI.GOOGLE_SPREADSHEET_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.MS_EXCEL_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.CLOUD_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.RSS_FILE_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.ATOM_FILE_PANEL);
		} else if (item.equals(SyncItemUI.MYSQL_PANEL)) {
			getTargetItem().getListType().removeAllItems();
			getTargetItem().getListType().addItem(SyncItemUI.MS_EXCEL_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.MS_ACCESS_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.MYSQL_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.CLOUD_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.RSS_FILE_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.ATOM_FILE_PANEL);
		} else if (item.equals(SyncItemUI.CLOUD_PANEL)) {
			getTargetItem().getListType().removeAllItems();
			getTargetItem().getListType().addItem(SyncItemUI.KML_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.MS_EXCEL_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.MS_ACCESS_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.GOOGLE_SPREADSHEET_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.MYSQL_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.RSS_FILE_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.ATOM_FILE_PANEL);
		} else if (item.equals(SyncItemUI.KML_PANEL)) {
			getTargetItem().getListType().removeAllItems();
			getTargetItem().getListType().addItem(SyncItemUI.CLOUD_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.KML_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.RSS_FILE_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.ATOM_FILE_PANEL);
		} else if (item.equals(SyncItemUI.FOLDER_PANEL)) {
			getTargetItem().getListType().removeAllItems();
			getTargetItem().getListType().addItem(SyncItemUI.FOLDER_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.CLOUD_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.RSS_FILE_PANEL);
			getTargetItem().getListType().addItem(SyncItemUI.ATOM_FILE_PANEL);
		} else {
			getTargetItem().getListType().removeAllItems();
			setTargetIcon(ImageManager.getUndefinedSourceIcon());
			getTargetItem().showInitCard();
		}

	}

	@Override
	public void notifyError(String error) {
		// TODO Auto-generated method stub

	}
}
