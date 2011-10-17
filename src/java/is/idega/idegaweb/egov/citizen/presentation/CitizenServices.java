package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.block.family.business.FamilyLogic;
import is.idega.idegaweb.egov.citizen.CitizenConstants;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.CountryHome;
import com.idega.core.location.data.PostalCode;
import com.idega.core.location.data.PostalCodeHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table2;
import com.idega.presentation.ui.handlers.IWDatePickerHandler;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

@Service(CitizenServices.SERVICE)
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RemoteProxy(creator=SpringCreator.class, creatorParams={
	@Param(name="beanName", value=CitizenServices.SERVICE),
	@Param(name="javascript", value="CitizenServices")
}, name="CitizenServices")
public class CitizenServices extends DefaultSpringBean implements
		DWRAnnotationPersistance {
	public static final String SERVICE = "citizenServices";

	public static final int DATE_FORMAT = DateFormat.SHORT;
	public UserBusiness userBusiness = null;
	private UserApplicationEngine userApplicationEngine = null;
	private PostalCodeHome postalCodeHome = null;
	private CountryHome countryHome = null;
	private FamilyLogic familyLogic = null;

	private UserHome userHome = null;
	
	@RemoteMethod
	public String saveUser(Map <String, List<String>> parameters){
		IWContext iwc = CoreUtil.getIWContext();
		IWBundle bundle = iwc.getIWMainApplication().getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		String successMsg = iwrb.getLocalizedString("user_profile_saved", "User profile successfully saved");
		String failureMsg = iwrb.getLocalizedString("failed_saving_user_profile", "Failed to save user profile") + "!";
		User user = null;
		int id = -1;
		UserBusiness userBusiness = getUserBusiness();
		try{
			id = Integer.valueOf(parameters.get(CitizenConstants.USER_EDIT_USER_ID_PARAMETER).get(0));
			if(id == -1){
				user = userBusiness.getUserHome().create();
			}else{
				user = userBusiness.getUser(id);
			}
		}catch(RemoteException e){
			this.getLogger().log(Level.WARNING, "failed getting user " + id, e);
			return failureMsg;
		} catch (CreateException e) {
			this.getLogger().log(Level.WARNING, "failed creating user", e);
			return failureMsg;
		}
		// Setting user data
		String name = null;
		List<String> params = parameters.get(CitizenConstants.USER_EDIT_NAME_PARAMETER);
		if(params != null){
			name = params.get(0);
			user.setName(name);
		}
		String born = null;
		params = parameters.get(CitizenConstants.USER_EDIT_BORN_PARAMETER);
		if(params != null){
			born = params.get(0);
			Date bornDate = IWDatePickerHandler.getParsedDate(born, iwc.getCurrentLocale());
			user.setDateOfBirth(new java.sql.Date(bornDate.getTime()));
		}
		String resume = null;
		params = parameters.get(CitizenConstants.USER_EDIT_RESUME_PARAMETER);
		if(params != null){
			resume = params.get(0);
			user.setResume(resume);
		}
		String personalId = null;
		params = parameters.get(CitizenConstants.USER_EDIT_PERSONAL_ID_PARAMETER);
		if(params != null){
			personalId = params.get(0);
			user.setPersonalID(personalId);
		}
		try{
			user.store();
		}catch(Exception e){
			
		}
		
		// Setting user address data
		String report = saveAddress(parameters, iwrb, user.getId());
		if(!StringUtil.isEmpty(report)){
			successMsg += CoreConstants.NEWLINE + report;
		}
		// Setting user family relations data
		report = saveFamilyRelations(parameters, iwrb, iwc, user);
		if(!StringUtil.isEmpty(report)){
			successMsg += CoreConstants.NEWLINE + report;
		}
		
		return successMsg;
	}
	
	@RemoteMethod
	public String saveUserAccessSetings(Map<String, List<String>> parameters) {
		IWContext iwc = CoreUtil.getIWContext();
		IWBundle bundle = iwc.getIWMainApplication().getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		String successMsg = iwrb.getLocalizedString("access_settings_saved", "Access settings successfully saved");
		String failureMsg = iwrb.getLocalizedString("failed_saving_access_setting", "Failed saving access settings") + "!";
		if(!iwc.isLoggedOn()){
			return failureMsg + CoreConstants.NEWLINE + iwrb.getLocalizedString("not_logged_in", "Not logged in");
		}
		
		LoginTable loginTable = LoginDBHandler.getUserLogin(iwc.getCurrentUserId());
		String login = null;
		List<String> params = parameters.get(CitizenConstants.USER_EDIT_USERNAME_PARAMETER);
		if(params != null){
			login = params.get(0);
			loginTable.setUserLogin(login);
		}
		String password = null;
		params = parameters.get(CitizenConstants.USER_EDIT_PASSWORD_PARAMETER);
		if(params != null){
			password = params.get(0);
			try {
				LoginBusinessBean.getLoginBusinessBean(iwc).changeUserPassword(iwc.getCurrentUser(), password);
			} catch (Exception e) {
				successMsg += CoreConstants.NEWLINE + iwrb.getLocalizedString("failed_saving_password", "Failed saving password");
			}
		}
		return successMsg;
	}
	
	private String saveFamilyRelations(Map <String, List<String>> parameters,IWResourceBundle iwrb,IWContext iwc,User user){
		String report = CoreConstants.EMPTY;
		String failure = null;
		try {
			FamilyLogic familyLogic = getFamilyLogic(iwc);
			UserBusiness userBusiness = getUserBusiness();
			List<String> params = parameters.get(CitizenConstants.USER_EDIT_MARITAL_STATUS_PARAMETER);
//			String maritalStatus = null;
//			if(params != null){
//				maritalStatus = params.get(0);
//			}
			Collection<String> relations = getFamilyRelationTypes(iwc);
			String userId = user.getId();
			for(String type : relations){
				params = parameters.get(type);
				if(params == null){
					continue;
				}
				for(String relatedId : params){
					if(userId.equals(relatedId)){// User can not be related to himself
						continue;
					}
					User relatedUser = null;
					try{
						relatedUser = userBusiness.getUser(Integer.valueOf(relatedId));
					}catch(RemoteException e){
						failure = iwrb.getLocalizedString("failed_to_find_related_person", "Failed to find related person");
						continue;
					}
					try{
						familyLogic.setRelation(user, relatedUser, type);
					}catch(RemoteException e){
						report += iwrb.getLocalizedString("failed_adding_relation_with", "Failed adding relation with")
								+ relatedUser.getName();
						continue;
					}
				}
			}
		} catch (IBOLookupException e) {
			this.getLogger().log(Level.WARNING, "failed getting family Logic", e);
			return  iwrb.getLocalizedString("failed_saving_family_relations_data", "Failed saving family relations data");
		} catch (Exception e) {
			this.getLogger().log(Level.WARNING, "Failed saving family relations data", e);
			return  iwrb.getLocalizedString("failed_saving_family_relations_data", "Failed saving family relations data");
		}
		if(failure != null){
			report += CoreConstants.NEWLINE + failure;
		}
		return report;
	}
	
	private String saveAddress(Map<String, List<String>> parameters, IWResourceBundle iwrb, String userId){
		String report = CoreConstants.EMPTY;
		String streetNameAndNumber = null;
		List<String> params = parameters.get(CitizenConstants.USER_EDIT_STREET_AND_NUMBER_PARAMETER);
		if(params != null){
			streetNameAndNumber = params.get(0);
		}
		String city = null;
		params = parameters.get(CitizenConstants.USER_EDIT_CITY_PARAMETER);
		if(params != null){
			city = params.get(0);
		}
		String country = null;
		params = parameters.get(CitizenConstants.USER_EDIT_COUNTRY_PARAMETER);
		if(params != null){
			country = params.get(0);
			Locale locale = ICLocaleBusiness.getLocaleFromLocaleString(country);
			country = locale.getDisplayCountry(Locale.ENGLISH);
		}
		Integer postalCodeId = null;
		params = parameters.get(CitizenConstants.USER_EDIT_POSTAL_CODE_PARAMETER);
		if(params != null){
			String code = params.get(0);
			PostalCode postalCode = getPostalCode(code, country);
			if(postalCode == null){
				report += iwrb.getLocalizedString("failed_saving_postal_code", "Failed saving postal code");
			}else{
				postalCodeId = (Integer)postalCode.getPrimaryKey();
			}
		}
		String postalBox = null;
		params = parameters.get(CitizenConstants.USER_EDIT_POSTAL_BOX_PARAMETER);
		if (!ListUtil.isEmpty(params))
			postalBox = params.get(0);
		try {
			userBusiness.updateUsersMainAddressOrCreateIfDoesNotExist(Integer.valueOf(userId), streetNameAndNumber, postalCodeId, country, city, null, postalBox);
		} catch (NumberFormatException e) {
			this.getLogger().log(Level.WARNING, "failed getting user id " + userId, e);
			return  iwrb.getLocalizedString("failed_saving_address_data", "Failed saving address data");
		} catch (RemoteException e) {
			this.getLogger().log(Level.WARNING, "failed saving user", e);
			return iwrb.getLocalizedString("failed_saving_address_data", "Failed saving address data");
		} catch (CreateException e) {
			this.getLogger().log(Level.WARNING, "failed creating user", e);
			return iwrb.getLocalizedString("failed_saving_address_data", "Failed saving address data");
		}
		return report;
	}
	
	private PostalCode getPostalCode(String postalCode, String countryName){
		try{
			PostalCodeHome postalcCodeHome = getPostalCodeHome();
			PostalCode code = null;
			try{
				code = postalcCodeHome.findByPostalCode(postalCode);
			}catch (FinderException e) {
				code = postalcCodeHome.create();
			}
			code.setPostalCode(postalCode);
			Country country = null;
			try{
				country = getCountryHome().findByCountryName(countryName);
				code.setCountry(country);
			}catch (FinderException e) {
				this.getLogger().log(Level.WARNING, "failed getting country by name" + countryName, e);
			}
			code.store();
			
			return code;
		}catch (IDOLookupException e) {
			this.getLogger().log(Level.WARNING, "failed getting PostalCodeHome", e);
		} catch (CreateException e) {
			this.getLogger().log(Level.WARNING, "failed creating PostalCode", e);
		} 
		return null;
	}
	
	public UserBusiness getUserBusiness() {
		if(userBusiness == null){
			userBusiness = this.getServiceInstance(UserBusiness.class);
		}
		return userBusiness;
	}

	public UserApplicationEngine getUserApplicationEngine() {
		if(userApplicationEngine == null){
			userApplicationEngine = ELUtil.getInstance().getBean(UserApplicationEngine.class);
		}
		return userApplicationEngine;
	}
	public PostalCodeHome getPostalCodeHome() throws IDOLookupException{
		if(postalCodeHome == null){
			postalCodeHome = (PostalCodeHome) IDOLookup.getHome(PostalCode.class);
		}
		return postalCodeHome;
	}
	public CountryHome getCountryHome() throws IDOLookupException{
		if(countryHome == null){
			countryHome = (CountryHome) IDOLookup.getHome(Country.class);
		}
		return countryHome;
	}
	public FamilyLogic getFamilyLogic(IWContext iwc) throws IBOLookupException{
		if(familyLogic == null){
			familyLogic = (FamilyLogic) IBOLookup.getServiceInstance(iwc, FamilyLogic.class);
		}
		return familyLogic;
	}
	
	public Map <String ,Collection<User>> getFamilyMembers(IWContext iwc, User user, Collection<String> relationTypes) {
		Map<String,Collection<User>> members = new HashMap<String,Collection<User>>();
		FamilyLogic familyLogic = null;
		try {
			familyLogic = getFamilyLogic(iwc);
			for(String type : relationTypes){
				Collection<User> related = null;
				try {
					related = familyLogic.getRelatedUsers(user, type);
					members.put(type, related);
				} catch (Exception e) {
					related = Collections.emptyList();
					members.put(type, related);
					continue;
				} 
			}
		} catch (IBOLookupException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting family data" , e);
		}
		return members;
//		try {
//			Collection<User> related = null;
//			try{
//				related = familyLogic.getChildrenFor(user);
//			}catch (FinderException e) {
//				related = Collections.emptyList();
//			}
//			members.put(familyLogic.getChildRelationType(), related);
//			try{
//				related = familyLogic.getParentsFor(user);
//			}catch (FinderException e) {
//				related = Collections.emptyList();
//			}
//			members.put(familyLogic.getParentRelationType(), related);
//			try{
//				related = familyLogic.getCustodiansFor(user);
//			}catch (FinderException e) {
//				related = Collections.emptyList();
//			}
//			members.put(familyLogic.getCustodianRelationType(), related);
//			try{
//				related = familyLogic.getSiblingsFor(user);
//			}catch (FinderException e) {
//				related = Collections.emptyList();
//			}
//			members.put(familyLogic.getSiblingRelationType(), related);
//			try{
//				User relatedUser = familyLogic.getSpouseFor(user);
//				related = new ArrayList<User>(1);
//				related.add(relatedUser);
//			}catch (FinderException e) {
//				related = Collections.emptyList();
//			}
//			members.put(familyLogic.getSpouseRelationType(), related);
//			try{
//				User relatedUser = familyLogic.getCohabitantFor(user);
//				related = new ArrayList(1);
//				related.add(relatedUser);
//			}catch (FinderException e) {
//				related = Collections.emptyList();
//			}
//			members.put(familyLogic.getCohabitantRelationType(), related);
//			
//		} catch (IBOLookupException e) {
//			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting family data" , e);
//		} catch (RemoteException e) {
//			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting family data" , e);
//		}
//		return members;
	}
	
	public List<String> getFamilyRelationTypes(IWContext iwc) throws Exception{
		List<String> familyRelationTypes = new ArrayList<String>();
		FamilyLogic familyLogic = getFamilyLogic(iwc);
		familyRelationTypes.add(familyLogic.getChildRelationType());
		familyRelationTypes.add(familyLogic.getParentRelationType());
		familyRelationTypes.add(familyLogic.getCustodianRelationType());
		familyRelationTypes.add(familyLogic.getSiblingRelationType());
		familyRelationTypes.add(familyLogic.getSpouseRelationType());
		familyRelationTypes.add(familyLogic.getCohabitantRelationType());
		return familyRelationTypes;
	}
	
	@RemoteMethod
	public Collection<String> getAutocompletedUsers(String request,int maxAmount,String relationType){
		request = request.toLowerCase();
		Collection <User> requestedUsers = (this.getUserHome().ejbAutocompleteRequest(request, -1, maxAmount, 0));
		UserApplicationEngine userApplicationEngine = this.getUserApplicationEngine();
		
		IWContext iwc = CoreUtil.getIWContext();
		IWBundle bundle = iwc.getIWMainApplication().getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		List <String>tables = new ArrayList<String>();
		FamilyLogic fl = null;
		try {
			fl = this.getFamilyLogic(iwc);
		} catch (IBOLookupException e) {
			this.getLogger().log(Level.WARNING, "failed getting family logic", e);
			return tables;
		}
		
		for(User user : requestedUsers){
			UserDataBean data =  userApplicationEngine.getUserInfo(user);
			Table2 table = CitizenProfile.getUserInfoView(data,relationType,fl,iwrb,iwb,true);
			String html = BuilderLogic.getInstance().getRenderedComponent(
					table, null).getHtml();
			tables.add(html);
			tables.add(request);
		}
		return tables;
	}
	
	public UserHome getUserHome() {
		if (this.userHome  == null) {
			try {
				this.userHome = (UserHome) IDOLookup.getHome(User.class);
			} catch (RemoteException rme) {
				this.getLogger().log(Level.WARNING, "Failed getting UserHome", rme);
			}
		}
		return this.userHome;
	}
	
	@RemoteMethod
	public Boolean removeRelation(String userId,String relatedId,String relationType){
		if(userId == null || relatedId == null || relationType == null){
			return Boolean.FALSE;
		}
		try{
			IWContext iwc = CoreUtil.getIWContext();
			FamilyLogic familyLogic = getFamilyLogic(iwc);
			UserBusiness userBusiness = getUserBusiness();
			User user = userBusiness.getUser(Integer.valueOf(userId));
			User relatedUser = userBusiness.getUser(Integer.valueOf(relatedId));
			familyLogic.removeRelation(user, relatedUser, relationType);
		}catch (IBOLookupException e) {
			this.getLogger().log(Level.WARNING, "failed getting FamilyLogic", e);
			return Boolean.FALSE;
		} catch (RemoteException e) {
			this.getLogger().log(Level.WARNING, "failed removing " + relationType + 
					" relation from user" + userId + "  with " + relatedId, e);
			return Boolean.FALSE;
		} catch (RemoveException e) {
			this.getLogger().log(Level.WARNING, "failed removing " + relationType + 
					" relation from user" + userId + "  with " + relatedId, e);
			return Boolean.FALSE;
		}catch(Exception e){
			this.getLogger().log(Level.WARNING, "failed removing " + relationType + 
					" relation from user" + userId + "  with " + relatedId, e);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
}