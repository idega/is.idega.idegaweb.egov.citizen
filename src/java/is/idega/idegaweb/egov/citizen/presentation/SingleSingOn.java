package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.idegaweb.egov.citizen.data.CitizenRemoteServices;
import is.idega.idegaweb.egov.citizen.data.CitizenRemoteServicesHome;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.TextInput;
import com.idega.util.CoreConstants;
import com.idega.util.StringUtil;

public class SingleSingOn extends Block {
	
	private String loginIdName = null; 
	private String addressClass = CoreConstants.EMPTY;
	private String userNameName = CoreConstants.EMPTY;
	private String passwordName = CoreConstants.EMPTY;
	
	private String loginId = "-1"; 
	private String name = CoreConstants.EMPTY;
	private String address = null;
	
	public SingleSingOn(){
		
	}
	
	public SingleSingOn(String addressClass, String userNameName, String passwordName, String loginIdName){
		this.addressClass = addressClass;
		this.userNameName = userNameName;
		this.passwordName = passwordName;
		this.loginIdName = loginIdName;
	}
	
	@Override
	public void main(IWContext iwc) throws Exception {
		IWBundle bundle = getBundle(iwc);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		Layer main = new Layer();
		this.add(main);
		main.setStyleClass("dwr-form");
		
		// Server
		main.add(getDropdownMenuForServices(iwrb));
		//Login id
		Layer layer = new Layer();
		main.add(layer);
		layer.setStyleClass("formItem");
		HiddenInput hidden = new HiddenInput(loginIdName,loginId);
		main.add(hidden);
		
		// Name
		layer = new Layer();
		main.add(layer);
		layer.setStyleClass("formItem");
		TextInput input = new TextInput(userNameName,name);
		Label cellInfo = new Label(input);
		layer.add(cellInfo);
		layer.add(input);
		cellInfo.addText(iwrb.getLocalizedString("name", "Name"));
		
		// Password
		layer = new Layer();
		main.add(layer);
		layer.setStyleClass("formItem");
		PasswordInput passwordInput = new PasswordInput(passwordName);
		cellInfo = new Label(passwordInput);
		layer.add(cellInfo);
		layer.add(passwordInput);
		cellInfo.addText(iwrb.getLocalizedString("password", "Password"));
		
		// Remove
		layer = new Layer();
		main.add(layer);
		layer.setStyleClass("formItem");
		GenericButton add = new GenericButton("remove", iwrb.getLocalizedString("remove", "Remove"));
		layer.add(add);
		add.setOnClick("UserAccessSettingsHelper.removeSingleSingOn('#"+main.getId()+ CoreConstants.JS_STR_PARAM_SEPARATOR
				+ loginId+CoreConstants.JS_STR_PARAM_END);
	}
	
	private Layer getDropdownMenuForServices(IWResourceBundle iwrb){
		Layer layer = new Layer();
		layer.setStyleClass("formItem");
		DropdownMenu dropdown = new DropdownMenu();
		dropdown.setName(dropdown.getId());
		dropdown.setStyleClass(addressClass);
		layer.add(dropdown);
		CitizenRemoteServicesHome citizenRemoteServicesHome = null;
		try {
			citizenRemoteServicesHome = (CitizenRemoteServicesHome) IDOLookup.getHome(CitizenRemoteServices.class);
		} catch (IDOLookupException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "failed adding default remote services", e);
			layer = new Layer();
			layer.addText(iwrb.getLocalizedString("failed_getting_remote_services", "Failed getting remote services"));
			return layer;
		}
		Collection<CitizenRemoteServices> services = citizenRemoteServicesHome.getRemoteServices(0);
		dropdown.addMenuElement(CoreConstants.EMPTY,iwrb.getLocalizedString("choose_service", "Choose service"));
		for(CitizenRemoteServices service : services){
			dropdown.addMenuElement(service.getPrimaryKey().toString(),service.getServerName());
		}
		if(!StringUtil.isEmpty(address)){
			dropdown.setSelectedElement(address);
		}
		return layer;
	}

	public String getAddressClass() {
		return addressClass;
	}

	public void setAddressClass(String addressClass) {
		this.addressClass = addressClass;
	}

	public String getUserNameName() {
		return userNameName;
	}

	public void setUserNameName(String userNameName) {
		this.userNameName = userNameName;
	}

	public String getPasswordName() {
		return passwordName;
	}

	public void setPasswordName(String passwordName) {
		this.passwordName = passwordName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLoginIdName() {
		return loginIdName;
	}

	public void setLoginIdName(String loginIdName) {
		this.loginIdName = loginIdName;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

}
