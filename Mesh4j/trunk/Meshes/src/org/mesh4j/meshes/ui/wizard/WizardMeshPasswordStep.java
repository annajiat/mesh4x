package org.mesh4j.meshes.ui.wizard;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JLabel;
import javax.swing.JPasswordField;

import net.miginfocom.swing.MigLayout;

import org.mesh4j.meshes.controller.CreateMeshWizardController;

public class WizardMeshPasswordStep extends BaseWizardPanel {

	private static final long serialVersionUID = -5220841948982059704L;
	private static String ID = "STEP_TWO";
	
	private CreateMeshWizardController controller;
	
	private JPasswordField passwordField;
	private JPasswordField confirmPasswordField;
	
	public WizardMeshPasswordStep(CreateMeshWizardController controller) {
		super();
		this.controller = controller;
		initComponents();
	}

	private void initComponents() {
		setLayout(new MigLayout("insets 10"));
		setSize(550, 350);
		
		JLabel titleLabel = new JLabel("<html><h2>Secure Access to your mesh</h2></html>");
		add(titleLabel, "span");
		
		JLabel subTitleLabel = new JLabel();
		subTitleLabel.setText("<html><h4>You should secure access to your mesh data. Please enter the username and password " +
							  "you want to use to share data with your mesh. Many users and applications can use the same " +
							  "password if needed</h4></html>");
		add(subTitleLabel, "span, wrap 10");
		
		JLabel passwordLabel = new JLabel("Password");
		passwordField = new JPasswordField();
		passwordField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				passwordFieldKeyReleased(e);
				
			}
		});
		add(passwordLabel, "gapright 20");
		add(passwordField, "growx, wrap");
		
		JLabel confirmPasswordLabel = new JLabel("Confirm Password");
		confirmPasswordField = new JPasswordField();
		confirmPasswordField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				confirmPasswordFieldKeyReleased(e);
			}
		});
		add(confirmPasswordLabel, "gapright 20");
		add(confirmPasswordField, "growx");
		
		WizardUtils.nextWhenEnterPressedOn(controller, passwordField, confirmPasswordLabel);
		WizardUtils.nextWhenEnterPressedOn(controller, confirmPasswordField, confirmPasswordLabel);
	}

	private void passwordFieldKeyReleased(KeyEvent evt) {
		String password = new String(passwordField.getPassword());
		controller.setValue("mesh.password", password);	
	}
	
	private void confirmPasswordFieldKeyReleased(KeyEvent evt) {
		String confirmPassword = new String(confirmPasswordField.getPassword());
		controller.setValue("mesh.password", confirmPassword);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
	}

	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public String getErrorMessage() {
		String password = new String(passwordField.getPassword());
		String confirmPassword = new String(confirmPasswordField.getPassword());
		
		if (password.length() < 5)
			return "The password is too short: it must be at least 5 characters long";
		if (!password.equals(confirmPassword))
			return "The password confirmation does not match the password";
		return null;
	}
	
}
