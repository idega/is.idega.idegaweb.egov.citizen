/*
 * Created on 2004-maj-11
 *
 * To change the template for this generated file go to Window - Preferences - Java - Code Generation - Code and Comments
 */

/**
 * @author Malin
 *
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */

package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.idegaweb.egov.citizen.business.WSCitizenAccountBusiness;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.UserHasLoginException;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.LoginTableHome;
import com.idega.core.builder.data.ICPage;
import com.idega.core.localisation.presentation.LocalePresentationUtil;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.Commune;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.ExceptionWrapper;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.Age;
import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.text.SocialSecurityNumber;

/**
 * Last modified: $Date$ by $Author$
 *
 * @author <a href="mail:laddi@idega.is">Laddi</a>
 * @version $Revision$
 */
public class CitizenAccountApplication extends CitizenBlock {

	protected final static int ACTION_VIEW_FORM = 0;
	protected final static int ACTION_SUBMIT_SIMPLE_FORM = 1;

	protected static final String ATTRIBUTE_VALID_ACCOUNT_AGE = "citizen_account_minimum_age";
	protected static final String ATTRIBUTE_VALID_ACCOUNT_AGE_YEAR = "citizen_account_minimum_age_this_year";

	protected final static String EMAIL_DEFAULT = "Email";
	protected final static String EMAIL_KEY = "email";
	protected final static String SNAIL_MAIL_KEY = "snail_mail";
	protected final static String SNAIL_MAIL_DEFAULT = "Send password using ordinary mail";

	protected final static String APP_AGREEMENT_PARAM = "applicationAgreement";

	protected final static String APP_AGREEMENT_NOTAGREED_KEY = "accountApplication.agreementNotAgreed";
	protected final static String APP_AGREEMENT_NOTAGREED_DEFAULT = "Þú þarft að samþykkja skilmálana fyrst";
	protected final static String APP_AGREEMENT_TERMSOFUSE_KEY = "accountApplication.termsofuse";
	protected final static String APP_AGREEMENT_TERMSOFUSE_DEFAULT = "<a href=\"{0}\">Notkunarskilmálar</a>";

	protected final static String EMAIL_KEY_REPEAT = "email_repeat";
	protected final static String EMAIL_REPEAT_DEFAULT = "Email again";
	protected final static String PHONE_HOME_KEY = "home_phone";
	protected final static String PHONE_HOME_DEFAULT = "Phone";
	protected final static String PHONE_CELL_KEY = "mobile_phone";
	protected final static String PREFERRED_LANGUAGE = "preferred_language";
	protected final static String PREFERRED_ROLE = "preferred_role";
	protected final static String PHONE_CELL_DEFAULT = "Cell phone";
	protected final static String UNKNOWN_CITIZEN_KEY = "unknown_citizen";
	protected final static String UNKNOWN_CITIZEN_DEFAULT = "Something is wrong with your personal id. Please try again or contact the responsible";
	protected final static String NOT_VALID_ACCOUNT_AGE_KEY = "not_valid_citizen_account_age";
	protected final static String NOT_VALID_ACCOUNT_AGE_DEFAULT = "You have to be {0} to apply for a citizen account";
	protected final static String COMMUNE_DEFAULT = "Commune";
	protected final static String COMMUNE_KEY = "commmune";

	public final static String PARAMETER_PREFERRED_LOCALE = "pref_locale";
	public final static String PARAMETER_HIDE_PERSONALID_INPUT = "hidePersonalIdInput";
	public final static String PARAMETER_CREATE_LOGIN_AND_LETTER = "createLoginAndLetter";
	public final static String PARAMETER_REDIRECT_URI = "redirectUriOnSubmit";

	private String communeUniqueIdsCSV;

	private final static String SSN_DEFAULT = "Personal ID";
	public final static String SSN_KEY = "personal_id";
	protected final static String TEXT_APPLICATION_SUBMITTED_DEFAULT = "Application is submitted.";
	protected final static String TEXT_APPLICATION_SUBMITTED_KEY = "application_submitted";
	protected final static String TEXT_APPLICATION_BANK_SUBMITTED_KEY = "application_bank_submitted";

