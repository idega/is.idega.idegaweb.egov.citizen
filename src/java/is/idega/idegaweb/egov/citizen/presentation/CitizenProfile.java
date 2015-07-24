package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.block.family.business.FamilyLogic;
import is.idega.idegaweb.egov.citizen.CitizenConstants;
import is.idega.idegaweb.egov.citizen.business.CitizenServices;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;


import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.business.IBOLookupException;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Strong;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SelectOption;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class CitizenProfile extends Block {

	private static final String REMOVE_ITEM_CLASS = "remove-item-class";
	private static final String RELATED_ITEM_CLASS = "simple-user-edit-related-item-class";
	private static final String RELATION_SELECTION_NAME = "relation_selection_name";
	private static final String ID_CONTANER_CLASS = "id-container-class";
	private static final String RELATION_TYPE_CONTAINER_CLASS = "relation-type-container";
	private static final String DELETE_IMG_CLASS = "delete-img";
	private static final String BORN_DATE_CLASS = "born-date-class";

	private String formId = null;
	private String relationInputId = null;
	private String relationSelectId = null;
	private String relationListsId = null;

	private String userId = null;

	@Autowired
	private CitizenServices citizenServices;

	private Boolean needFiles = Boolean.TRUE;

	private IWResourceBundle iwrb = null;

	private CitizenServices getCitizenServices() {
		if (citizenServices == null)
			ELUtil.getInstance().autowire(this);

		return citizenServices;
	}

	@Override
	public String getBundleIdentifier() {
		return CitizenConstants.IW_BUNDLE_IDENTIFIER;
	}

	@Override
	public void main(IWContext context) throws Exception {
		IWContext iwc = IWContext.getIWContext(context);

		IWBundle bundle = getBundle(iwc);
		iwrb = bundle.getResourceBundle(iwc);

		User user = getUser(iwc);
		if(user == null){
			Layer layer = new Layer();
			this.add(layer);
			layer.addText(iwrb.getLocalizedString("no_user_specified", "No user specified"));
			return;
		}

		this.add(this.getUserEditForm(user, iwc));
		Layer scriptLayer = new Layer();
		this.add(scriptLayer);
		addactions(scriptLayer);
		String filesNeeded = iwc.getParameter(CitizenConstants.NEEDED_SCRIPT_AND_STYLE_FILES);
		if((needFiles == null ) && (filesNeeded != null) && (filesNeeded.equalsIgnoreCase(CitizenConstants.False))){
			needFiles = Boolean.FALSE;
		}
		if(needFiles){
			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getNeededScripts(iwc));
			PresentationUtil.addStyleSheetsToHeader(iwc, getNeededStyles(iwc));
		}
	}

	public void setNeedFiles(Boolean needFiles) {
		this.needFiles = needFiles;
	}

	private User getUser(IWContext iwc){
		if(userId == null){
			userId = iwc.getParameter(CitizenConstants.USER_EDIT_USER_ID_PARAMETER);
		}
		User user = null;
		try{
			if(userId != null){
				user = getCitizenServices().getUserBusiness().getUser(Integer.valueOf(userId));
			}else if(iwc.isLoggedOn()){
				user =iwc.getCurrentUser();
			}
		}catch(RemoteException e){
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting user with id " + userId , e);
		}
		return user;
	}

	private Form getUserEditForm(User user, IWContext iwc){
		UserDataBean userData = null;
		boolean isData = false;

		Date born = null;
		String id = String.valueOf(-1);
		String resumeString = CoreConstants.EMPTY;
		if(user != null){
			born = user.getDateOfBirth();
			userData = getCitizenServices().getUserApplicationEngine().getUserInfo(user);
			if(userData != null){
				isData = true;
			}
			id = user.getId();
			resumeString = user.getResume();
			if(resumeString == null){
				resumeString = CoreConstants.EMPTY;
			}
		}
		String name = CoreConstants.EMPTY;
		String streetNameAndNumber = CoreConstants.EMPTY;
		String city = CoreConstants.EMPTY;
		String postalCode = CoreConstants.EMPTY;
		String postalBox = CoreConstants.EMPTY;
		String country = null;
		String personalId = CoreConstants.EMPTY;
		if(isData){
			name = userData.getName();
			if(name == null){
				name = CoreConstants.EMPTY;
			}
			personalId = userData.getPersonalId();
			if(personalId == null){
				personalId = CoreConstants.EMPTY;
			}
			streetNameAndNumber = userData.getStreetNameAndNumber();
			if(name == null){
				name = CoreConstants.EMPTY;
			}
			city = userData.getCity();
			if(city == null){
				city = CoreConstants.EMPTY;
			}
			postalCode = userData.getPostalCodeId();
			if(postalCode == null){
				postalCode = CoreConstants.EMPTY;
			}
			postalBox = userData.getPostalBox();
			if (StringUtil.isEmpty(postalBox))
				postalBox = CoreConstants.EMPTY;
			country = userData.getCountryName();
			if (StringUtil.isEmpty(country))
				country = CoreConstants.EMPTY;
		}

		Form form = new Form();
		form.setStyleClass("dwr-form");
		Table2 table = new Table2();
		form.add(table);
		formId = form.getId();

		// Id
		TableRow row = table.createRow();
		HiddenInput idInput = new HiddenInput(CitizenConstants.USER_EDIT_USER_ID_PARAMETER,id);
		TableCell2 cell = row.createCell();
		cell.add(idInput);

		// Name
		Strong cellInfo = new Strong();
		cell.add(cellInfo);
		cellInfo.addText(iwrb.getLocalizedString("name", "Name"));
		TextInput input = new TextInput(CitizenConstants.USER_EDIT_NAME_PARAMETER,name);
		cell = row.createCell();
		cell.add(input);
		cell.setStyleClass("input-container");

		// Personal ID
		row = table.createRow();
		cellInfo = new Strong();
		row.createCell().add(cellInfo);
		cellInfo.addText(iwrb.getLocalizedString("personal_id", "Personal id"));
		input = new TextInput(CitizenConstants.USER_EDIT_PERSONAL_ID_PARAMETER,personalId);
		row.createCell().add(input);

		// Born
		row = table.createRow();
		cellInfo = new Strong();
		cellInfo.addText(iwrb.getLocalizedString("birth_date", "Birth date"));
		row.createCell().add(cellInfo);
		input = new TextInput(CitizenConstants.USER_EDIT_BORN_PARAMETER);
		row.createCell().add(input);
		if(born != null){
			input.setValue(born.toString());
		}
		input.setStyleClass(BORN_DATE_CLASS);

		// Street and number
		row = table.createRow();
		cellInfo = new Strong();
		cellInfo.addText(iwrb.getLocalizedString("street_and_number", "Street and number"));
		row.createCell().add(cellInfo);
		input = new TextInput(CitizenConstants.USER_EDIT_STREET_AND_NUMBER_PARAMETER,streetNameAndNumber);
		cell = row.createCell();
		cell.add(input);
		cell.setStyleClass("input-container");

		// City
		row = table.createRow();
		cellInfo = new Strong();
		cellInfo.addText(iwrb.getLocalizedString("city", "City"));
		row.createCell().add(cellInfo);
		input = new TextInput(CitizenConstants.USER_EDIT_CITY_PARAMETER,city);
		cell = row.createCell();
		cell.add(input);
		cell.setStyleClass("input-container");

		// Postal code
		row = table.createRow();
		cellInfo = new Strong();
		cellInfo.addText(iwrb.getLocalizedString("postal_code", "Postal code"));
		row.createCell().add(cellInfo);
		input = new TextInput(CitizenConstants.USER_EDIT_POSTAL_CODE_PARAMETER,postalCode);
		cell = row.createCell();
		cell.add(input);
		cell.setStyleClass("input-container");

		//	Postal box
		row = table.createRow();
		cellInfo = new Strong();
		cellInfo.addText(iwrb.getLocalizedString("postal_box", "Postal box"));
		row.createCell().add(cellInfo);
		input = new TextInput(CitizenConstants.USER_EDIT_POSTAL_BOX_PARAMETER, postalBox);
		cell = row.createCell();
		cell.add(input);
		cell.setStyleClass("input-container");

		// Country
		row = table.createRow();
		cellInfo = new Strong();
		cellInfo.addText(iwrb.getLocalizedString("country", "Country"));
		row.createCell().add(cellInfo);
		DropdownMenu dropdown = new DropdownMenu(CitizenConstants.USER_EDIT_COUNTRY_PARAMETER);
		row.createCell().add(dropdown);
		@SuppressWarnings("unchecked")
		Collection<Locale> locales = ICLocaleBusiness.getListOfAllLocalesJAVA();
		TreeMap<String, Locale> localeMap = new TreeMap<String, Locale>();
		Locale currentLocale = iwc.getCurrentLocale();
		for (Locale locale : locales){
			String countryName = locale.getDisplayCountry(currentLocale);
			if(!StringUtil.isEmpty(countryName)){
				localeMap.put(countryName, locale);
			}
		}
		Set <String> keys = localeMap.keySet();
		for(String countryName : keys){
			dropdown.addMenuElement(localeMap.get(countryName).toString(), countryName);
		}
		SelectOption option = new SelectOption(iwrb.getLocalizedString("choose_country", "Choose country"),-1);
		dropdown.addFirstOption(option);

		if (!StringUtil.isEmpty(country)) {
			dropdown.setSelectedElement(country);
		}

		// Marital status
		//TODO: search for types

		// Family
		cellInfo = new Strong();
		cellInfo.addText(iwrb.getLocalizedString("family", "Family"));
		table.createRow().createCell().add(cellInfo);
		row = table.createRow();
		cell = row.createCell();
		cell.setStyleAttribute("visibility", "hidden");
		input = new TextInput("tag[]");
		cell.add(input);
		relationInputId = input.getId();
		List<String> relations = null;
		try {
			relations = getCitizenServices().getFamilyRelationTypes(iwc);
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting user " + id + " family relations", e);
			relations = Collections.emptyList();
		}
		dropdown = getFamilyRelationSelection(iwc, RELATION_SELECTION_NAME, relations);
		row.createCell().add(dropdown);
		dropdown.setOnChange("SimpleUserEditFormHelper.showUserInput(this, '" + cell.getId() + CoreConstants.JS_STR_PARAM_END);
		relationSelectId = dropdown.getId();
		
		

		table = new Table2();
		form.add(table);
		Lists relationList = getfamilyMemberList(iwc, user, relations);
		table.createRow().createCell().add(relationList);
		relationListsId = relationList.getId();

		// Resume
		cellInfo = new Strong();
		cellInfo.addText(iwrb.getLocalizedString("resume", "Resume"));
		table.createRow().createCell().add(cellInfo);
		TextArea resume = new TextArea(CitizenConstants.USER_EDIT_RESUME_PARAMETER,resumeString);
		table.createRow().createCell().add(resume);
		resume.setColumns(50);

		// Submit
		GenericButton buttonSubmit = new GenericButton("buttonSubmit", iwrb.getLocalizedString("save", "Save"));
		form.add(buttonSubmit);
		buttonSubmit.setOnClick("SimpleUserEditFormHelper.saveUser('#" + formId + CoreConstants.JS_STR_PARAM_END);

		return form;
	}

	private Lists getfamilyMemberList(IWContext iwc, User theUser, Collection <String> relations){
		Lists list = new Lists();
		list.setStyleClass("relation-list");
		FamilyLogic fl = null;
		try {
			fl = getCitizenServices().getFamilyLogic(iwc);
		} catch (IBOLookupException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "failed getting family logic", e);
			return list;
		}
		Map <String,Collection<User>> members = getCitizenServices().getFamilyMembers(iwc, theUser,relations);
		Set <String> keys = members.keySet();
		UserApplicationEngine userApplicationEngine = getCitizenServices().getUserApplicationEngine();
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		for(String key : keys){
			Collection <User> users = members.get(key);
			for(User user : users){
				ListItem li = new ListItem();
				list.add(li);
				li.setStyleClass(RELATED_ITEM_CLASS);
				Table2 table =  getUserInfoView(userApplicationEngine.getUserInfo(user),key,fl,iwrb,iwb,false);;
				li.add(table);
			}
		}
		return list;
	}

	public static Table2 getUserInfoView(UserDataBean userData,String relation,
			FamilyLogic familyLogic,IWResourceBundle iwrb,IWBundle iwb,boolean addRelationNameToInput ){
		Table2 table = new Table2();
		TableRow row = table.createRow();
		table.setStyleClass("user-info-table");

		// User to add
		TableCell2 cell = row.createCell();
		HiddenInput input = new HiddenInput();
		if(addRelationNameToInput){
			input.setName(relation);
		}
		input.setValue(String.valueOf(userData.getUserId()));
		cell.add(input);
		input.setStyleClass(ID_CONTANER_CLASS);


		// Relation type
		Label label = new Label();
		label.addText(familyLogic.getRelationName(iwrb, relation) + CoreConstants.COLON);
		cell.add(label);
		cell.setStyleClass("relation-type");

		input = new HiddenInput();
		cell.add(input);
		input.setValue(relation);
		input.setStyleClass(RELATION_TYPE_CONTAINER_CLASS);


		// Picture
		Image image = new Image(userData.getPictureUri());
		row.createCell().add(image);

		// Name
		cell = row.createCell();
		cell.setStyleClass("relation-user-info-name");
		label = new Label();
		cell.add(label);
		label.addText(userData.getName());

		// Remove
		cell = row.createCell();
		Image removeImage = new Image();
		cell.add(removeImage);
		String url = iwb.getVirtualPathWithFileNameString("delete.png");
		removeImage.setURL(url);
		cell.setStyleClass(REMOVE_ITEM_CLASS);
		removeImage.setTitle(iwrb.getLocalizedString("remove", "Remove"));
		removeImage.setStyleClass(DELETE_IMG_CLASS);

		return table;
	}

	private DropdownMenu getFamilyRelationSelection(IWContext iwc, String name, Collection<String> relations){
		DropdownMenu dropdown = new DropdownMenu(name);
		try {
			FamilyLogic fl = getCitizenServices().getFamilyLogic(iwc);
			for(String relation : relations){
				dropdown.addMenuElement(relation, fl.getRelationName(iwrb, relation));
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting family relations ", e);
		}
		dropdown.addFirstOption(new SelectOption(iwrb.getLocalizedString("choose_relationship", "Choose relationship"), String.valueOf(-1)));
		return dropdown;
	}

	private void addactions(Layer layer){
		StringBuilder actions = new StringBuilder("SimpleUserEditFormHelper.initialize = function(){")
		.append("SimpleUserEditFormHelper.FORM_SELECTOR = '#")
		.append(formId).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.RELATION_INPUT_SELECTOR = '#")
		.append(relationInputId).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.RELATION_SELECT_SELECTOR= '#")
		.append(relationSelectId).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.RELATION_LIST_SELECTOR= '#")
		.append(relationListsId).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.NON_TRIVIAL_USER_PHRASE= '")
		.append(iwrb.getLocalizedString("entered_search_request_does_not_trivially_identifies_the_person",
				"Entered request does not trivially identifies the person")).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.RELATED_ITEM_CLASS= '")
		.append(RELATED_ITEM_CLASS).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.REMOVE_ITEM_CLASS= '")
		.append(REMOVE_ITEM_CLASS).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.ID_CONTANER_CLASS= '")
		.append(ID_CONTANER_CLASS).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.RESUME_INPUT_SELECTOR = '[name =")
		.append(CitizenConstants.USER_EDIT_RESUME_PARAMETER).append("]';")
		.append("SimpleUserEditFormHelper.RELATION_TYPE_CONTAINER_CLASS= '")
		.append(RELATION_TYPE_CONTAINER_CLASS).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.USER_EDIT_USER_ID_PARAMETER= '")
		.append(CitizenConstants.USER_EDIT_USER_ID_PARAMETER).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.USER_EDIT_DATE_SELECTOR= '.")
		.append(BORN_DATE_CLASS).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.FAILED_MSG= '")
		.append(iwrb.getLocalizedString("failed", "Failed")).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.REMOVED_MSG = '")
		.append(iwrb.getLocalizedString("removed", "Removed")).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("\n}");

		String actionString = PresentationUtil.getJavaScriptAction(actions.toString());
		layer.add(actionString);
	}

	private List<String> getNeededScripts(IWContext iwc){
		List<String> scripts = new ArrayList<String>();

		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();
			scripts.add(jQuery.getBundleURIToJQueryLib());

			scripts.add(jQuery.getBundleURIToJQueryUILib(
						Web2BusinessBean.JQUERY_UI_LATEST_VERSION,
						"js/jquery-ui-" + Web2BusinessBean.JQUERY_UI_LATEST_VERSION
						+ ".custom.min.js"));
			scripts.add(jQuery.getBundleURIToJQueryUILib(
						Web2BusinessBean.JQUERY_UI_1_8_17_VERSION,
						"ui.autocomplete.html.js"));

			Locale locale = iwc.getLocale();
			scripts.add(jQuery.getBundleURIToJQueryUILib(
						Web2BusinessBean.JQUERY_UI_LATEST_VERSION,
						"i18n") + "/ui.datepicker-"
						+ locale.getLanguage() + ".js");


			scripts.add(web2.getBundleUriToHumanizedMessagesScript());

			try{
				StringBuilder path = new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX).append("/jquery-tagedit-remake.js");
				scripts.add(web2.getBundleURIWithinScriptsFolder(path.toString()));
				scripts.add(web2.getBundleURIWithinScriptsFolder(new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
						.append(CoreConstants.SLASH)
						.append(Web2BusinessBean.TAGEDIT_SCRIPT_FILE_AUTOGROW).toString()));
				scripts.add(web2.getBundleURIWithinScriptsFolder(new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
				.append("/jquery.autoresizev-textarea.js").toString()));
			}catch(RemoteException e){
				Logger.getLogger(getClass().getName()).log(Level.WARNING,CoreConstants.EMPTY,e);
			}
		}else{
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/SimpleUserEditFormHelper.js"));
		scripts.add("/dwr/interface/CitizenServices.js");

		return scripts;
	}

	private List<String> getNeededStyles(IWContext iwc){
		List<String> styles = new ArrayList<String>();

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();

			styles.add(web2.getBundleURIToFancyBoxStyleFile());

			styles.add(jQuery.getBundleURIToJQueryUILib(
					Web2BusinessBean.JQUERY_UI_LATEST_VERSION,
					"themes/smoothness/minified/jquery-ui.min.css"));

			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());

			styles.addAll(web2.getBundleURIsToTageditStyleFiles());


		}else{
			Logger.getLogger(getClass().getName()).warning("Failed getting Web2Business no jQuery and it's plugins files were added");
		}
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		styles.add(iwb.getVirtualPathWithFileNameString("style/citizen.css"));
		styles.add(iwb.getVirtualPathWithFileNameString("style/simpleUserEditForm.css"));
		return styles;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}