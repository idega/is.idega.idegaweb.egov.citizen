/*
 * $Id$
 * Created on Jan 24, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.citizen.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import javax.ejb.FinderException;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserSession;
import com.idega.user.data.User;
import com.idega.util.EmailValidator;


public class ChangeEmail extends CitizenBlock {

	private final static String PARAMETER_ACTION = "prm_action";
	private final static String PARAMETER_EMAIL = "prm_email";
	private final static String PARAMETER_EMAIL_REPEAT = "prm_email_repeat";
	
	private final static String ACTION_VIEW_FORM = "action_view_form";
	private final static String ACTION_FORM_SUBMIT = "action_form_submit";

	private boolean iUseSessionUser = false;

	private IWResourceBundle iwrb;

	public void present(IWContext iwc) {
		this.iwrb = getResourceBundle(iwc);

		try {
			String action = parseAction(iwc);
			if (ACTION_VIEW_FORM.equals(action)) {
				viewForm(iwc);
			}
			else if (ACTION_FORM_SUBMIT.equals(action)) {
				submitForm(iwc);
			}
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}
	
	/**
	 * Parses the parameter string.
	 * 
	 * @param iwc
	 * @return either string for action "view form" or string for action "form was
	 *         submitted".
	 */
	private String parseAction(final IWContext iwc) {
		String action = ACTION_VIEW_FORM;
		if (iwc.isParameterSet(PARAMETER_ACTION)) {
			action = ACTION_FORM_SUBMIT;
		}
		return action;
	}

	private void submitForm(IWContext iwc) throws RemoteException {
		String email = iwc.getParameter(PARAMETER_EMAIL);
		String emailRepeat = iwc.getParameter(PARAMETER_EMAIL_REPEAT);

		boolean hasErrors = false;
		Collection errors = new ArrayList();
		
		if (email == null || email.length() == 0) {
			errors.add(this.iwrb.getLocalizedString("must_provide_email", "You have to enter an e-mail address."));
			hasErrors = true;
		}
		else if (!EmailValidator.getInstance().validateEmail(email)) {
			errors.add(this.iwrb.getLocalizedString("not_a_valid_email", "The e-mail address you have entered is not valid."));
			hasErrors = true;
		}
		
		if (emailRepeat == null || emailRepeat.length() == 0) {
			errors.add(this.iwrb.getLocalizedString("must_provide_email_repeat", "You have to enter repeat e-mail address."));
			hasErrors = true;
		}
		else if (!email.equals(emailRepeat)) {
			errors.add(this.iwrb.getLocalizedString("emails_dont_match", "The e-mail addresses you have entered don't match."));
			hasErrors = true;
		}

		User user = getUser(iwc);
		if (user != null && !hasErrors) {
			getUserBusiness(iwc).storeUserEmail(user, email, true);
		}
		
		if (!hasErrors) {
			Form form = new Form();
			form.setID("changeEmailForm");
			form.setStyleClass("citizenForm");

			Layer header = new Layer(Layer.DIV);
			header.setStyleClass("header");
			form.add(header);
			
			Heading1 heading = new Heading1(this.iwrb.getLocalizedString("change_email", "Change e-mail"));
			header.add(heading);
			
			Layer layer = new Layer(Layer.DIV);
			layer.setStyleClass("receipt");
			
			Layer image = new Layer(Layer.DIV);
			image.setStyleClass("receiptImage");
			layer.add(image);
			
			heading = new Heading1(this.iwrb.getLocalizedString("new_email_saved", "New e-mail saved"));
			layer.add(heading);
			
			Paragraph paragraph = new Paragraph();
			paragraph.add(new Text(this.iwrb.getLocalizedString("new_email_saved_text", "The new e-mail has been saved.")));
			layer.add(paragraph);
			
			ICPage userHomePage = null;
			try {
				UserBusiness ub = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
				userHomePage = ub.getHomePageForUser(user);
			}
			catch (FinderException fe) {
				//No page found...
			}
			catch (RemoteException re) {
				throw new IBORuntimeException(re);
			}
			
			if (userHomePage != null) {
				Layer span = new Layer(Layer.SPAN);
				span.add(new Text(this.iwrb.getLocalizedString("my_page", "My page")));
				Link link = new Link(span);
				link.setStyleClass("homeLink");
				link.setPage(userHomePage);
				paragraph.add(new Break(2));
				paragraph.add(link);
			}
						
			form.add(layer);
			add(form);
		}
		else {
			showErrors(iwc, errors);
			viewForm(iwc);
		}
	}

	/**
	 * Builds a presentation containing the form with input field and submit
	 * button.
	 * 
	 * @param iwc
	 */
	private void viewForm(final IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.addParameter(PARAMETER_ACTION, Boolean.TRUE.toString());
		form.setID("changeEmailForm");
		form.setStyleClass("citizenForm");
		
		User user = getUser(iwc);
		if (user != null) {
			LoginTable loginTable = LoginDBHandler.getUserLogin(user);
			if (loginTable == null) {
				Layer header = new Layer(Layer.DIV);
				header.setStyleClass("header");
				form.add(header);
				
				Heading1 heading = new Heading1(this.iwrb.getLocalizedString("change_email", "Change e-mail"));
				header.add(heading);
				
				Layer layer = new Layer(Layer.DIV);
				layer.setStyleClass("stop");
				
				Layer image = new Layer(Layer.DIV);
				image.setStyleClass("stopImage");
				layer.add(image);
				
				heading = new Heading1(this.iwrb.getLocalizedString("user_has_no_account", "User has no account"));
				layer.add(heading);
				
				Paragraph paragraph = new Paragraph();
				paragraph.add(new Text(this.iwrb.getLocalizedString("user_has_no_login", "The user you are trying to change e-mail for doesn't have an account.")));
				layer.add(paragraph);
				
				Link link = new Link(this.iwrb.getLocalizedString("back", "Back"));
				link.setStyleClass("homeLink");
				link.setAsBackLink();
				paragraph.add(new Break(2));
				paragraph.add(link);
				
				form.add(layer);
			}
			else {
				Layer header = new Layer(Layer.DIV);
				header.setStyleClass("header");
				form.add(header);
				
				Heading1 heading = new Heading1(this.iwrb.getLocalizedString("change_email", "Change e-mail"));
				header.add(heading);
				
				Layer section = new Layer(Layer.DIV);
				section.setStyleClass("formSection");
				form.add(section);
				
				Paragraph paragraph = new Paragraph();
				paragraph.add(new Text(this.iwrb.getLocalizedString("change_email_helper_text", "Please enter the new e-mail and click 'Save'.")));
				section.add(paragraph);
				
				TextInput email = new TextInput(PARAMETER_EMAIL);
				email.keepStatusOnAction(true);

				TextInput emailRepeat = new TextInput(PARAMETER_EMAIL_REPEAT);
				emailRepeat.keepStatusOnAction(true);

				Layer formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				Label label = new Label(this.iwrb.getLocalizedString("new_email", "New e-mail"), email);
				formItem.add(label);
				formItem.add(email);
				section.add(formItem);
				
				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				label = new Label(this.iwrb.getLocalizedString("new_email_repeat", "New e-mail repeat"), emailRepeat);
				formItem.add(label);
				formItem.add(emailRepeat);
				section.add(formItem);
				
				Layer clearLayer = new Layer(Layer.DIV);
				clearLayer.setStyleClass("Clear");
				section.add(clearLayer);

				Layer buttonLayer = new Layer(Layer.DIV);
				buttonLayer.setStyleClass("buttonLayer");
				form.add(buttonLayer);
				
				Layer span = new Layer(Layer.SPAN);
				span.add(new Text(this.iwrb.getLocalizedString("save", "Save")));
				Link send = new Link(span);
				send.setStyleClass("sendLink");
				send.setToFormSubmit(form);
				buttonLayer.add(send);
			}
		}

		add(form);
	}

	private User getUser(IWContext iwc) throws RemoteException {
		if (this.iUseSessionUser) {
			return getUserSession(iwc).getUser();
		}
		else {
			return iwc.getCurrentUser();
		}
	}

	private UserBusiness getUserBusiness(IWApplicationContext iwac) {
		try {
			return (UserBusiness) IBOLookup.getServiceInstance(iwac, UserBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
	
	private UserSession getUserSession(IWUserContext iwuc) {
		try {
			return (UserSession) IBOLookup.getSessionInstance(iwuc, UserSession.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
	
	public void setUseSessionUser(boolean useSessionUser) {
		this.iUseSessionUser = useSessionUser;
	}
}