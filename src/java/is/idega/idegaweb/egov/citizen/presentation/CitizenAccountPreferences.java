package is.idega.idegaweb.egov.citizen.presentation;

import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.account.citizen.business.CitizenAccountSession;
import se.idega.idegaweb.commune.message.business.MessageSession;

import com.idega.business.IBOLookup;
import com.idega.core.builder.data.ICPage;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.file.data.ICFile;
import com.idega.core.location.business.AddressBusiness;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.AddressHome;
import com.idega.core.location.data.AddressType;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.PostalCode;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.io.UploadFile;
import com.idega.presentation.ExceptionWrapper;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CountryDropdownMenu;
import com.idega.presentation.ui.FileInput;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.EmailValidator;
import com.idega.util.FileUtil;

/*
 * import com.idega.presentation.ExceptionWrapper; import
 * com.idega.presentation.IWContext; import com.idega.presentation.*; import
 * com.idega.presentation.ui.*; import com.idega.core.data.Address; import
 * com.idega.core.data.Email; import com.idega.user.data.*; import
 * com.idega.business.IBOLookup; import com.idega.user.business.UserBusiness;
 * 
 * import se.idega.idegaweb.commune.presentation.CommuneBlock;
 */
/**
 * Title: Description: Copyright: Copyright (c) 2002 Company:
 * 
 * @author Anders Lindman
 * @version 1.0
 */
public class CitizenAccountPreferences extends CitizenBlock {

	private final static int ACTION_VIEW_FORM = 1;
	private final static int ACTION_FORM_SUBMIT = 2;

	private final static String PARAMETER_FORM_SUBMIT = "cap_sbmt";
	private final static String PARAMETER_EMAIL = "cap_email";
	private final static String PARAMETER_PHONE_HOME = "cap_phn_h";
	private final static String PARAMETER_PHONE_WORK = "cap_phn_w";
	private final static String PARAMETER_PHONE_MOBILE = "cap_phn_m";
	private final static String PARAMETER_CO_ADDRESS_SELECT = "cap_co";
	private final static String PARAMETER_CO_STREET_ADDRESS = "cap_co_sa";
	private final static String PARAMETER_CO_POSTAL_CODE = "cap_co_pc";
	private final static String PARAMETER_CO_CITY = "cap_co_ct";
	private final static String PARAMETER_CO_COUNTRY = "cap_co_country";
	private final static String PARAMETER_MESSAGES_VIA_EMAIL = "cap_m_v_e";
	private final static String PARAMETER_REMOVE_IMAGE = "cap_remove_image";
	
	private final static String KEY_PREFIX = "citizen.";
	private final static String KEY_EMAIL = KEY_PREFIX + "email";
	private final static String KEY_UPDATE = KEY_PREFIX + "update";
	private final static String KEY_PHONE_HOME = KEY_PREFIX + "phone_home";
	private final static String KEY_PHONE_MOBILE = KEY_PREFIX + "phone_mobile";
	private final static String KEY_PHONE_WORK = KEY_PREFIX + "phone_work";
	private final static String KEY_CO_ADDRESS_SELECT = KEY_PREFIX + "co_address_select";
	private final static String KEY_CO_STREET_ADDRESS = KEY_PREFIX + "co_street_address";
	private final static String KEY_CO_POSTAL_CODE = KEY_PREFIX + "co_postal_code";
	private final static String KEY_CO_CITY = KEY_PREFIX + "co_city";
	private final static String KEY_CO_COUNTRY = KEY_PREFIX + "co_country";
	private final static String KEY_MESSAGES_VIA_EMAIL = KEY_PREFIX + "messages_via_email";
	private final static String KEY_EMAIL_INVALID = KEY_PREFIX + "email_invalid";
	private final static String KEY_CO_STREET_ADDRESS_MISSING = KEY_PREFIX + "co_street_address_missing";
	private final static String KEY_CO_POSTAL_CODE_MISSING = KEY_PREFIX + "co_postal_code_missing";
	private final static String KEY_CO_CITY_MISSING = KEY_PREFIX + "co_city_missing";
	private final static String KEY_PREFERENCES_SAVED = KEY_PREFIX + "preferenced_saved";
	private final static String KEY_NO_EMAIL_FOR_LETTERS = KEY_PREFIX + "no_email_to_send_letter_to";
	