	protected final static String USER_ALLREADY_HAS_A_LOGIN_DEFAULT = "You already have an account";
	protected final static String USER_ALLREADY_HAS_A_LOGIN_KEY = "user_already_has_an_account";

	protected final static String SIMPLE_FORM_SUBMIT_KEY = "scaa_simpleSubmit";
	protected final static String SIMPLE_FORM_SUBMIT_DEFAULT = "Forward >>";

	protected final static String ERROR_NO_INSERT_KEY = "unable_to_store";
	protected final static String ERROR_EMAILS_DONT_MATCH = "emails_dont_match";
	protected final static String ERROR_EMAILS_DONT_MATCH_DEFAULT = "Emails don't match";
	protected static final String ERROR_APPLYING_FOR_WRONG_COMMUNE = "user_in_wrong_commune";
	protected static final String ERROR_APPLYING_FOR_WRONG_COMMUNE_DEFAULT = "You do not belong to the commune you are applying for according to our records and the application cannot be finished. Please contact your commune if you think this is an error.";

	private IWResourceBundle iwrb;
	protected ICPage iPage;
	protected int iRedirectDelay = 15;

	protected boolean iForwardToURL = false;
	private Map<String, String> iCommuneMap;
	private boolean hidePersonalIdInput = false;
	private boolean createLoginAndLetter = true;
	private String redirectUrlOnSubmit;
	private String agreementFileUrl;

	private boolean showPreferredLocaleChooser = false;
	private boolean showPreferredRoleChooser = false;
	private boolean showSendSnailMailChooser = false;

