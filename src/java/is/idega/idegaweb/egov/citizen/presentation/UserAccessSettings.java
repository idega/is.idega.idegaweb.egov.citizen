package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.idegaweb.egov.citizen.CitizenConstants;
import is.idega.idegaweb.egov.citizen.business.CitizenServices;
import is.idega.idegaweb.egov.citizen.data.LoginData;
import is.idega.idegaweb.egov.citizen.data.LoginDataHome;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.FinderException;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.business.LoginContext;
import com.idega.core.localisation.data.ICLanguage;
import com.idega.core.localisation.data.ICLanguageHome;
import com.idega.data.IDOLookupException;
import com.idega.data.IDORelationshipException;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.ui.FieldSet;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.ui.TextInput;
import com.idega.user.bean.UserDataBean;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class UserAccessSettings extends Block {
	
	private static final String LANGUAGE_CONTAINER_CLASS = "language-container-class";
	private static final String LANGUAGE_SELECT_CLASS = "language-select-class";
	
	private static final String SINGLE_SING_ON_ID = "single-sing-on-user-id";
	private static final String SINGLE_SING_ON_SERVER = "single-sing-on-server";
	private static final String SINGLE_SING_ON_NAME = "single-sing-on-name";
	private static final String SINGLE_SING_ON_PASSWORD = "single-sing-on-password";
	
	private String formId = null;
	
	private String userId = null;
	
	
	@Autowired
	private CitizenServices citizenServices;
	
	private Boolean needFiles = Boolean.TRUE;
	
	public UserAccessSettings(){
		ELUtil.getInstance().autowire(this);
	}
	
	@Override
	public String getBundleIdentifier() {
		return CitizenConstants.IW_BUNDLE_IDENTIFIER;
	}
	
	@Override
	public void main(IWContext iwc) throws Exception {
		IWBundle bundle = getBundle(iwc);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		
		User user = getUser(iwc);
		if(user == null){
			Layer layer = new Layer();
			this.add(layer);
			layer.addText(iwrb.getLocalizedString("no_user_specified", "No user specified"));
			return;
		}
		this.add(this.getUserEditForm(user,iwrb,iwc));
		Layer scriptLayer = new Layer();
		this.add(scriptLayer);
		addactions(scriptLayer,iwrb);
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
				user = citizenServices.getUserBusiness().getUser(Integer.valueOf(userId));
			}else if(iwc.isLoggedOn()){
				user =iwc.getCurrentUser();
			}
		}catch(RemoteException e){
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting user with id " + userId , e);
		}
		return user;
	}
	
	private Layer getUserEditForm(User user,IWResourceBundle iwrb,IWContext iwc){
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		UserDataBean userData = null;
		boolean isData = false;
//		LoginDBHandler.getUserLogin(user).getUserPassword();
		String id = String.valueOf(-1);
		String username = CoreConstants.EMPTY;
		Collection<ICLanguage> userLanguages = Collections.emptyList();
		if(user != null){
			userData = citizenServices.getUserApplicationEngine().getUserInfo(user);
			if(userData != null){
				isData = true;
			}
			id = user.getId();
			LoginContext loginContext = LoginBusinessBean.getLoginContext(user);
			username = loginContext.getUserName();
			if(username == null){
				username = CoreConstants.EMPTY;
			}
			try {
				userLanguages = user.getLanguages();
			} catch (IDORelationshipException e) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting user with id " + userId + " languages", e);
			}
		}
		String name = CoreConstants.EMPTY;
		if(isData){
			name = userData.getName();
			if(name == null){
				name = CoreConstants.EMPTY;
			}
		}
		Layer main = new Layer();
		
		Form form = new Form();
		main.add(form);
		form.setStyleClass("dwr-form");
		
		// Id
		Layer layer = new Layer();
		form.add(layer);
		HiddenInput idInput = new HiddenInput(CitizenConstants.USER_EDIT_USER_ID_PARAMETER,id);
		layer.add(idInput);
		
		// Name
		layer = new Layer();
		form.add(layer);
		layer.setStyleClass("formItem");
		Label nameLabel = new Label();
		nameLabel.add(name);
		Label cellInfo = new Label(nameLabel);
		layer.add(cellInfo);
		layer.add(nameLabel);
		cellInfo.addText(iwrb.getLocalizedString("name", "Name"));
		
		FieldSet fieldset = new FieldSet(iwrb.getLocalizedString("login_settings","Login settings"));
		form.add(fieldset);
		formId = form.getId();
		
		
		// Username
		layer = new Layer();
		fieldset.add(layer);
		layer.setStyleClass("formItem");
		TextInput input = new TextInput(CitizenConstants.USER_EDIT_USERNAME_PARAMETER,username);
		cellInfo = new Label(input);
		layer.add(cellInfo);
		layer.add(input);
		cellInfo.addText(iwrb.getLocalizedString("login", "Login"));
		
		// Password
		layer = new Layer();
		fieldset.add(layer);
		layer.setStyleClass("formItem");
		PasswordInput passwordInput = new PasswordInput(CitizenConstants.USER_EDIT_PASSWORD_PARAMETER);
		cellInfo = new Label(passwordInput);
		layer.add(cellInfo);
		layer.add(passwordInput);
		cellInfo.addText(iwrb.getLocalizedString("password", "Password"));
		
		// Languages
		FieldSet languagesFieldset = new FieldSet(iwrb.getLocalizedString("languages_you_know", "Languages you know"));
		form.add(languagesFieldset);
		languagesFieldset.add(getLanguageSelectLayer(userLanguages, iwb, iwrb,user));
		
		// Single sing on
		Form singleSingOnFrom = new Form();
		main.add(singleSingOnFrom);
		FieldSet singleSingonFieldset = new FieldSet(iwrb.getLocalizedString("single_sing_on_access", "Single sing-on access"));
		singleSingOnFrom.add(singleSingonFieldset);
		Layer singleSingOn = new  Layer();
		singleSingonFieldset.add(singleSingOn);
		try {
			Collection<Block> layers = getSingleSingOnLayers(user,citizenServices.getLoginDataHome());
			for(Block login : layers){
				singleSingOn.add(login);
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed creating single sing on layers", e);
		}
		
		// Add single sing on
		layer = new Layer();
		layer.setStyleClass("add-more-layer");
		singleSingonFieldset.add(layer);
		String singleSingOnSelector = CoreConstants.NUMBER_SIGN + singleSingOn.getId();
		GenericButton add = new GenericButton("more", iwrb.getLocalizedString("add_single_sing_on_access", "Add single sing-on access"));
		layer.add(add);
		add.setOnClick("UserAccessSettingsHelper.addSingleSingOn('" + singleSingOnSelector + CoreConstants.JS_STR_PARAM_END);
		
		// Submit
		GenericButton buttonSubmit = new GenericButton("buttonSubmit", iwrb.getLocalizedString("save", "Save"));
		main.add(buttonSubmit);
		buttonSubmit.setOnClick("UserAccessSettingsHelper.saveAccessSettings('#" + formId +CoreConstants.JS_STR_PARAM_SEPARATOR
				+ singleSingOnSelector +CoreConstants.JS_STR_PARAM_SEPARATOR +id+ CoreConstants.JS_STR_PARAM_END);
		
		
		return main;
	}
	
	public static Block getSingleSingOnLayer(){
		return new SingleSingOn(SINGLE_SING_ON_SERVER, SINGLE_SING_ON_NAME, 
				SINGLE_SING_ON_PASSWORD,SINGLE_SING_ON_ID);
	}
	
	public static ArrayList<Block> getSingleSingOnLayers(User user,LoginDataHome loginDataHome) throws Exception{
		Collection <LoginData> logins =  loginDataHome.getLoginData(user);
		ArrayList<Block> layers = new ArrayList<Block>(logins.size());
		for(LoginData data : logins){
			SingleSingOn login = new SingleSingOn(SINGLE_SING_ON_SERVER, SINGLE_SING_ON_NAME, 
					SINGLE_SING_ON_PASSWORD,SINGLE_SING_ON_ID);
			login.setAddress(data.getService().getPrimaryKey().toString());
			login.setName(data.getUserName());
			login.setLoginId(data.getPrimaryKey().toString());
			layers.add(login);
		}
		return layers;
	}
	
	private Layer getLanguageSelectLayer(Collection<ICLanguage> userLanguages,IWBundle iwb,IWResourceBundle iwrb,User user){
		Layer layer = new Layer();
		layer.setStyleClass("language-layer");
		try{
			ICLanguageHome icLanguageHome= citizenServices.getICLanguageHome();
			@SuppressWarnings("unchecked")
			Collection<ICLanguage> languages = icLanguageHome.findAll();
			SelectionBox languageSelection =  new SelectionBox();
			layer.add(languageSelection);
			languageSelection.setStyleClass(LANGUAGE_SELECT_CLASS);
			for(ICLanguage language : languages){
				languageSelection.addMenuElement(language.getPrimaryKey().toString(), language.getName());
			}
			Layer addLayerr = new Layer();
			layer.add(addLayerr);
			Layer container = new Layer();
			layer.add(container);
			container.setStyleClass("language-table-container");
			Table2 languageContainer = new Table2();
			container.add(languageContainer);
			languageContainer.setStyleClass(LANGUAGE_CONTAINER_CLASS);
			GenericButton addButton = new GenericButton("addButton", iwrb.getLocalizedString("add", "Add"));
			addLayerr.add(addButton);
			addLayerr.setStyleClass("button-add-layer");
			String imgSrc = iwb.getVirtualPathWithFileNameString("delete.png");
			StringBuilder onclickAction = new StringBuilder("UserAccessSettingsHelper.addLanguage('#").append(formId)
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(CoreConstants.DOT).append(LANGUAGE_SELECT_CLASS)
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(CoreConstants.DOT).append(LANGUAGE_CONTAINER_CLASS)
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(CitizenConstants.USER_EDIT_LANGUAGE_PARAMETER)
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(imgSrc)
				.append(CoreConstants.JS_STR_PARAM_END);
			addButton.setOnClick(onclickAction.toString());
			
			StringBuilder addLanguagesAction = 
				new StringBuilder("jQuery(document).ready(function(){\nUserAccessSettingsHelper.languages = [];");
			int i = 0;
			for(ICLanguage language : userLanguages){
				addLanguagesAction.append("UserAccessSettingsHelper.languages[").append(i++).append("] = '")
				.append(language.getPrimaryKey()).append(CoreConstants.JS_STR_INITIALIZATION_END);
			}
			addLanguagesAction.append("UserAccessSettingsHelper.initLanguages('#").append(languageSelection.getId())
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append("#").append(languageContainer.getId())
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(user.getId())
				.append("',UserAccessSettingsHelper.languages,'")
				.append(imgSrc)
				.append(CoreConstants.JS_STR_PARAM_END);
			addLanguagesAction.append("});");
			String actionString = PresentationUtil.getJavaScriptAction(addLanguagesAction.toString());
			layer.add(actionString);
		}catch(IDOLookupException e){
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting ICLanguageHome", e);
			layer.addText(getLanguageFailerror(iwrb));
		} catch (FinderException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting languages", e);
			layer.addText(getLanguageFailerror(iwrb));
		}
		return layer;
	}
	
	private String getLanguageFailerror(IWResourceBundle iwrb){
		return iwrb.getLocalizedString("failed_getting_languages", "Failed getting languages");
	}
	private void addactions(Layer layer,IWResourceBundle iwrb){
		StringBuilder actions = new StringBuilder("UserAccessSettingsHelper.initialize = function(){")
			.append("UserAccessSettingsHelper.REMOVE_MSG = '").append(iwrb.getLocalizedString("removed", "Removed"))
			.append(CoreConstants.JS_STR_INITIALIZATION_END)
			.append("UserAccessSettingsHelper.FAILED_MSG = '").append(iwrb.getLocalizedString("failed", "Failed"))
			.append(CoreConstants.JS_STR_INITIALIZATION_END)
			.append("UserAccessSettingsHelper.SINGLE_SING_ON_ID_SELECTOR = '[name = ").append(SINGLE_SING_ON_ID)
			.append("]';")
			.append("UserAccessSettingsHelper.SINGLE_SING_ON_SERVER_SELECTOR = '.").append(SINGLE_SING_ON_SERVER)
			.append(CoreConstants.JS_STR_INITIALIZATION_END)
			.append("UserAccessSettingsHelper.SINGLE_SING_ON_NAME_SELECTOR = '[name = ").append(SINGLE_SING_ON_NAME)
			.append("]';")
			.append("UserAccessSettingsHelper.SINGLE_SING_ON_PASSWORD_SELECTOR = '[name = ").append(SINGLE_SING_ON_PASSWORD)
			.append("]';")
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

			scripts.add(web2.getBundleUriToHumanizedMessagesScript());

		}else{
			Logger.getLogger("SimpleUserEditForm").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/UserAccessSettingsHelper.js"));
		scripts.add("/dwr/interface/CitizenServices.js");

		return scripts;
	}

	private List<String> getNeededStyles(IWContext iwc){
		List<String> styles = new ArrayList<String>();

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();

			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());

		}else{
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		
		IWBundle iwb = iwma.getBundle("is.idega.idegaweb.egov.application");
		styles.add(iwb.getVirtualPathWithFileNameString("style/application.css"));
		
		iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		styles.add(iwb.getVirtualPathWithFileNameString("style/citizen.css"));
		styles.add(iwb.getVirtualPathWithFileNameString("style/UserAccessSettings.css"));
		return styles;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}