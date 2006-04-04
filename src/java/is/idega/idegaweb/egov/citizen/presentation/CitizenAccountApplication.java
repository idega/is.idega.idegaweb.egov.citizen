/*
 * Created on 2004-maj-11
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */

/**
 * @author Malin
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */

package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.idegaweb.egov.citizen.business.WSCitizenAccountBusiness;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import se.idega.idegaweb.commune.account.citizen.business.CitizenAccountBusiness;

import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.UserHasLoginException;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.LoginTableHome;
import com.idega.core.builder.data.ICPage;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.Commune;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.ExceptionWrapper;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;
import com.idega.util.Age;
import com.idega.util.IWTimestamp;
import com.idega.util.text.SocialSecurityNumber;

/**
 * Last modified: $Date$ by $Author$
 * 
 * @author <a href="mail:laddi@idega.is">Laddi</a>
 * @version $Revision$
 */
public class CitizenAccountApplication extends CitizenBlock {

	private final static int ACTION_VIEW_FORM = 0;
	private final static int ACTION_SUBMIT_SIMPLE_FORM = 1;
	
	private static final String ATTRIBUTE_VALID_ACCOUNT_AGE = "citizen_account_minimum_age";
	private static final String ATTRIBUTE_VALID_ACCOUNT_AGE_YEAR = "citizen_account_minimum_age_this_year";

	private final static String EMAIL_DEFAULT = "Email";
	private final static String EMAIL_KEY = "email";
	private final static String EMAIL_KEY_REPEAT = "email_repeat";
	private final static String EMAIL_REPEAT_DEFAULT = "Email again";
	private final static String PHONE_HOME_KEY = "home_phone";
	private final static String PHONE_HOME_DEFAULT = "Phone";
	private final static String PHONE_CELL_KEY = "mobile_phone";
	private final static String PHONE_CELL_DEFAULT = "Cell phone";
	private final static String UNKNOWN_CITIZEN_KEY = "unknown_citizen";
	private final static String UNKNOWN_CITIZEN_DEFAULT = "Something is wrong with your personal id. Please try again or contact the responsible";
	private final static String NOT_VALID_ACCOUNT_AGE_KEY = "not_valid_citizen_account_age";
	private final static String NOT_VALID_ACCOUNT_AGE_DEFAULT = "You have to be {0} to apply for a citizen account";
	private final static String COMMUNE_DEFAULT = "Commune";
	private final static String COMMUNE_KEY = "commmune";

	private String communeUniqueIdsCSV;

	private final static String SSN_DEFAULT = "Personal ID";
	protected 	final static String SSN_KEY = "personal_id";
	private final static String TEXT_APPLICATION_SUBMITTED_DEFAULT = "Application is submitted.";
	private final static String TEXT_APPLICATION_SUBMITTED_KEY = "application_submitted";

	private final static String USER_ALLREADY_HAS_A_LOGIN_DEFAULT = "You already have an account";
	private final static String USER_ALLREADY_HAS_A_LOGIN_KEY = "user_already_has_an_account";

	private final static String SIMPLE_FORM_SUBMIT_KEY = "scaa_simpleSubmit";
	private final static String SIMPLE_FORM_SUBMIT_DEFAULT = "Forward >>";

	private final static String ERROR_NO_INSERT_KEY = "unable_to_store";
	private final static String ERROR_EMAILS_DONT_MATCH = "emails_dont_match";
	private final static String ERROR_EMAILS_DONT_MATCH_DEFAULT = "Emails don't match";
	private static final String ERROR_APPLYING_FOR_WRONG_COMMUNE = "user_in_wrong_commune";
	private static final String ERROR_APPLYING_FOR_WRONG_COMMUNE_DEFAULT = "You do not belong to the commune you are applying for according to our records and the application cannot be finished. Please contact your commune if you think this is an error.";

	private IWResourceBundle iwrb;
	private ICPage iPage;
	private int iRedirectDelay = 15;
	
	private boolean iForwardToURL = false;
	private Map iCommuneMap;
	
	public void present(IWContext iwc) {
		iwrb = getResourceBundle(iwc);

		try {
			int action = parseAction(iwc);
			switch (action) {
				case ACTION_VIEW_FORM:
					viewSimpleApplicationForm(iwc);
					break;
				case ACTION_SUBMIT_SIMPLE_FORM:
					submitSimpleForm(iwc);
					break;
			}
		}
		catch (Exception e) {
			super.add(new ExceptionWrapper(e, this));
		}
	}

