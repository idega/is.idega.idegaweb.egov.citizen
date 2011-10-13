package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.block.family.business.FamilyLogic;
import is.idega.idegaweb.egov.citizen.CitizenConstants;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.business.IBOLookupException;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.IWDatePicker;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SelectOption;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class SimpleUserEditForm extends IWBaseComponent{
	private static final String REMOVE_ITEM_CLASS = "remove-item-class";
	private static final String RELATION_SELECTION_NAME = "relation_selection_name";
	
	private String formId = null;
	private String relationInputId = null;
	private String relationSelectId = null;
	private String relationListsId = null;
	
	@Autowired
	private CitizenServices citizenServices;
	
	
	private Boolean needFiles = Boolean.TRUE;
	
	IWContext iwc = null;
	IWResourceBundle iwrb = null;
	public SimpleUserEditForm(){
		iwc = CoreUtil.getIWContext();
		IWBundle bundle = iwc.getIWMainApplication().getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		iwrb = bundle.getResourceBundle(iwc);
		ELUtil.getInstance().autowire(this);
	}
	
	@Override
	protected void initializeComponent(FacesContext context) {

		this.add(this.getUserEditForm(getUserData()));
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

//		this.addActions();
	}
	public void setNeedFiles(Boolean needFiles) {
		this.needFiles = needFiles;
	}
	private User getUserData(){
		User user = null;
		String id = iwc.getParameter(CitizenConstants.USER_EDIT_USER_ID_PARAMETER);
		try{
			if(id != null){
				user = citizenServices.getUserBusiness().getUser(Integer.valueOf(id));
			}else if(iwc.isLoggedOn()){
				user =iwc.getCurrentUser();
			}
		}catch(RemoteException e){
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting user " + id , e);
		}
		return user;
	}
	
	private Form getUserEditForm(User user){
		UserDataBean userData = null;
		boolean isData = false;
		
		Date born = null;
		String id = "-1";
		String resumeString = CoreConstants.EMPTY;
//		List
		if(user != null){
			born = user.getDateOfBirth();
			userData = citizenServices.getUserApplicationEngine().getUserInfo(user);
			if(userData != null){
				isData = true;
			}
			id = user.getId();
			resumeString = user.getDescription();
			if(resumeString == null){
				resumeString = CoreConstants.EMPTY;
			}
//			FamilyLogic familyLogic = null;
//			try {
//				familyLogic = IBOLookup.getServiceInstance(iwc, FamilyLogic.class);
//				familyLogic.getf
//			} catch (IBOLookupException e) {
//				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting family data" , e);
//			}
		}
		if(born == null){
			born = 	new Date();
		}
		String name = CoreConstants.EMPTY;
		String streetNameAndNumber = CoreConstants.EMPTY;
		String city = CoreConstants.EMPTY;
		String postalCode = CoreConstants.EMPTY;
		String country = null;
		if(isData){
			name = userData.getName();
			if(name == null){
				name = CoreConstants.EMPTY;
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
			country = userData.getCountryName();
		}
		
		Form form = new Form();
		Table2 table = new Table2();
		form.add(table);
		formId = form.getId();
		
		// Id
		TableRow row = table.createRow();
		HiddenInput idInput = new HiddenInput(CitizenConstants.USER_EDIT_USER_ID_PARAMETER,id);
		TableCell2 cell = row.createCell();
		cell.add(idInput);
		
		// Name
		Label cellInfo = new Label();
		cellInfo.addText(iwrb.getLocalizedString("name", "Name"));
		cell.add(cellInfo);
		TextInput input = new TextInput(CitizenConstants.USER_EDIT_NAME_PARAMETER,name);
		row.createCell().add(input);
		
		// Born
		row = table.createRow();
		cellInfo = new Label();
		cellInfo.addText(iwrb.getLocalizedString("birth_date", "Birth date"));
		row.createCell().add(cellInfo);
		IWDatePicker bornDate = new IWDatePicker(CitizenConstants.USER_EDIT_BORN_PARAMETER);
		row.createCell().add(bornDate);
		bornDate.setDate(born);
		
		// Street and number
		row = table.createRow();
		cellInfo = new Label();
		cellInfo.addText(iwrb.getLocalizedString("street_and_number", "Street and number"));
		row.createCell().add(cellInfo);
		input = new TextInput(CitizenConstants.USER_EDIT_STREET_AND_NUMBER_PARAMETER,streetNameAndNumber);
		row.createCell().add(input);
		
		// City
		row = table.createRow();
		cellInfo = new Label();
		cellInfo.addText(iwrb.getLocalizedString("city", "City"));
		row.createCell().add(cellInfo);
		input = new TextInput(CitizenConstants.USER_EDIT_CITY_PARAMETER,city);
		row.createCell().add(input);
		
		// Postal code
		row = table.createRow();
		cellInfo = new Label();
		cellInfo.addText(iwrb.getLocalizedString("postal_code", "Postal code"));
		row.createCell().add(cellInfo);
		input = new TextInput(CitizenConstants.USER_EDIT_POSTAL_CODE_PARAMETER,postalCode);
		row.createCell().add(input);
		
		// Country
		row = table.createRow();
		cellInfo = new Label();
		cellInfo.addText(iwrb.getLocalizedString("country", "Country"));
		row.createCell().add(cellInfo);
		DropdownMenu dropdown = new DropdownMenu(CitizenConstants.USER_EDIT_COUNTRY_PARAMETER);
		row.createCell().add(dropdown);
		@SuppressWarnings("unchecked")
		Collection<Locale> locales = ICLocaleBusiness.getListOfAllLocalesJAVA();
		TreeMap <String, Locale> localeMap = new TreeMap<String, Locale> ();
		for (Locale locale : locales){
			String countryName = locale.getDisplayCountry();
			if(!StringUtil.isEmpty(countryName)){
				localeMap.put(countryName, locale);
			}
		}
		Set <String> keys = localeMap.keySet();
		for(String countryName : keys){
			dropdown.addMenuElement(localeMap.get(countryName).toString(), countryName);
		}
		
		if(country != null){
			dropdown.setSelectedElement(country);
		}else{
			SelectOption option = new SelectOption(iwrb.getLocalizedString("select_country", "Select country"),-1);
			dropdown.addFirstOption(option);
		}
		
		// Marital status
		//TODO: search for types
		
		// Family
		cellInfo = new Label();
		cellInfo.addText(iwrb.getLocalizedString("family", "Family"));
		table.createRow().createCell().add(cellInfo);
		row = table.createRow();
		input = new TextInput("tag[]");
		relationInputId = input.getId();
		row.createCell().add(input);
		dropdown = getFamilyRelationSelection(RELATION_SELECTION_NAME);
		row.createCell().add(dropdown);
		relationSelectId = dropdown.getId();
//		FamilyRelationConnector con = new FamilyRelationConnector();
//		try {
//			row.createCell().add(con.getRelationMenu(iwc));
//		} catch (RemoteException e) {
//			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed adding relation menu"	 , e);
//		}
//		GenericButton button = new GenericButton("buttonAdd", iwrb.getLocalizedString("add", "Add"));
//		row.createCell().add(button);
//		StringBuilder action = new StringBuilder("SimpleUserEditFormHelper.addFamilyMember('#")
//				.append(form.getId())
//				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(RELATION_SELECTION_NAME)
//				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(RELATION_VALUE_NAME)
//				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(CitizenConstants.USER_EDIT_USER_ID_PARAMETER)
//				.append(CoreConstants.JS_STR_PARAM_END);
//		button.setOnClick(action.toString());
		row = table.createRow();
		Lists relationList = getfamilyMemberList(user);
		relationListsId = relationList.getId();
		row.createCell().add(relationList);
		
		
		
		// Resume
		row = table.createRow();
		cellInfo = new Label();
		cellInfo.addText(iwrb.getLocalizedString("resume", "Resume"));
		row.createCell().add(cellInfo);
		TextArea resume = new TextArea(CitizenConstants.USER_EDIT_RESUME_PARAMETER,resumeString);
		row.createCell().add(resume);
		
		// Submit
		GenericButton buttonSubmit = new GenericButton("buttonSubmit", iwrb.getLocalizedString("save", "Save"));
		form.add(buttonSubmit);
		buttonSubmit.setOnClick("SimpleUserEditFormHelper.saveUser('#" + formId + CoreConstants.JS_STR_PARAM_END);
		
		return form;
	}
	
	private Lists getfamilyMemberList(User theUser){
		Lists list = new Lists();
		FamilyLogic fl = null;
		try {
			fl = citizenServices.getFamilyLogic(iwc);
		} catch (IBOLookupException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "failed getting family logic", e);
			return list;
		}
		Map <String,Collection<User>> members = citizenServices.getFamilyMembers(iwc, theUser);
		Set <String> keys = members.keySet();
		UserApplicationEngine userApplicationEngine = citizenServices.getUserApplicationEngine();
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		for(String key : keys){
			Collection <User> users = members.get(key);
			for(User user : users){
				ListItem li = new ListItem();
				list.add(li);
				Table2 table =  getUserInfoView(userApplicationEngine.getUserInfo(user),key,fl,iwrb,iwb);;
				li.add(table);
			}
		}
		return list;
	}
	
	public static Table2 getUserInfoView(UserDataBean userData,String relation,
			FamilyLogic familyLogic,IWResourceBundle iwrb,IWBundle iwb ){
		Table2 table = new Table2();
		TableRow row = table.createRow();
		
		// User to add
		HiddenInput input = new HiddenInput(relation, String.valueOf(userData.getUserId()));
		TableCell2 cell = row.createCell();
		cell.add(input);
		
		// Relation type
		Label label = new Label();
		label.addText(familyLogic.getRelationName(iwrb, relation));
		cell.add(label);
		
		
		// Picture
		Image image = new Image(userData.getPictureUri());
		row.createCell().add(image);
		
		// Name
		label = new Label();
		label.addText(userData.getName());
		row.createCell().add(label);
		
		// Remove
		cell = row.createCell();
		Image removeImage = new Image();
		cell.add(removeImage);
		String url = iwb.getVirtualPathWithFileNameString("delete.png");
		removeImage.setURL(url);
		cell.setStyleClass(REMOVE_ITEM_CLASS);
		removeImage.setTitle(iwrb.getLocalizedString("remove", "Remove"));
		
		return table;
	}
	
	private DropdownMenu getFamilyRelationSelection(String name){
		DropdownMenu dropdown = new DropdownMenu(name);
		try {
			List<String> relations = citizenServices.getFamilyRelationTypes(iwc);
			FamilyLogic fl = citizenServices.getFamilyLogic(iwc);
			for(String relation : relations){
				dropdown.addMenuElement(relation, fl.getRelationName(iwrb, relation));
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting family relations ", e);
		}
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
		.append(iwrb.getLocalizedString("user_not_found", "User not found")).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("SimpleUserEditFormHelper.REMOVE_ITEM_CLASS= '")
		.append(REMOVE_ITEM_CLASS).append(CoreConstants.JS_STR_INITIALIZATION_END)
		.append("\n}");
		
		String actionString = PresentationUtil.getJavaScriptAction(actions.toString());
		layer.add(actionString);
	}
	
	/**
	 * Gets the scripts that is need for this element to work
	 * if this element is loaded dynamically (ajax) and not
	 * in frame, than containing element have to add theese
	 * scriptFiles.
	 * @return script files uris
	 */
	public static List<String> getNeededScripts(IWContext iwc){
		List<String> scripts = new ArrayList<String>();

		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();
			scripts.add(jQuery.getBundleURIToJQueryLib());

			scripts.add(jQuery.getBundleURIToJQueryUILib("1.8.14","js/jquery-ui-1.8.14.custom.min.js"));
			scripts.add(jQuery.getBundleURIToJQueryUILib("1.8.14","development-bundle/ui/jquery.ui.autocomplete.js"));
			scripts.add(jQuery.getBundleURIToJQueryUILib("1.8.14","development-bundle/ui/jquery-ui-autocomplete-html.js"));


			scripts.add(web2.getBundleUriToHumanizedMessagesScript());

			try{
				StringBuilder path = new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
				.append("/jquery-tagedit-remake.js");
				scripts.add(web2.getBundleURIWithinScriptsFolder(path.toString()));
				scripts.add(web2.getBundleURIWithinScriptsFolder(new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
						.append(CoreConstants.SLASH)
						.append(Web2BusinessBean.TAGEDIT_SCRIPT_FILE_AUTOGROW).toString()));
			}catch(RemoteException e){
				Logger.getLogger("SimpleUserEditForm").log(Level.WARNING,CoreConstants.EMPTY,e);
			}
		}else{
			Logger.getLogger("SimpleUserEditForm").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/SimpleUserEditFormHelper.js"));
		scripts.add("/dwr/interface/CitizenServices.js");

		return scripts;
	}

	/**
	 * Gets the stylesheets that is need for this element to work
	 * if this element is loaded dynamically (ajax) and not
	 * in frame, than containing element have to add theese
	 * files.
	 * @return style files uris
	 */
	public static List<String> getNeededStyles(IWContext iwc){
		List<String> styles = new ArrayList<String>();

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();

			styles.add(web2.getBundleURIToFancyBoxStyleFile());

			styles.add(jQuery.getBundleURIToJQueryUILib("1.8.14","css/ui-lightness/jquery-ui-1.8.14.custom.css"));

			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());

			styles.addAll(web2.getBundleURIsToTageditStyleFiles());


		}else{
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
//		styles.add(iwb.getVirtualPathWithFileNameString("style/simpleUserEditForm.css"));
		return styles;
	}
}