	private final static String DEFAULT_EMAIL = "E-mail";
	private final static String DEFAULT_UPDATE = "Update";
	private final static String DEFAULT_PHONE_HOME = "Phone (home)";
	private final static String DEFAULT_PHONE_MOBILE = "Phone (mobile)";
	private final static String DEFAULT_PHONE_WORK = "Phone (work)";
	private final static String DEFAULT_CO_ADDRESS_SELECT = "Use c/o address";
	private final static String DEFAULT_CO_STREET_ADDRESS = "Street address c/o";
	private final static String DEFAULT_CO_POSTAL_CODE = "Postal code c/o";
	private final static String DEFAULT_CO_CITY = "City c/o";
	private final static String DEFAULT_CO_COUNTRY = "Country c/o";
	private final static String DEFAULT_MESSAGES_VIA_EMAIL = "I want to get my messages via e-mail";
	private final static String DEFAULT_EMAIL_INVALID = "Email address invalid.";
	private final static String DEFAULT_CO_STREET_ADDRESS_MISSING = "Street address c/o must be entered.";
	private final static String DEFAULT_CO_POSTAL_CODE_MISSING = "Postal code c/o must be entered.";
	private final static String DEFAULT_CO_CITY_MISSING = "City c/o must be entered.";
	private final static String DEFAULT_PREFERENCES_SAVED = "Your preferences has been saved.";
	private final static String DEFAULT_NO_EMAIL_FOR_LETTERS = "No email entered to send letters to.";
	public static final String CITIZEN_ACCOUNT_PREFERENCES_PROPERTIES = "citizen_account_preferences";
	public static final String USER_PROPERTY_USE_CO_ADDRESS = "cap_use_co_address";

	private User user = null;

	private IWResourceBundle iwrb;

	public CitizenAccountPreferences() {
	}

	public void present(IWContext iwc) {
		if (!iwc.isLoggedOn()) {
			return;
		}
		iwrb = getResourceBundle(iwc);
		this.user = iwc.getCurrentUser();
		try {
			int action = parseAction(iwc);
			switch (action) {
				case ACTION_VIEW_FORM:
					viewForm(iwc);
					break;
				case ACTION_FORM_SUBMIT:
					updatePreferences(iwc);
					break;
			}
		}
		catch (Exception e) {
			super.add(new ExceptionWrapper(e, this));
		}
	}

	private int parseAction(final IWContext iwc) {
		int action = ACTION_VIEW_FORM;
		if (iwc.isParameterSet(PARAMETER_FORM_SUBMIT)) {
			action = ACTION_FORM_SUBMIT;
		}
		return action;
	}

	private void viewForm(IWContext iwc) throws java.rmi.RemoteException {
		Form form = new Form();
		form.setMultiPart();
		form.addParameter(PARAMETER_FORM_SUBMIT, Boolean.TRUE.toString());
		form.setID("accountApplicationForm");
		form.setStyleClass("citizenForm");
		
		Layer header = new Layer(Layer.DIV);
		header.setStyleClass("header");
		form.add(header);
		
		Heading1 heading = new Heading1(iwrb.getLocalizedString("citizen_preferences", "Citizen preferences"));
		header.add(heading);
		
		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);
		