	private void viewSimpleApplicationForm(IWContext iwc) {
		Form form = new Form();
		form.addParameter(SIMPLE_FORM_SUBMIT_KEY, Boolean.TRUE.toString());
		form.setID("accountApplicationForm");
		form.setStyleClass("citizenForm");
		
		Layer header = new Layer(Layer.DIV);
		header.setStyleClass("header");
		form.add(header);
		
		Heading1 heading = new Heading1(iwrb.getLocalizedString("citizen_registration", "Citizen registration"));
		header.add(heading);
		
		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);
		
		Layer helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(iwrb.getLocalizedString("citizen_registraction_help", "Please fill in your personal ID as well as your e-mail.  Your e-mail is required so that you can be contacted directly about changes to you ongoing cases.  If you don't have an e-mail account please contact the commune offices.")));
		section.add(helpLayer);
		
		TextInput personalID = new TextInput(SSN_KEY);
		personalID.keepStatusOnAction(true);

		TextInput email = new TextInput(EMAIL_KEY);
		email.keepStatusOnAction(true);
		
		TextInput emailRepeat = new TextInput(EMAIL_KEY_REPEAT);
		emailRepeat.keepStatusOnAction(true);
		
		TextInput mobile = new TextInput(PHONE_CELL_KEY);
		mobile.keepStatusOnAction(true);
		
		TextInput homePhone = new TextInput(PHONE_HOME_KEY);
		homePhone.keepStatusOnAction(true);
		
		Layer required = new Layer(Layer.SPAN);
		required.setStyleClass("required");
		required.add(new Text("*"));
		
		if (iCommuneMap != null) {
			DropdownMenu communes = new DropdownMenu(COMMUNE_KEY);
			Iterator iter = iCommuneMap.keySet().iterator();
			while (iter.hasNext()) {
				String commune = (String) iter.next();
				communes.addMenuElement((String) iCommuneMap.get(commune), commune);
			}

			Layer formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			Label label = new Label(communes);
			label.add(new Text(iwrb.getLocalizedString(COMMUNE_KEY, COMMUNE_DEFAULT)));
			formItem.add(label);
			formItem.add(communes);
			section.add(formItem);
		}

		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		Label label = new Label(personalID);
		label.add(new Text(iwrb.getLocalizedString(SSN_KEY, SSN_DEFAULT)));
		label.add(required);
		formItem.add(label);
		formItem.add(personalID);
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(email);
		label.add(new Text(iwrb.getLocalizedString(EMAIL_KEY, EMAIL_DEFAULT)));
		label.add(required);
		formItem.add(label);
		formItem.add(email);
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(emailRepeat);
		label.add(new Text(iwrb.getLocalizedString(EMAIL_KEY_REPEAT, EMAIL_REPEAT_DEFAULT)));
		label.add(required);
		formItem.add(label);
		formItem.add(emailRepeat);
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(iwrb.getLocalizedString(PHONE_CELL_KEY, PHONE_CELL_DEFAULT), mobile);
		formItem.add(label);
		formItem.add(mobile);
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(iwrb.getLocalizedString(PHONE_HOME_KEY, PHONE_HOME_DEFAULT), homePhone);
		formItem.add(label);
		formItem.add(homePhone);
		section.add(formItem);
		
		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");
		section.add(clearLayer);
		
		Paragraph paragraph = new Paragraph();
		paragraph.setStyleClass("requiredInfo");
		paragraph.add(required);
		paragraph.add(new Text(iwrb.getLocalizedString("required_information", "Required information")));
		form.add(paragraph);

		Layer buttonLayer = new Layer(Layer.DIV);
		buttonLayer.setStyleClass("buttonLayer");
		form.add(buttonLayer);
		
		Layer span = new Layer(Layer.SPAN);
		span.add(new Text(iwrb.getLocalizedString(SIMPLE_FORM_SUBMIT_KEY + "_button", SIMPLE_FORM_SUBMIT_DEFAULT)));
		Link send = new Link(span);
		send.setToFormSubmit(form);
		buttonLayer.add(send);
		
		add(form);
	}

	private void submitSimpleForm(IWContext iwc) throws RemoteException {
		if (iwc.isParameterSet(COMMUNE_KEY) && iForwardToURL) {
			iwc.forwardToURL(getParentPage(), iwc.getParameter(COMMUNE_KEY), true);
			return;
		}
		
		String ssn = iwc.getParameter(SSN_KEY);
		
		boolean hasErrors = false;
		Collection errors = new ArrayList();
		
		if (ssn == null ||ssn.length() == 0) {
			errors.add(iwrb.getLocalizedString("must_provide_personal_id", "You have to enter a personal ID."));
			hasErrors = true;
		}
		else if (!SocialSecurityNumber.isValidIcelandicSocialSecurityNumber(ssn)) {
			errors.add(iwrb.getLocalizedString("not_a_valid_personal_id", "The personal ID you've entered is not valid."));
			hasErrors = true;
		}
		if (!isValidAge(iwc, ssn)) { 
			Object[] arguments = { iwc.getApplicationSettings().getProperty(ATTRIBUTE_VALID_ACCOUNT_AGE, String.valueOf(18)) };
			errors.add(MessageFormat.format(iwrb.getLocalizedString(NOT_VALID_ACCOUNT_AGE_KEY, NOT_VALID_ACCOUNT_AGE_DEFAULT), arguments));
			hasErrors = true;
		}
		
		String email = iwc.getParameter(EMAIL_KEY).toString();
		if (email == null || email.length() == 0) {
			errors.add(iwrb.getLocalizedString("email_can_not_be_empty", "You must provide a valid e-mail address"));
			hasErrors = true;
		}
		
		String emailRepeat = iwc.getParameter(EMAIL_KEY_REPEAT).toString();
		String phoneHome = iwc.getParameter(PHONE_HOME_KEY).toString();
		String phoneWork = iwc.getParameter(PHONE_CELL_KEY).toString();
		CitizenAccountBusiness business = (CitizenAccountBusiness) IBOLookup.getServiceInstance(iwc, CitizenAccountBusiness.class);
		User user = business.getUserIcelandic(ssn);
		
		boolean userHasLogin = false;
		Collection userLoginError = new ArrayList();
		if (user == null && (ssn != null && SocialSecurityNumber.isValidIcelandicSocialSecurityNumber(ssn))) {
			errors.add(iwrb.getLocalizedString(UNKNOWN_CITIZEN_KEY, UNKNOWN_CITIZEN_DEFAULT));
			hasErrors = true;
		}
		else {
			//FIRST CHECK IF THE USER IS IN THE CORRECT COMMUNE!
			boolean inCorrectCommune = false;
//				TODO check if must be in a certain commune, create a setmethod for that, make sure the user is added to the root commune and the accepted group
			if( getCommuneUniqueIdsCSV()!=null ){
				String communesCSV = getCommuneUniqueIdsCSV();
				//for better uniqueness
				communesCSV = ","+communesCSV+",";
				
				Collection addresses = user.getAddresses();
				Iterator iter = addresses.iterator();
				while(iter.hasNext() && !inCorrectCommune) {
					Address address = (Address) iter.next();
					Commune commune = address.getCommune();
					
					if(commune!=null){
						String userCommune = commune.getCommuneCode();
						if(userCommune!=null){
							if( communesCSV.indexOf(","+userCommune+",") > -1 ){
								inCorrectCommune = true;
							}
						}
					}
				}
				
				if (!inCorrectCommune){
					errors.add(iwrb.getLocalizedString(ERROR_APPLYING_FOR_WRONG_COMMUNE, ERROR_APPLYING_FOR_WRONG_COMMUNE_DEFAULT));
					hasErrors = true;
				}
			}
			
			try {
				Collection logins = new ArrayList();
				logins.addAll(getLoginTableHome().findLoginsForUser(user));
				if (!logins.isEmpty()) { 
					userLoginError.add(iwrb.getLocalizedString(USER_ALLREADY_HAS_A_LOGIN_KEY, USER_ALLREADY_HAS_A_LOGIN_DEFAULT));
					hasErrors = true;
					userHasLogin = true;
				}	
			}
			catch (Exception e) {
				// no problem, no login found
			}
			
			if (email != null && email.length() > 0) {
				if (emailRepeat == null || !email.equals(emailRepeat)) {
					errors.add(iwrb.getLocalizedString(ERROR_EMAILS_DONT_MATCH, ERROR_EMAILS_DONT_MATCH_DEFAULT));
					hasErrors = true;
				}
			}
			
			try {
				if (!hasErrors) {
					if (null == business.insertApplication(iwc, user, ssn, email, phoneHome, phoneWork, true)) {
						errors.add(iwrb.getLocalizedString(ERROR_NO_INSERT_KEY, ERROR_NO_INSERT_KEY));
						hasErrors = true;
					}
				}
			}
			catch (UserHasLoginException e) {
				errors.add(iwrb.getLocalizedString(USER_ALLREADY_HAS_A_LOGIN_KEY, USER_ALLREADY_HAS_A_LOGIN_DEFAULT));
				hasErrors = true;
			}
		}
		
		if (hasErrors) {
			showErrors(iwc, userHasLogin ? userLoginError : errors);
			viewSimpleApplicationForm(iwc);
		}
		else {
			Layer header = new Layer(Layer.DIV);
			header.setStyleClass("header");
			add(header);
			
			Heading1 heading = new Heading1(iwrb.getLocalizedString("citizen_registration", "Citizen registration"));
			header.add(heading);
			
			Layer layer = new Layer(Layer.DIV);
			layer.setStyleClass("receipt");
			
			Layer image = new Layer(Layer.DIV);
			image.setStyleClass("receiptImage");
			layer.add(image);
			
			String serverName = iwc.getApplicationSettings().getProperty("server_name", "");

			heading = new Heading1(iwrb.getLocalizedString(TEXT_APPLICATION_SUBMITTED_KEY + (serverName.length() > 0 ? ("_" + serverName) : ""), TEXT_APPLICATION_SUBMITTED_DEFAULT));
			layer.add(heading);
			
			layer.add(new Text(iwrb.getLocalizedString(TEXT_APPLICATION_SUBMITTED_KEY + "_text" + (serverName.length() > 0 ? ("_" + serverName) : ""), TEXT_APPLICATION_SUBMITTED_DEFAULT + " info")));
			
			add(layer);
			
			if (iPage != null) {
				iwc.forwardToIBPage(getParentPage(), iPage, iRedirectDelay, false);
			}
		}
	}

	/**
	 * 
	 * @return a comma seperated values string with unique ids of communes in the IC_COMMUNE table
	 */
	public String getCommuneUniqueIdsCSV() {
		return communeUniqueIdsCSV;
	}
	
	/**
	 * If the parameter is set then the applications checks if the user has an address registered to one of the commune ids in this string
	 * @param communeUniqueIdsCSV a comma seperated values string with unique ids of communes in the IC_COMMUNE table
	 */
	public void setCommuneUniqueIdsCSV(String communeUniqueIdsCSV) {
		this.communeUniqueIdsCSV = communeUniqueIdsCSV;
	}
	
	private boolean isValidAge(IWContext iwc, String ssn) {
		int validAge = Integer.parseInt(iwc.getApplicationSettings().getProperty(ATTRIBUTE_VALID_ACCOUNT_AGE, String.valueOf(18)));
		boolean validAgeYear = iwc.getApplicationSettings().getBoolean(ATTRIBUTE_VALID_ACCOUNT_AGE_YEAR);
		
		if (ssn != null && SocialSecurityNumber.isValidIcelandicSocialSecurityNumber(ssn)) {
			IWTimestamp dateOfBirth = new IWTimestamp(SocialSecurityNumber.getDateFromSocialSecurityNumber(ssn));
			if (validAgeYear) {
				dateOfBirth.setDay(1);
				dateOfBirth.setMonth(1);
			}
			
			Age age = new Age(dateOfBirth.getDate());
			return age.getYears() >= validAge;
		}
		return true;
	}

	private int parseAction(final IWContext iwc) {
		int action = ACTION_VIEW_FORM;

		if (iwc.isParameterSet(SIMPLE_FORM_SUBMIT_KEY)) {
			action = ACTION_SUBMIT_SIMPLE_FORM;
		}

		return action;
	}

	protected WSCitizenAccountBusiness getBusiness(IWApplicationContext iwac) throws RemoteException {
		return (WSCitizenAccountBusiness) IBOLookup.getServiceInstance(iwac, WSCitizenAccountBusiness.class);
	}

	private LoginTableHome getLoginTableHome() {
		try {
			return (LoginTableHome) IDOLookup.getHome(LoginTable.class);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setPage(ICPage page) {
		iPage = page;
	}
	
	public void setRedirectDelay(int redirectDelay) {
		iRedirectDelay = redirectDelay;
	}
	
	public void setCommunePage(String name, String URL) {
		if (iCommuneMap == null) {
			iCommuneMap = new HashMap();
		}
		iCommuneMap.put(name, URL);
		iForwardToURL = true;
	}
}