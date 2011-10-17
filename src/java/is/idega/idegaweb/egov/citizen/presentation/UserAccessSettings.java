package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.idegaweb.egov.citizen.CitizenConstants;
import is.idega.idegaweb.egov.citizen.business.CitizenServices;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.business.LoginContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.text.Strong;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.TextInput;
import com.idega.user.bean.UserDataBean;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class UserAccessSettings extends Block {
	
	private static final String REMOVE_ITEM_CLASS = "remove-item-class";
	private static final String RELATED_ITEM_CLASS = "simple-user-edit-related-item-class";
	private static final String ID_CONTANER_CLASS = "id-container-class";
	private static final String RELATION_TYPE_CONTAINER_CLASS = "relation-type-container";
	
	private String formId = null;
	private String relationInputId = null;
	private String relationSelectId = null;
	private String relationListsId = null;
	
	private String userId = null;
	
	@Autowired
	private CitizenServices citizenServices;
	
	private Boolean needFiles = Boolean.TRUE;
	
	IWResourceBundle iwrb = null;
	
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
		iwrb = bundle.getResourceBundle(iwc);
		
		User user = getUser(iwc);
		if(user == null){
			Layer layer = new Layer();
			this.add(layer);
			layer.addText(iwrb.getLocalizedString("no_user_specified", "No user specified"));
			return;
		}
		this.add(this.getUserEditForm(user));
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
				user = citizenServices.getUserBusiness().getUser(Integer.valueOf(userId));
			}else if(iwc.isLoggedOn()){
				user =iwc.getCurrentUser();
			}
		}catch(RemoteException e){
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting user with id " + userId , e);
		}
		return user;
	}
	
	private Form getUserEditForm(User user){
		UserDataBean userData = null;
		boolean isData = false;
		String id = String.valueOf(-1);
		String username = CoreConstants.EMPTY;
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
		}
		String name = CoreConstants.EMPTY;
		if(isData){
			name = userData.getName();
			if(name == null){
				name = CoreConstants.EMPTY;
			}
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
		cellInfo = new Strong();
		cell = row.createCell();
		cell.add(cellInfo);
		cellInfo.add(name);
		
		// Username
		row = table.createRow();
		cellInfo = new Strong();
		row.createCell().add(cellInfo);
		cellInfo.addText(iwrb.getLocalizedString("login", "Login"));
		TextInput input = new TextInput(CitizenConstants.USER_EDIT_USERNAME_PARAMETER,username);
		row.createCell().add(input);
		
		// Password
		row = table.createRow();
		cellInfo = new Strong();
		row.createCell().add(cellInfo);
		cellInfo.addText(iwrb.getLocalizedString("password", "Password"));
		PasswordInput passwordInput = new PasswordInput(CitizenConstants.USER_EDIT_PASSWORD_PARAMETER);
		row.createCell().add(passwordInput);
		
		// Submit
		GenericButton buttonSubmit = new GenericButton("buttonSubmit", iwrb.getLocalizedString("save", "Save"));
		form.add(buttonSubmit);
		buttonSubmit.setOnClick("UserAccessSettingsHelper.saveAccessSettings('#" + formId + CoreConstants.JS_STR_PARAM_END);
		
		return form;
	}
	
	private void addactions(Layer layer){
		StringBuilder actions = new StringBuilder("UserAccessSettingsHelper.initialize = function(){")
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
				scripts.add(web2.getBundleURIWithinScriptsFolder(new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
				.append("/jquery.autoresizev-textarea.js").toString()));
			}catch(RemoteException e){
				Logger.getLogger("SimpleUserEditForm").log(Level.WARNING,CoreConstants.EMPTY,e);
			}
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

			styles.add(web2.getBundleURIToFancyBoxStyleFile());

			styles.add(jQuery.getBundleURIToJQueryUILib("1.8.14","css/ui-lightness/jquery-ui-1.8.14.custom.css"));

			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());

			styles.addAll(web2.getBundleURIsToTageditStyleFiles());


		}else{
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
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