		UserBusiness ub = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);

		Image image = null;
		if (user.getSystemImageID() > 0) {
			try {
				image = new Image(user.getSystemImageID());
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		Email mail = ub.getUserMail(user);
		Phone homePhone = null;
		try {
			homePhone = ub.getUsersHomePhone(user);
		}
		catch (NoPhoneFoundException e) {
			e.printStackTrace();
		}
		Phone mobilePhone = null;
		try {
			mobilePhone = ub.getUsersMobilePhone(user);
		}
		catch (NoPhoneFoundException e) {
			e.printStackTrace();
		}
		Phone workPhone = null;
		try {
			workPhone = ub.getUsersWorkPhone(user);
		}
		catch (NoPhoneFoundException e) {
			e.printStackTrace();
		}
		Address coAddress = getCOAddress(iwc);
		PostalCode postal = null;
		if (coAddress != null) {
			postal = coAddress.getPostalCode();
		}

		FileInput file = new FileInput();

		TextInput tiEmail = new TextInput(PARAMETER_EMAIL);
		if (mail != null && mail.getEmailAddress() != null) {
			tiEmail.setContent(mail.getEmailAddress());
		}

		TextInput tiPhoneHome = new TextInput(PARAMETER_PHONE_HOME);
		if (homePhone != null && homePhone.getNumber() != null) {
			tiPhoneHome.setContent(homePhone.getNumber());
		}
		
		TextInput tiPhoneMobile = new TextInput(PARAMETER_PHONE_MOBILE);
		if (mobilePhone != null && mobilePhone.getNumber() != null) {
			tiPhoneMobile.setContent(mobilePhone.getNumber());
		}
		
		TextInput tiPhoneWork = new TextInput(PARAMETER_PHONE_WORK);
		if (workPhone != null && workPhone.getNumber() != null) {
			tiPhoneWork.setContent(workPhone.getNumber());
		}
		
		TextInput tiCOStreetAddress = new TextInput(PARAMETER_CO_STREET_ADDRESS);
		if (coAddress != null && coAddress.getStreetAddress() != null) {
			tiCOStreetAddress.setContent(coAddress.getStreetAddress());
		}
		
		TextInput tiCOPostalCode = new TextInput(PARAMETER_CO_POSTAL_CODE);
		if (postal != null && postal.getPostalCode() != null) {
			tiCOPostalCode.setValue(postal.getPostalCode());
		}
		
		TextInput tiCOCity = new TextInput(PARAMETER_CO_CITY);
		if (coAddress != null && coAddress.getCity() != null) {
			tiCOCity.setValue(coAddress.getCity());
		}
		
		CountryDropdownMenu tiCOCountry = new CountryDropdownMenu(PARAMETER_CO_COUNTRY);
		if (postal != null && postal.getCountryID() > -1) {
			tiCOCountry.setSelectedCountry(postal.getCountry());
		}
		
		CitizenAccountSession cas = getCitizenAccountSession(iwc);
		CheckBox useCOAddress = new CheckBox(PARAMETER_CO_ADDRESS_SELECT, "true");
		useCOAddress.setStyleClass("checkbox");
		useCOAddress.setChecked(cas.getIfUserUsesCOAddress());
		useCOAddress.keepStatusOnAction(true);

		MessageSession messageSession = getMessageSession(iwc);
		CheckBox messagesViaEmail = new CheckBox(PARAMETER_MESSAGES_VIA_EMAIL, "true");
		messagesViaEmail.setStyleClass("checkbox");
		messagesViaEmail.keepStatusOnAction(true);
		messagesViaEmail.setChecked(messageSession.getIfUserPreferesMessageByEmail());

		CheckBox removeImage = new CheckBox(PARAMETER_REMOVE_IMAGE, "true");
		removeImage.setStyleClass("checkbox");
		removeImage.keepStatusOnAction(true);
		
		Layer formItem;
		Label label;
		
		if (image != null) {
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label();
			label.add(new Text(iwrb.getLocalizedString("image", "Image")));
			formItem.add(label);
			formItem.add(image);
			section.add(formItem);
			
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("indentedCheckbox");
			formItem.setID("removeImage");
			label = new Label(iwrb.getLocalizedString("remove_image", "Remove image"), removeImage);
			formItem.add(removeImage);
			formItem.add(label);
			section.add(formItem);
		}
		
		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");
		section.add(clearLayer);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setID("imageUpload");
		label = new Label(iwrb.getLocalizedString("image_upload", "Image upload"), file);
		formItem.add(label);
		formItem.add(file);
		section.add(formItem);
		
		section.add(clearLayer);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(iwrb.getLocalizedString(KEY_EMAIL, DEFAULT_EMAIL), tiEmail);
		formItem.add(label);
		formItem.add(tiEmail);
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("indentedCheckbox");
		formItem.setID("messagesViaEmail");
		label = new Label(iwrb.getLocalizedString(KEY_MESSAGES_VIA_EMAIL, DEFAULT_MESSAGES_VIA_EMAIL), messagesViaEmail);
		formItem.add(messagesViaEmail);
		formItem.add(label);
		section.add(formItem);
		
		section.add(clearLayer);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(iwrb.getLocalizedString(KEY_PHONE_HOME, DEFAULT_PHONE_HOME), tiPhoneHome);
		formItem.add(label);
		formItem.add(tiPhoneHome);
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(iwrb.getLocalizedString(KEY_PHONE_WORK, DEFAULT_PHONE_WORK), tiPhoneWork);
		formItem.add(label);
		formItem.add(tiPhoneWork);
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setID("mobilePhone");
		label = new Label(iwrb.getLocalizedString(KEY_PHONE_MOBILE, DEFAULT_PHONE_MOBILE), tiPhoneMobile);
		formItem.add(label);
		formItem.add(tiPhoneMobile);
		section.add(formItem);
		
		section.add(clearLayer);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(iwrb.getLocalizedString(KEY_CO_STREET_ADDRESS, DEFAULT_CO_STREET_ADDRESS), tiCOStreetAddress);
		formItem.add(label);
		formItem.add(tiCOStreetAddress);
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(iwrb.getLocalizedString(KEY_CO_POSTAL_CODE, DEFAULT_CO_POSTAL_CODE), tiCOPostalCode);
		formItem.add(label);
		formItem.add(tiCOPostalCode);
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(iwrb.getLocalizedString(KEY_CO_CITY, DEFAULT_CO_CITY), tiCOCity);
		formItem.add(label);
		formItem.add(tiCOCity);
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(iwrb.getLocalizedString(KEY_CO_COUNTRY, DEFAULT_CO_COUNTRY), tiCOCountry);
		formItem.add(label);
		formItem.add(tiCOCountry);
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("indentedCheckbox");
		label = new Label(iwrb.getLocalizedString(KEY_CO_ADDRESS_SELECT, DEFAULT_CO_ADDRESS_SELECT), useCOAddress);
		formItem.add(useCOAddress);
		formItem.add(label);
		section.add(formItem);
		
		section.add(clearLayer);

		Layer buttonLayer = new Layer(Layer.DIV);
		buttonLayer.setStyleClass("buttonLayer");
		form.add(buttonLayer);
		
		Layer span = new Layer(Layer.SPAN);
		span.add(new Text(iwrb.getLocalizedString(KEY_UPDATE, DEFAULT_UPDATE)));
		Link send = new Link(span);
		send.setToFormSubmit(form);
		buttonLayer.add(send);
		
		add(form);
	}

	private void updatePreferences(IWContext iwc) throws Exception {
		boolean hasErrors = false;
		Collection errors = new ArrayList();
		
		int fileID = -1;
		UploadFile uploadFile = iwc.getUploadedFile();
		if (uploadFile != null && uploadFile.getName() != null && uploadFile.getName().length() > 0) {
			try {
				FileInputStream input = new FileInputStream(uploadFile.getRealPath());

				ICFile file = ((com.idega.core.file.data.ICFileHome)com.idega.data.IDOLookup.getHome(ICFile.class)).create();
				file.setName(uploadFile.getName());
				file.setMimeType(uploadFile.getMimeType());
				file.setFileValue(input);
				file.setFileSize((int)uploadFile.getSize());
				file.store();
				
				fileID = ((Integer) file.getPrimaryKey()).intValue();
				uploadFile.setId(fileID);
				try {
					FileUtil.delete(uploadFile);
				}
				catch (Exception ex) {
					System.err.println("MediaBusiness: deleting the temporary file at " + uploadFile.getRealPath() + " failed.");
				}
			}
			catch (RemoteException e) {
				e.printStackTrace(System.err);
				uploadFile.setId(-1);
			}
		}

		String sEmail = iwc.isParameterSet(PARAMETER_EMAIL) ? iwc.getParameter(PARAMETER_EMAIL) : null;
		String phoneHome = iwc.getParameter(PARAMETER_PHONE_HOME);
		String phoneMobile = iwc.getParameter(PARAMETER_PHONE_MOBILE);
		String phoneWork = iwc.getParameter(PARAMETER_PHONE_WORK);
		String coStreetAddress = iwc.getParameter(PARAMETER_CO_STREET_ADDRESS);
		String coPostalCode = iwc.getParameter(PARAMETER_CO_POSTAL_CODE);
		String coCity = iwc.getParameter(PARAMETER_CO_CITY);
		String coCountry = iwc.getParameter(PARAMETER_CO_COUNTRY);
		boolean useCOAddress = iwc.isParameterSet(PARAMETER_CO_ADDRESS_SELECT);
		boolean messagesViaEmail = iwc.isParameterSet(PARAMETER_MESSAGES_VIA_EMAIL);
		boolean removeImage = iwc.isParameterSet(PARAMETER_REMOVE_IMAGE);

		boolean updateEmail = false;
		boolean updateCOAddress = false;

		if (sEmail != null) {
			updateEmail = EmailValidator.getInstance().validateEmail(sEmail);
			if (!updateEmail) {
				errors.add(iwrb.getLocalizedString(KEY_EMAIL_INVALID, DEFAULT_EMAIL_INVALID));
				hasErrors = true;
			}
		}
		/*
		 * IF user checks that he wants all letters sent by email but doesn't
		 * enter a valid email address he should get a warning
		 */
		if (messagesViaEmail && !updateEmail) {
			errors.add(iwrb.getLocalizedString(KEY_NO_EMAIL_FOR_LETTERS, DEFAULT_NO_EMAIL_FOR_LETTERS));
			hasErrors = true;
		}
		
		// Validate c/o-address
		if (useCOAddress) {
			if (coStreetAddress.equals("")) {
				errors.add(iwrb.getLocalizedString(KEY_CO_STREET_ADDRESS_MISSING, DEFAULT_CO_STREET_ADDRESS_MISSING));
				hasErrors = true;
			}
			if (coPostalCode.equals("")) {
				errors.add(iwrb.getLocalizedString(KEY_CO_POSTAL_CODE_MISSING, DEFAULT_CO_POSTAL_CODE_MISSING));
				hasErrors = true;
			}
			if (coCity.equals("")) {
				errors.add(iwrb.getLocalizedString(KEY_CO_CITY_MISSING, DEFAULT_CO_CITY_MISSING));
				hasErrors = true;
			}
			updateCOAddress = true;
		}

		if (!hasErrors) {
			UserBusiness ub = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			if (updateEmail) {
				ub.storeUserEmail(user, sEmail, true);
			}
			ub.updateUserHomePhone(user, phoneHome);
			ub.updateUserWorkPhone(user, phoneWork);
			ub.updateUserMobilePhone(user, phoneMobile);
			if (updateCOAddress) {
				Address coAddress = getCOAddress(iwc);
				coAddress.setStreetName(coStreetAddress);

				AddressBusiness addressBusiness = (AddressBusiness) IBOLookup.getServiceInstance(iwc, AddressBusiness.class);
				Country country = addressBusiness.getCountry(coCountry);
				PostalCode pc = addressBusiness.getPostalCodeAndCreateIfDoesNotExist(coPostalCode, coCity, country);

				coAddress.setPostalCode(pc);
				coAddress.setCity(coCity);
				coAddress.store();
			}
			MessageSession messageSession = getMessageSession(iwc);
			messageSession.setIfUserPreferesMessageByEmail(messagesViaEmail);
			CitizenAccountSession cas = getCitizenAccountSession(iwc);
			cas.setIfUserUsesCOAddress(useCOAddress);
			
			if (removeImage) {
				user.setSystemImageID(null);
				user.store();
			}
			if (fileID != -1) {
				user.setSystemImageID(fileID);
				user.store();
			}

			Layer header = new Layer(Layer.DIV);
			header.setStyleClass("header");
			add(header);
			
			Heading1 heading = new Heading1(iwrb.getLocalizedString("citizen_preferences", "Citizen preferences"));
			header.add(heading);
			
			Layer layer = new Layer(Layer.DIV);
			layer.setStyleClass("receipt");
			
			Layer image = new Layer(Layer.DIV);
			image.setStyleClass("receiptImage");
			layer.add(image);
			
			heading = new Heading1(iwrb.getLocalizedString(KEY_PREFERENCES_SAVED, DEFAULT_PREFERENCES_SAVED));
			layer.add(heading);
			
			Paragraph paragraph = new Paragraph();
			paragraph.add(new Text(iwrb.getLocalizedString(KEY_PREFERENCES_SAVED + "_text", DEFAULT_PREFERENCES_SAVED + " info")));
			layer.add(paragraph);
			
			try {
				ICPage page = ub.getHomePageForUser(user);
				paragraph.add(new Break(2));
				
				Layer span = new Layer(Layer.SPAN);
				span.add(new Text(iwrb.getLocalizedString("my_page", "My page")));
				Link link = new Link(span);
				link.setStyleClass("homeLink");
				link.setPage(page);
				paragraph.add(link);
			}
			catch (FinderException fe) {
				//No homepage found...
			}
			
			add(layer);
		}
		else {
			showErrors(iwc, errors);
			viewForm(iwc);
		}
	}

	private Address getCOAddress(IWContext iwc) {
		Address coAddress = null;
		try {
			UserBusiness ub = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			AddressHome ah = ub.getAddressHome();
			AddressType coType = ah.getAddressType2();

			Address address = ub.getUserAddressByAddressType(iwc.getCurrentUserId(), coType);
			if (address != null)
				return address;
			coAddress = ah.create();
			coAddress.setAddressType(coType);
			coAddress.store();
			user.addAddress(coAddress);
		}
		catch (Exception e) {
			super.add(new ExceptionWrapper(e, this));
		}
		return coAddress;
	}

	private MessageSession getMessageSession(IWContext iwc) throws RemoteException {
		return (MessageSession) com.idega.business.IBOLookup.getSessionInstance(iwc, MessageSession.class);
	}

	private CitizenAccountSession getCitizenAccountSession(IWContext iwc) throws RemoteException {
		return (CitizenAccountSession) com.idega.business.IBOLookup.getSessionInstance(iwc, CitizenAccountSession.class);
	}

	public void setToRemoveEmailWhenEmpty(boolean flag) {
	}
}