	@Override
	public void present(IWContext iwc) {

		this.iwrb = getResourceBundle(iwc);
		parseParameters(iwc);
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
		} catch (Exception e) {
			super.add(new ExceptionWrapper(e, this));
		}
	}

	/**
	 * <p>
	 * Parse the request parameters and initialize this component with teir
	 * values if they are set
	 * </p>
	 *
	 * @param iwc
	 */
	protected void parseParameters(IWContext iwc) {

		if (iwc.isParameterSet(PARAMETER_HIDE_PERSONALID_INPUT)) {
			String value = iwc.getParameter(PARAMETER_HIDE_PERSONALID_INPUT);
			if (Boolean.valueOf(value).booleanValue() == true) {
				setHidePersonalIdInput(true);
			}
		}
		if (iwc.isParameterSet(PARAMETER_CREATE_LOGIN_AND_LETTER)) {
			String value = iwc.getParameter(PARAMETER_CREATE_LOGIN_AND_LETTER);
			if (Boolean.valueOf(value).booleanValue() == false) {
				setCreateLoginAndLetter(false);
			}
		}
		if (iwc.isParameterSet(PARAMETER_REDIRECT_URI)) {
			String value = iwc.getParameter(PARAMETER_REDIRECT_URI);
			if (value != null) {
				setRedirectUrlOnSubmit(value);
			}
		}
	}

	/**
	 * <p>
	 * Maintain the needed parameters between submits.
	 * </p>
	 *
	 * @param iwc
	 * @param form
	 */
	protected void maintainHiddenParameters(IWContext iwc, Form form) {
		if (iwc.isParameterSet(PARAMETER_HIDE_PERSONALID_INPUT)) {
			String value = iwc.getParameter(PARAMETER_HIDE_PERSONALID_INPUT);
			if (value != null) {
				HiddenInput input = new HiddenInput(PARAMETER_HIDE_PERSONALID_INPUT);
				input.keepStatusOnAction();
				form.add(input);
			}
		}
		if (iwc.isParameterSet(PARAMETER_CREATE_LOGIN_AND_LETTER)) {
			String value = iwc.getParameter(PARAMETER_CREATE_LOGIN_AND_LETTER);
			if (value != null) {
				HiddenInput input = new HiddenInput(PARAMETER_CREATE_LOGIN_AND_LETTER);
				input.keepStatusOnAction();
				form.add(input);
			}
		}
		if (iwc.isParameterSet(PARAMETER_REDIRECT_URI)) {
			String value = iwc.getParameter(PARAMETER_REDIRECT_URI);
			if (value != null) {
				HiddenInput input = new HiddenInput(PARAMETER_REDIRECT_URI);
				input.keepStatusOnAction();
				form.add(input);
			}
		}
	}

	protected void viewSimpleApplicationForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.addParameter(SIMPLE_FORM_SUBMIT_KEY, Boolean.TRUE.toString());
		form.setID("accountApplicationForm");
		form.setStyleClass("citizenForm");

		Layer header = new Layer(Layer.DIV);
		header.setStyleClass("header");
		form.add(header);

		Heading1 heading = new Heading1(this.iwrb.getLocalizedString("citizen_registration", "Citizen registration"));
		header.add(heading);

		Layer contents = new Layer(Layer.DIV);
		contents.setStyleClass("formContents");
		form.add(contents);

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		contents.add(section);

		boolean sendMessageToBank = getBusiness(iwc).sendMessageToBank();

		Layer helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(this.iwrb.getLocalizedString(sendMessageToBank ? "citizen_registraction_bank_help" : "citizen_registraction_help",
				"Please fill in your personal ID as well as your e-mail.  Your e-mail is required so that you can be contacted directly about changes to you ongoing cases.  If you don't have an e-mail account please contact the commune offices.")));
		section.add(helpLayer);

		maintainHiddenParameters(iwc, form);

		TextInput email = new TextInput(EMAIL_KEY);
		email.keepStatusOnAction(true);

		CheckBox snailMail = new CheckBox(SNAIL_MAIL_KEY);
		snailMail.setStyleClass("checkbox");
		snailMail.keepStatusOnAction(true);

		TextInput emailRepeat = new TextInput(EMAIL_KEY_REPEAT);
		emailRepeat.keepStatusOnAction(true);

		TextInput mobile = new TextInput(PHONE_CELL_KEY);
		mobile.keepStatusOnAction(true);

		TextInput homePhone = new TextInput(PHONE_HOME_KEY);
		homePhone.keepStatusOnAction(true);

		CheckBox applicationAgreement = new CheckBox(APP_AGREEMENT_PARAM);
		applicationAgreement.setStyleClass("checkbox");
		applicationAgreement.keepStatusOnAction(true);

		if (this.iCommuneMap != null) {
			DropdownMenu communes = new DropdownMenu(COMMUNE_KEY);
			for (String commune: iCommuneMap.keySet()) {
				communes.addMenuElement(this.iCommuneMap.get(commune), commune);
			}
			communes.addMenuElementFirst("", this.iwrb.getLocalizedString("select_commune", "Select commune"));

			Layer formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("required");
			Label label = new Label(communes);
			label.add(new Span(new Text(this.iwrb.getLocalizedString(COMMUNE_KEY, COMMUNE_DEFAULT))));
			formItem.add(label);
			formItem.add(communes);
			section.add(formItem);
		}

		Layer formItem;
		Label label;
		if (isHidePersonalIdInput()) {
			HiddenInput personalID = new HiddenInput(SSN_KEY);
			personalID.keepStatusOnAction(true);
			section.add(personalID);
		} else {
			TextInput personalID = new TextInput(SSN_KEY);
			personalID.setStyleClass("personalID");
			personalID.setMaxlength(10);
			personalID.setLength(10);
			personalID.keepStatusOnAction(true);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("required");
			formItem.setID("personalID");
			label = new Label(personalID);
			label.add(new Span(new Text(this.iwrb.getLocalizedString(SSN_KEY, SSN_DEFAULT))));
			formItem.add(label);
			formItem.add(personalID);
			section.add(formItem);
		}

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("required");
		formItem.setID("email");
		label = new Label(email);
		label.add(new Span(new Text(this.iwrb.getLocalizedString(EMAIL_KEY, EMAIL_DEFAULT))));
		formItem.add(label);
		formItem.add(email);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("required");
		formItem.setID("emailRepeat");
		label = new Label(emailRepeat);
		label.add(new Span(new Text(this.iwrb.getLocalizedString(EMAIL_KEY_REPEAT, EMAIL_REPEAT_DEFAULT))));
		formItem.add(label);
		formItem.add(emailRepeat);
		section.add(formItem);

		if (isSetToShowSendSnailMailChooser()) {
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("radioButtonItem");
			formItem.setID("snail_mail");
			snailMail.setStyleClass("checkbox");
			label = new Label(snailMail);
			label.add(new Span(new Text(this.iwrb.getLocalizedString(SNAIL_MAIL_KEY, SNAIL_MAIL_DEFAULT))));
			formItem.add(snailMail);
			formItem.add(label);
			section.add(formItem);
		}

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setID("mobilePhone");
		label = new Label(this.iwrb.getLocalizedString(PHONE_CELL_KEY, PHONE_CELL_DEFAULT), mobile);
		formItem.add(label);
		formItem.add(mobile);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setID("homePhone");
		label = new Label(this.iwrb.getLocalizedString(PHONE_HOME_KEY, PHONE_HOME_DEFAULT), homePhone);
		formItem.add(label);
		formItem.add(homePhone);
		section.add(formItem);

		DropdownMenu localesDrop = LocalePresentationUtil.getAvailableLocalesDropdown(iwc.getIWMainApplication(), PARAMETER_PREFERRED_LOCALE);
		if (localesDrop.getChildCount() > 1 && isSetToShowPreferredLocaleChooser()) {
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setID("preferredLang");
			localesDrop.setSelectedElement(iwc.getCurrentLocale().toString());
			label = new Label(this.iwrb.getLocalizedString(PREFERRED_LANGUAGE, "Preferred language"), localesDrop);
			formItem.add(label);
			formItem.add(localesDrop);
			section.add(formItem);
		}

		renderComponentsBeforeTermOfUseAggreement(section);


		// TODO: localize the url to agreement file
		Object[] linkToAgreement = new Object[] { getAgreementFileUrl() };
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("radioButtonItem");
		formItem.setStyleClass("required");
		formItem.setID(APP_AGREEMENT_PARAM);
		label = new Label(new Span(new Text(this.iwrb.getLocalizedAndFormattedString(APP_AGREEMENT_TERMSOFUSE_KEY, APP_AGREEMENT_TERMSOFUSE_DEFAULT, linkToAgreement))), applicationAgreement);
		formItem.add(applicationAgreement);
		formItem.add(label);
		section.add(formItem);

		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");
		section.add(clearLayer);

		Layer buttonLayer = new Layer(Layer.DIV);
		buttonLayer.setStyleClass("buttonLayer");
		contents.add(buttonLayer);

		Layer span = new Layer(Layer.SPAN);
		span.add(new Text(this.iwrb.getLocalizedString(SIMPLE_FORM_SUBMIT_KEY + "_button", SIMPLE_FORM_SUBMIT_DEFAULT)));
		Link send = new Link(span);
		send.setToFormSubmit(form);
		buttonLayer.add(send);

		add(form);
	}

	protected void submitSimpleForm(IWContext iwc) throws RemoteException {
		boolean hasErrors = false;
		Collection<String> errors = new ArrayList<String>();

		if (this.iForwardToURL) {
			if (iwc.isParameterSet(COMMUNE_KEY)) {
				String URL = iwc.getParameter(COMMUNE_KEY);
				StringBuffer query = new StringBuffer();
				Enumeration<String> enumeration = iwc.getParameterNames();
				if (enumeration != null) {
					query.append("?");

					while (enumeration.hasMoreElements()) {
						String element = enumeration.nextElement();
						query.append(element).append("=").append(iwc.getParameter(element));
						if (enumeration.hasMoreElements()) {
							query.append("&");
						}
					}
				}
				iwc.sendRedirect(URL + query.toString());
				return;
			} else {
				errors.add(this.iwrb.getLocalizedString("must_select_commune", "You have to select a commune."));
				hasErrors = true;
			}
		}

		String ssn = iwc.getParameter(SSN_KEY);

		if (ssn == null || ssn.length() == 0) {
			errors.add(this.iwrb.getLocalizedString("must_provide_personal_id", "You have to enter a personal ID."));
			hasErrors = true;
		} else if (!SocialSecurityNumber.isValidIcelandicSocialSecurityNumber(ssn)) {
			errors.add(this.iwrb.getLocalizedString("not_a_valid_personal_id", "The personal ID you've entered is not valid."));
			hasErrors = true;
		}
		if (!isValidAge(iwc, ssn)) {
			Object[] arguments = { iwc.getApplicationSettings().getProperty(ATTRIBUTE_VALID_ACCOUNT_AGE, String.valueOf(18)) };
			errors.add(MessageFormat.format(this.iwrb.getLocalizedString(NOT_VALID_ACCOUNT_AGE_KEY, NOT_VALID_ACCOUNT_AGE_DEFAULT), arguments));
			hasErrors = true;
		}

		String email = iwc.getParameter(EMAIL_KEY);
		if (email == null || email.length() == 0) {
			errors.add(this.iwrb.getLocalizedString("email_can_not_be_empty", "You must provide a valid e-mail address"));
			hasErrors = true;
		}

		boolean agreementAccepted = iwc.getParameter(APP_AGREEMENT_PARAM) != null && iwc.getParameter(APP_AGREEMENT_PARAM).length() > 0;

		if (!agreementAccepted) {
			errors.add(this.iwrb.getLocalizedString(APP_AGREEMENT_NOTAGREED_KEY, APP_AGREEMENT_NOTAGREED_DEFAULT));
			hasErrors = true;
		}

		boolean sendSnailMail = iwc.getParameter(SNAIL_MAIL_KEY) != null && iwc.getParameter(SNAIL_MAIL_KEY).length() > 0;

		String emailRepeat = iwc.getParameter(EMAIL_KEY_REPEAT);
		String phoneHome = iwc.getParameter(PHONE_HOME_KEY);
		String phoneWork = iwc.getParameter(PHONE_CELL_KEY);
		String preferredLocale = iwc.getParameter(PARAMETER_PREFERRED_LOCALE);

		WSCitizenAccountBusiness business = getBusiness(iwc);
		User user = business.getUserIcelandic(ssn);

		boolean userHasLogin = false;
		Collection<String> userLoginError = new ArrayList<String>();
		if (user == null) {
			getLogger().warning("Unknown citizen with personal ID: " + ssn);
			errors.add(this.iwrb.getLocalizedString(UNKNOWN_CITIZEN_KEY, UNKNOWN_CITIZEN_DEFAULT));
			hasErrors = true;
		} else {
			// FIRST CHECK IF THE USER IS IN THE CORRECT COMMUNE!
			boolean inCorrectCommune = false;
			// TODO check if must be in a certain commune, create a setmethod
			// for that, make sure the user is added to the root commune and the
			// accepted
			// group
			if (getCommuneUniqueIdsCSV() != null) {
				String communesCSV = getCommuneUniqueIdsCSV();
				// for better uniqueness
				communesCSV = "," + communesCSV + ",";

				Collection<Address> addresses = user.getAddresses();
				String userCommunes = CoreConstants.EMPTY;
				for (Iterator<Address> iter = addresses.iterator(); (iter.hasNext() && !inCorrectCommune);) {
					Address address = iter.next();
					Commune commune = address.getCommune();

					if (commune != null) {
						String userCommune = commune.getCommuneCode();
						if (userCommune != null) {
							userCommunes = userCommunes.concat(userCommune);
							if (communesCSV.indexOf("," + userCommune + ",") > -1) {
								inCorrectCommune = true;
							}
						}
					}
				}

				if (!inCorrectCommune) {
					getLogger().warning("Citizen with personal ID=" + ssn + " can not be accepted: he is from wrong commune(s): " + userCommunes + ". Allowed communes: " + communesCSV);
					errors.add(this.iwrb.getLocalizedString(ERROR_APPLYING_FOR_WRONG_COMMUNE, ERROR_APPLYING_FOR_WRONG_COMMUNE_DEFAULT));
					hasErrors = true;
				}
			}

			try {
				Collection<LoginTable> logins = new ArrayList<LoginTable>();
				logins.addAll(getLoginTableHome().findLoginsForUser(user));
				if (!ListUtil.isEmpty(logins)) {
					getLogger().warning("User with personal ID=" + ssn + " already has login");
					userLoginError.add(this.iwrb.getLocalizedString(USER_ALLREADY_HAS_A_LOGIN_KEY, USER_ALLREADY_HAS_A_LOGIN_DEFAULT));
					hasErrors = true;
					userHasLogin = true;
				}
			} catch (Exception e) {
				// no problem, no login found
			}

			if (email != null && email.length() > 0) {
				if (emailRepeat == null || !email.equals(emailRepeat)) {
					getLogger().warning("Emails do not match for user: " + ssn);
					errors.add(this.iwrb.getLocalizedString(ERROR_EMAILS_DONT_MATCH, ERROR_EMAILS_DONT_MATCH_DEFAULT));
					hasErrors = true;
				}
			}

			try {
				if (!hasErrors) {
					boolean sendSnailEmail = isSetToShowSendSnailMailChooser() ? sendSnailMail : true;
					if (null == business.insertApplication(iwc, user, ssn, email, phoneHome, phoneWork, true, isCreateLoginAndLetter(), sendSnailEmail)) {
						getLogger().warning("Error creating user application for applicant with personal ID: " + ssn + ", send mail: " + isCreateLoginAndLetter() + ", send snail mail: " + sendSnailEmail);
						errors.add(this.iwrb.getLocalizedString(ERROR_NO_INSERT_KEY, ERROR_NO_INSERT_KEY));
						hasErrors = true;
					} else {
						getLogger().info("Created user application for applicant with personal ID: " + ssn + ", send mail: " + isCreateLoginAndLetter() + ", send snail mail: " + sendSnailEmail);
					}
				}
			} catch (UserHasLoginException e) {
				errors.add(this.iwrb.getLocalizedString(USER_ALLREADY_HAS_A_LOGIN_KEY, USER_ALLREADY_HAS_A_LOGIN_DEFAULT));
				getLogger().log(Level.WARNING, "Applicant with personal ID: " + ssn + " already has login", e);
				hasErrors = true;
			}
		}

		if (!hasErrors && preferredLocale != null) {
			getUserBusiness(iwc).setUsersPreferredLocale(user, preferredLocale, true);
		}

		if (hasErrors) {
			showErrors(iwc, userHasLogin ? userLoginError : errors);
			viewSimpleApplicationForm(iwc);
		} else {
			Form form = new Form();
			form.setID("accountApplicationForm");
			form.setStyleClass("citizenForm");

			Layer header = new Layer(Layer.DIV);
			header.setStyleClass("header");
			form.add(header);

			Heading1 heading = new Heading1(this.iwrb.getLocalizedString("citizen_registration", "Citizen registration"));
			header.add(heading);

			Layer layer = new Layer(Layer.DIV);
			layer.setStyleClass("receipt");

			Layer image = new Layer(Layer.DIV);
			image.setStyleClass("receiptImage");
			layer.add(image);

			String serverName = iwc.getApplicationSettings().getProperty("server_name", "");
			boolean sendMessageToBank = getBusiness(iwc).sendMessageToBank();

			heading = new Heading1(this.iwrb.getLocalizedString((sendMessageToBank ? TEXT_APPLICATION_BANK_SUBMITTED_KEY : TEXT_APPLICATION_SUBMITTED_KEY) + (serverName.length() > 0 ? ("_" + serverName) : ""), TEXT_APPLICATION_SUBMITTED_DEFAULT));
			layer.add(heading);

			layer.add(new Text(this.iwrb.getLocalizedString((sendMessageToBank ? TEXT_APPLICATION_BANK_SUBMITTED_KEY + "_text" : TEXT_APPLICATION_SUBMITTED_KEY + "_text") + (serverName.length() > 0 ? ("_" + serverName) : ""), TEXT_APPLICATION_SUBMITTED_DEFAULT + " info")));

			form.add(layer);
			add(form);

			if (this.iPage != null) {
				iwc.forwardToIBPage(getParentPage(), this.iPage, this.iRedirectDelay, false);
			} else if (getRedirectUrlOnSubmit() != null) {

				try {
					iwc.getResponse().sendRedirect(getRedirectUrlOnSubmit());
				} catch (IOException io) {
					// the response is most likely been committed, i.e. written
					// output to already
					iwc.forwardToURL(getParentPage(), getRedirectUrlOnSubmit(), this.iRedirectDelay, false);
				}

			}
		}
	}

	/**
	 *
	 * @return a comma seperated values string with unique ids of communes in
	 *         the IC_COMMUNE table
	 */
	public String getCommuneUniqueIdsCSV() {
		return this.communeUniqueIdsCSV;
	}

	/**
	 * If the parameter is set then the applications checks if the user has an
	 * address registered to one of the commune ids in this string
	 *
	 * @param communeUniqueIdsCSV
	 *            a comma seperated values string with unique ids of communes in
	 *            the IC_COMMUNE table
	 */
	public void setCommuneUniqueIdsCSV(String communeUniqueIdsCSV) {
		this.communeUniqueIdsCSV = communeUniqueIdsCSV;
	}

	protected boolean isValidAge(IWContext iwc, String ssn) {
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

	protected int parseAction(final IWContext iwc) {
		int action = ACTION_VIEW_FORM;

		if (iwc.isParameterSet(SIMPLE_FORM_SUBMIT_KEY)) {
			action = ACTION_SUBMIT_SIMPLE_FORM;
		}

		return action;
	}

	protected WSCitizenAccountBusiness getBusiness(IWApplicationContext iwac) throws RemoteException {
		return IBOLookup.getServiceInstance(iwac, WSCitizenAccountBusiness.class);
	}

	protected UserBusiness getUserBusiness(IWApplicationContext iwac) throws RemoteException {
		return IBOLookup.getServiceInstance(iwac, UserBusiness.class);
	}

	protected LoginTableHome getLoginTableHome() {
		try {
			return (LoginTableHome) IDOLookup.getHome(LoginTable.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setPage(ICPage page) {
		this.iPage = page;
	}

	public void setRedirectDelay(int redirectDelay) {
		this.iRedirectDelay = redirectDelay;
	}

	public void setCommunePage(String name, String URL) {
		if (this.iCommuneMap == null) {
			this.iCommuneMap = new HashMap<String, String>();
		}
		this.iCommuneMap.put(name, URL);
		this.iForwardToURL = true;
	}

	/**
	 * @return Returns the showPreferredLocaleChooser.
	 */
	public boolean isSetToShowPreferredLocaleChooser() {
		return showPreferredLocaleChooser;
	}

	/**
	 * @param showPreferredLocaleChooser
	 *            The showPreferredLocaleChooser to set.
	 */
	public void setToShowPreferredLocaleChooser(boolean showPreferredLocaleChooser) {
		this.showPreferredLocaleChooser = showPreferredLocaleChooser;
	}

	public void setToShowSendSnailMailChooser(boolean showSendSnailMailChooser) {
		this.showSendSnailMailChooser = showSendSnailMailChooser;
	}

	public boolean isSetToShowSendSnailMailChooser() {
		return showSendSnailMailChooser;
	}

	public boolean isCreateLoginAndLetter() {
		return createLoginAndLetter;
	}

	public void setCreateLoginAndLetter(boolean createLoginAndLetter) {
		this.createLoginAndLetter = createLoginAndLetter;
	}

	public boolean isHidePersonalIdInput() {
		return hidePersonalIdInput;
	}

	public void setHidePersonalIdInput(boolean hidePersonalIdInput) {
		this.hidePersonalIdInput = hidePersonalIdInput;
	}

	public String getRedirectUrlOnSubmit() {
		return redirectUrlOnSubmit;
	}

	public void setRedirectUrlOnSubmit(String redirectUrlOnSubmit) {
		this.redirectUrlOnSubmit = redirectUrlOnSubmit;
	}

	public String getAgreementFileUrl() {
		return agreementFileUrl == null || agreementFileUrl.equals("") ? "#" : agreementFileUrl;
	}

	public void setAgreementFileUrl(String agreementFileUrl) {
		this.agreementFileUrl = agreementFileUrl;
	}

	public boolean isSetToShowPreferredRoleChooser() {
		return showPreferredRoleChooser;
	}

	/**
	 * method to override to add your components to the form before term of use agreement
	 * @param form
	 * @throws RemoteException
	 */
	protected void renderComponentsBeforeTermOfUseAggreement(Layer layer) throws RemoteException {

	}
}