package org.mesh4j.ektoo.ui.validator;

import java.util.List;

import javax.swing.JComponent;

import org.mesh4j.ektoo.controller.MySQLUIController;
import org.mesh4j.ektoo.model.AbstractModel;
import org.mesh4j.ektoo.model.MySQLAdapterModel;
import org.mesh4j.ektoo.ui.MySQLUI;
import org.mesh4j.ektoo.ui.translator.EktooUITranslator;
import org.mesh4j.ektoo.validator.AbstractValidator;

/**
 * @author Bhuiyan Mohammad Iklash
 * 
 */
public class MySQLConnectionValidator extends AbstractValidator {
	
	
	// BUSINESS METHODS
//	public MySQLConnectionValidator(JComponent form, AbstractModel model) {
//		super(form, model);
//	}
	
/*	public MySQLConnectionValidator(JComponent form, AbstractModel model, boolean mustValidateTable) {
		super(form, model);
		this.mustValidateTable = mustValidateTable;
	}*/
	
	public MySQLConnectionValidator(JComponent form, AbstractModel model, List<JComponent> uiFieldListForValidation) {
		super(form, model, uiFieldListForValidation);
	}
	
	@Override
	protected boolean validate() {
		// model based validation
		// return validate((MySQLAdapterModel)this.model);
		
		// form based validation
		return validate((MySQLUI) this.form);
	
	}

	protected boolean validate(MySQLAdapterModel mysqlModel) {
		boolean isValid = true;
		return isValid;
	}

	private boolean validate(JComponent form) {
		boolean isValid = true;

		MySQLUI ui = (MySQLUI) form;

		if ((getUiFieldListForValidation().isEmpty() || getUiFieldListForValidation().contains(ui.getUserText()))
				&& ui.getUserText().getText().trim().length() == 0) {
			setError(ui.getUserText(), EktooUITranslator
					.getErrorEmptyOrNull(MySQLUIController.USER_NAME_PROPERTY));
			isValid = false;
		}

//		if ((getUiFieldListForValidation().isEmpty() || getUiFieldListForValidation().contains(ui.getPassText()))
//				&& new String(ui.getPassText().getPassword()).trim().length() == 0) {
//			setError(ui.getPassText(), EktooUITranslator
//					.getErrorEmptyOrNull(MySQLUIController.USER_PASSWORD_PROPERTY));
//			isValid = false;
//		}

		if ((getUiFieldListForValidation().isEmpty() || getUiFieldListForValidation().contains(ui.getHostText()))
				&& ui.getHostText().getText().trim().length() == 0) {
			setError(ui.getHostText(), EktooUITranslator
					.getErrorEmptyOrNull(MySQLUIController.HOST_NAME_PROPERTY));
			isValid = false;
		}

		if ((getUiFieldListForValidation().isEmpty() || getUiFieldListForValidation().contains(ui.getPortText()))
				&& ui.getPortText().getText().trim().length() == 0) {
			setError(ui.getPortText(), EktooUITranslator
					.getErrorEmptyOrNull(MySQLUIController.PORT_NO_PROPERTY));
			isValid = false;
		}

		if ((getUiFieldListForValidation().isEmpty() || getUiFieldListForValidation().contains(ui.getDatabaseText()))
				&& ui.getDatabaseText().getText().trim().length() == 0) {
			setError(ui.getDatabaseText(), EktooUITranslator
					.getErrorEmptyOrNull(MySQLUIController.DATABASE_NAME_PROPERTY));
			isValid = false;
		}

		//validating the table 
		//if(mustValidateTable){
		if ((getUiFieldListForValidation().isEmpty() || getUiFieldListForValidation().contains(ui.getTableList()))
				&& (ui.getTableList().getModel().getSize() == 0 || ui.getTableList().getSelectedIndices().length == 0)) {
			
			if(ui.getTableList().getModel().getSize() == 0)
				setError(ui.getTableList(), EktooUITranslator.getErrorEmptyOrNull(MySQLUIController.TABLE_NAME_PROPERTY));
			else
				setError(ui.getTableList(), EktooUITranslator.getErrorEmptySelection(MySQLUIController.TABLE_NAME_PROPERTY));
			
			isValid = false;
		}
		//} 

		return isValid;
	}

//	public boolean validateToConnect() {
//		return validate((MySQLUI) this.form); 
//	}
}
