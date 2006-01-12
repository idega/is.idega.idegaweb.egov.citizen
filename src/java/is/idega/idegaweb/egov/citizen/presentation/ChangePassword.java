/*
 * $Id$ Created on
 * 24.3.2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package is.idega.idegaweb.egov.citizen.presentation;

import java.rmi.RemoteException;
import javax.ejb.FinderException;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.ExceptionWrapper;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;


public class ChangePassword extends Block {

	private final static String IW_BUNDLE_IDENTIFIER = "se.idega.idegaweb.commune";

	private final static int ACTION_VIEW_FORM = 1;
	private final static int ACTION_FORM_SUBMIT = 2;

	private final static String PARAMETER_FORM_SUBMIT = "cap_sbmt";

	private final static String PARAMETER_CURRENT_PASSWORD = "cap_c_pw";
	private final static String PARAMETER_NEW_PASSWORD = "cap_n_pw";
	private final static String PARAMETER_NEW_PASSWORD_REPEATED = "cap_n_pw_r";

	private int MIN_PASSWORD_LENGTH = 8;
	
	private final static String KEY_PREFIX = "citizen.";
	private final static String KEY_CURRENT_PASSWORD = KEY_PREFIX + "current_password";
	private final static String KEY_NEW_PASSWORD = KEY_PREFIX + "new_password";
	private final static String KEY_NEW_PASSWORD_REPEATED = KEY_PREFIX + "new_password_repeated";
	private final static String KEY_UPDATE = KEY_PREFIX + "update";

	private final static String KEY_PASSWORD_EMPTY = KEY_PREFIX + "password_empty";
	private final static String KEY_PASSWORD_REPEATED_EMPTY = KEY_PREFIX + "password_repeated_empty";
	private final static String KEY_PASSWORDS_NOT_SAME = KEY_PREFIX + "passwords_not_same";
	private final static String KEY_PASSWORD_INVALID = KEY_PREFIX + "invalid_password";	
	private final static String KEY_PASSWORD_TOO_SHORT = KEY_PREFIX + "password_too_short";	
	private final static String KEY_PASSWORD_CHAR_ILLEGAL = KEY_PREFIX + "password_char_illegal";	
	private final static String KEY_PASSWORD_SAVED = KEY_PREFIX + "password_saved";	

	private final static String DEFAULT_CURRENT_PASSWORD = "Current password";	
	private final static String DEFAULT_NEW_PASSWORD = "New password";	
	private final static String DEFAULT_NEW_PASSWORD_REPEATED = "Repeat new password";	
	private final static String DEFAULT_UPDATE = "Update";	

	private final static String DEFAULT_PASSWORD_EMPTY = "Password cannot be empty.";		
	private final static String DEFAULT_PASSWORD_REPEATED_EMPTY = "Repeated password cannot be empty.";		
	private final static String DEFAULT_PASSWORDS_NOT_SAME = "New passwords not the same.";		
	private final static String DEFAULT_PASSWORD_INVALID = "Invalid password.";		
	private final static String DEFAULT_PASSWORD_TOO_SHORT = "Password too short.";		
	private final static String DEFAULT_PASSWORD_CHAR_ILLEGAL = "Password contains illegal character(s).";		
	private final static String DEFAULT_PASSWORD_SAVED = "Your password has been saved.";	

	private User user = null;
	private IWResourceBundle iwrb;
		
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	private int parseAction (final IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_FORM_SUBMIT)) {
			return ACTION_FORM_SUBMIT;
		}
		else {
			return ACTION_VIEW_FORM;
		}
	}
	
	public void main(IWContext iwc) {
		if (!iwc.isLoggedOn()) {
			return;
		}
		iwrb = getResourceBundle(iwc);
		user = iwc.getCurrentUser();

		try {
			int action = parseAction(iwc);
			switch (action) {
				case ACTION_VIEW_FORM:
					drawForm(iwc);
					break;
				case ACTION_FORM_SUBMIT:
					updatePassword(iwc);
					break;
			}
		}
		catch (Exception e) {
			super.add(new ExceptionWrapper(e, this));
		}    
	}

	private void drawForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		
		Table table = new Table();	
		table.setColumns(2);
		table.setCellpadding(2);
		form.add(table);
		int row = 1;

		UserBusiness ub = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);

		String valueCurrentPassword = iwc.getParameter(PARAMETER_CURRENT_PASSWORD) != null ? iwc.getParameter(PARAMETER_CURRENT_PASSWORD) : "";
		String valueNewPassword = iwc.getParameter(PARAMETER_NEW_PASSWORD) != null ? iwc.getParameter(PARAMETER_NEW_PASSWORD) : "";
		String valueNewPasswordRepeated = iwc.getParameter(PARAMETER_NEW_PASSWORD_REPEATED) != null ? iwc.getParameter(PARAMETER_NEW_PASSWORD_REPEATED) : "";

		Text tCurrentPassword = new Text(iwrb.getLocalizedString(KEY_CURRENT_PASSWORD, DEFAULT_CURRENT_PASSWORD));
		Text tNewPassword = new Text(iwrb.getLocalizedString(KEY_NEW_PASSWORD, DEFAULT_NEW_PASSWORD));
		Text tNewPasswordRepeated = new Text(iwrb.getLocalizedString(KEY_NEW_PASSWORD_REPEATED, DEFAULT_NEW_PASSWORD_REPEATED));

		PasswordInput tiCurrentPassword = new PasswordInput(PARAMETER_CURRENT_PASSWORD);	
		if(valueCurrentPassword!=null){
			tiCurrentPassword.setValue(valueCurrentPassword);
		}

		PasswordInput tiNewPassword = new PasswordInput(PARAMETER_NEW_PASSWORD);
		if(valueNewPassword!=null){
			tiNewPassword.setValue(valueNewPassword);
		}
		
		PasswordInput tiNewPasswordRepeated = new PasswordInput(PARAMETER_NEW_PASSWORD_REPEATED);
		if(valueNewPasswordRepeated!=null){
			tiNewPasswordRepeated.setValue(valueNewPasswordRepeated);
		}

		SubmitButton sbUpdate = new SubmitButton(iwrb.getLocalizedString(KEY_UPDATE, DEFAULT_UPDATE), PARAMETER_FORM_SUBMIT, "true");
		
		table.add(tCurrentPassword, 1, row);
		table.add(tiCurrentPassword, 2, row++);

		table.add(tNewPassword, 1, row);
		table.add(tiNewPassword, 2, row++);

		table.add(tNewPasswordRepeated, 1, row);
		table.add(tiNewPasswordRepeated, 2, row++);
		
		table.setHeight(row++, 12);
		
		ICPage homepage = null;
		try {
			homepage = ub.getHomePageForUser(user);
		}
		catch (FinderException fe) {
			homepage = null;
		}
		
		table.add(sbUpdate, 1, row);
		if (homepage != null) {
			table.add(new Text(Text.NON_BREAKING_SPACE), 1, row);
			GenericButton home = new GenericButton("home", iwrb.getLocalizedString("my_page", "Back to My Page"));
			home.setPageToOpen(homepage);
			table.add(home, 1, row);
		}
		table.add(new Text(Text.NON_BREAKING_SPACE), 1, row);
		
		add(form);
	}
	
	private void updatePassword(IWContext iwc)  throws Exception {
		LoginTable loginTable = LoginDBHandler.getUserLogin(((Integer) user.getPrimaryKey()).intValue());
		String login    = loginTable.getUserLogin();
		String currentPassword = iwc.getParameter(PARAMETER_CURRENT_PASSWORD);
		String newPassword1 = iwc.getParameter(PARAMETER_NEW_PASSWORD);
		String newPassword2 = iwc.getParameter(PARAMETER_NEW_PASSWORD_REPEATED);		

		String errorMessage = null;
		boolean updatePassword = false;
		
		try {
			if (!LoginDBHandler.verifyPassword(login, currentPassword)) {
				throw new Exception(iwrb.getLocalizedString(KEY_PASSWORD_INVALID, DEFAULT_PASSWORD_INVALID));
			}

			// Validate new password
			if (!newPassword1.equals("") || !newPassword2.equals("")) {
				if (newPassword1.equals("")) {
					throw new Exception(iwrb.getLocalizedString(KEY_PASSWORD_EMPTY, DEFAULT_PASSWORD_EMPTY));
				}
				if (newPassword2.equals("")) {
					throw new Exception(iwrb.getLocalizedString(KEY_PASSWORD_REPEATED_EMPTY, DEFAULT_PASSWORD_REPEATED_EMPTY));
				}
				if (!newPassword1.equals(newPassword2)) {
					throw new Exception(iwrb.getLocalizedString(KEY_PASSWORDS_NOT_SAME, DEFAULT_PASSWORDS_NOT_SAME));
				}
				if (newPassword1.length() < MIN_PASSWORD_LENGTH) {
					throw new Exception(iwrb.getLocalizedString(KEY_PASSWORD_TOO_SHORT, DEFAULT_PASSWORD_TOO_SHORT));
				}
				for (int i = 0; i < newPassword1.length(); i++) {
					char c = newPassword1.charAt(i);
					boolean isPasswordCharOK = false;
					if ((c >= 'a') && (c <= 'z')) {
						isPasswordCharOK = true;
					} else if ((c >= 'A') && (c <= 'Z')) {
						isPasswordCharOK = true;
					} else if ((c >= '0') && (c <= '9')) {
						isPasswordCharOK = true;
					} else if ((c == 'Œ') || (c == 'Š') || (c == 'š')) {
						isPasswordCharOK = true;
					} else if ((c == '') || (c == '€') || (c == '…')) {
						isPasswordCharOK = true;
					}
					if (!isPasswordCharOK) {
						throw new Exception(iwrb.getLocalizedString(KEY_PASSWORD_CHAR_ILLEGAL, DEFAULT_PASSWORD_CHAR_ILLEGAL));
					}
				}
				updatePassword = true;
			}
		}
		catch (Exception e) {
			errorMessage = e.getMessage();
		}


		if (errorMessage != null) {
			add(new Text(" " + errorMessage));
		}
		else {
			// Ok to update password
			if (updatePassword) {
				LoginDBHandler.updateLogin(((Integer)user.getPrimaryKey()).intValue(), login, newPassword1);
			}
			
			drawForm(iwc);
			if (errorMessage == null) {
				if (getParentPage() != null) {
					getParentPage().setAlertOnLoad(iwrb.getLocalizedString(KEY_PASSWORD_SAVED, DEFAULT_PASSWORD_SAVED));
				}
				else {
					add(new Break());
					add(iwrb.getLocalizedString(KEY_PASSWORD_SAVED, DEFAULT_PASSWORD_SAVED));
				}
			}
		}
	}
	
	public void setMinimumPasswordLength(int length) {
		MIN_PASSWORD_LENGTH = length;
	}
}