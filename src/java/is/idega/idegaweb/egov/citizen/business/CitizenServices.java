package is.idega.idegaweb.egov.citizen.business;

import is.idega.block.family.business.FamilyLogic;
import is.idega.idegaweb.egov.citizen.CitizenConstants;
import is.idega.idegaweb.egov.citizen.bean.LoginDataBean;
import is.idega.idegaweb.egov.citizen.data.LoginData;
import is.idega.idegaweb.egov.citizen.data.LoginDataHome;
import is.idega.idegaweb.egov.citizen.presentation.CitizenProfile;
import is.idega.idegaweb.egov.citizen.presentation.UserAccessSettings;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.cal.business.CalendarManagementService;
import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.EmailHome;
import com.idega.core.contact.data.Phone;
import com.idega.core.contact.data.PhoneHome;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.core.localisation.data.ICLanguage;
import com.idega.core.localisation.data.ICLanguageHome;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.CountryHome;
import com.idega.core.location.data.PostalCode;
import com.idega.core.location.data.PostalCodeHome;
import com.idega.data.IDOAddRelationshipException;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.data.IDORelationshipException;
import com.idega.data.IDORemoveRelationshipException;
import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.ui.handlers.IWDatePickerHandler;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.GroupBusiness;
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
	private LoginDataHome loginDataHome = null;
	private FamilyLogic familyLogic = null;
	
	@Autowired(required = false)
	private CalendarManagementService calendarManagementService;

	private UserHome userHome = null;
	private ICLanguageHome iCLanguageHome = null;
	
	private Pattern pattern;
	private static final String EMAIL_PATTERN = 
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	public CitizenServices(){
		pattern = Pattern.compile(EMAIL_PATTERN);
	}
	
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
		if(!ListUtil.isEmpty(params)){
			name = params.get(0);
			user.setName(name);
		}
		String born = null;
		params = parameters.get(CitizenConstants.USER_EDIT_BORN_PARAMETER);
		if (!ListUtil.isEmpty(params)){
			born = params.get(0);
			Date bornDate = IWDatePickerHandler.getParsedDate(born, iwc.getCurrentLocale());
			if (bornDate != null)
				user.setDateOfBirth(new java.sql.Date(bornDate.getTime()));
		}
		String resume = null;
		params = parameters.get(CitizenConstants.USER_EDIT_RESUME_PARAMETER);
		if(!ListUtil.isEmpty(params)){
			resume = params.get(0);
			user.setResume(resume);
		}
		String personalId = null;
		params = parameters.get(CitizenConstants.USER_EDIT_PERSONAL_ID_PARAMETER);
		if(!ListUtil.isEmpty(params)){
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
	public String getSingleSingOnLayer(){
		Block layer = UserAccessSettings.getSingleSingOnLayer();
		String html = BuilderLogic.getInstance().getRenderedComponent(
				layer, null).getHtml();
		return html;
	}
	
	@RemoteMethod
	public String getSingleSingOnLayers(Integer userId){
		if(userId == null){
			return CoreConstants.EMPTY;
		}
		UserBusiness userBusiness = getUserBusiness();
		Collection<Block> layers = null;
		try{
			User user = userBusiness.getUser(userId);
			layers = UserAccessSettings.getSingleSingOnLayers(user, getLoginDataHome());
		}catch(Exception e){
			getLogger().log(Level.WARNING, "Failed getting single sing on layers", e);
			return CoreConstants.EMPTY;
		}
		if(ListUtil.isEmpty(layers)){
			return CoreConstants.EMPTY;
		}
		Layer layer = new Layer();
		for(Block login : layers){
			layer.add(login);
		}
		String html = BuilderLogic.getInstance().getRenderedComponent(
				layer, null).getHtml();
		return html;
	}
	
	@RemoteMethod
	public String saveUserAccessSetings(Map<String, List<String>> parameters, ArrayList<LoginDataBean> singleSingOn) {
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
		if(!ListUtil.isEmpty(params)){
			login = params.get(0);
			if(!StringUtil.isEmpty(login)){
				loginTable.setUserLogin(login);
			}
		}
		User user = iwc.getCurrentUser();
		String password = null;
		params = parameters.get(CitizenConstants.USER_EDIT_PASSWORD_PARAMETER);
		if(!ListUtil.isEmpty(params)){
			password = params.get(0);
			if(!StringUtil.isEmpty(password)){
				try {
					LoginBusinessBean.getLoginBusinessBean(iwc).changeUserPassword(user, password);
				} catch (Exception e) {
					successMsg += CoreConstants.NEWLINE + iwrb.getLocalizedString("failed_saving_password", "Failed saving password");
				}
			}
		}
		params = parameters.get(CitizenConstants.USER_EDIT_LANGUAGE_PARAMETER);
		if(!ListUtil.isEmpty(params)){
			try {
				ICLanguageHome iCLanguageHome = this.getICLanguageHome();
				Collection<Integer> ids = new ArrayList<Integer>(params.size());
				for(String languageId : params){
					ids.add(Integer.valueOf(languageId));
				}
				Collection <ICLanguage>languages = iCLanguageHome.getEntityCollectionForPrimaryKeys(ids);
				for(ICLanguage language : languages){
					user.addLanguage(language);
				}
			} catch (IDOLookupException e) {
				getLogger().log(Level.WARNING, "", e);
			} catch (FinderException e) {
				getLogger().log(Level.WARNING, "", e);
			} catch (IDOAddRelationshipException e) {
				getLogger().log(Level.WARNING, "", e);
			}catch (Exception e) {
				getLogger().log(Level.WARNING, "", e);
			}
		}
		try {
			storeSingleSingOn(singleSingOn, user);
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed saving single sing on", e);
			successMsg += CoreConstants.NEWLINE + iwrb.getLocalizedString("failed_saving_single_sing_on", "Failed saving single sing on");
		}	
		return successMsg;
	}
	

	private void storeSingleSingOn(ArrayList<LoginDataBean> singleSingOn,User user) throws IDOLookupException{
		LoginDataHome loginDataHome = getLoginDataHome();
		for(LoginDataBean data : singleSingOn){
			LoginData login = null;
			String id = data.getLoginId();
			if(StringUtil.isEmpty(id) || id.equals("-1")){
				try {
					login = (LoginData)loginDataHome.createIDO();
				} catch (CreateException e) {
					this.getLogger().log(Level.WARNING, "Failed creating LoginData", e);
					continue;
				}
			}else{
				Collection <Integer> ids = new ArrayList<Integer>(1);
				try{
					ids.add(Integer.valueOf(id));
					Collection <LoginData> logins = loginDataHome.getEntityCollectionForPrimaryKeys(ids);
					if(ListUtil.isEmpty(logins)){
						continue;
					}
					login = logins.iterator().next();
				}catch(Exception e){
					this.getLogger().log(Level.WARNING, "Failed getting LoginData by id " , e);
					continue;
				}
			}
			String input = data.getPassword();
			if(!StringUtil.isEmpty(input)){
				login.setPassword(input);
			}
			input = data.getAddress();
			if(StringUtil.isEmpty(input)){
				continue;
			}
			Integer serviceId = null;
			try{
				serviceId = Integer.valueOf(input);
			}catch(NumberFormatException e){
				getLogger().log(Level.WARNING, "Not an integer id of remote service");
				continue;
			}
			login.setService(serviceId);
			
			input = user.getPersonalID();
			if(StringUtil.isEmpty(input)){
				continue;
			}
			login.setPersonalId(input);
			input = data.getUserName();
			if(StringUtil.isEmpty(input)){
				continue;
			}
			login.setUserName(input);
			login.store();
		}
	}
	
	@RemoteMethod
	public Boolean removeSingleSingOn(Integer loginId){
		LoginDataHome loginDataHome = null;
		try {
			loginDataHome = getLoginDataHome();
		} catch (IDOLookupException e) {
			getLogger().log(Level.WARNING, "failed getting LoginDataHome", e);
			return Boolean.FALSE;
		}
		Collection<Integer> ids = new ArrayList<Integer>(1);
		ids.add(loginId);
		Collection<LoginData> dataList = null;
		try {
			dataList = loginDataHome.getEntityCollectionForPrimaryKeys(ids);
		} catch (FinderException e) {
			return Boolean.FALSE;
		}
		if(ListUtil.isEmpty(ids)){
			return Boolean.FALSE;
		}
		LoginData loginData = dataList.iterator().next();
		try {
			loginData.remove();
		} catch (EJBException e) {
			getLogger().log(Level.WARNING, "Failed removing LoginData " + loginId, e);
			return Boolean.FALSE;
		} catch (RemoveException e) {
			getLogger().log(Level.WARNING, "Failed removing LoginData " + loginId);
			return Boolean.TRUE;
		}
		return Boolean.TRUE;
	}
	
	private String saveFamilyRelations(
			Map <String, List<String>> parameters,
			IWResourceBundle iwrb,
			IWContext iwc,
			User user) {
		String report = CoreConstants.EMPTY;
		String failure = null;
		try {
			FamilyLogic familyLogic = getFamilyLogic(iwc);
			UserBusiness userBusiness = getUserBusiness();
			List<String> params = parameters.get(CitizenConstants.USER_EDIT_MARITAL_STATUS_PARAMETER);
//			String maritalStatus = null;
//			if(!ListUtil.isEmpty(params)){
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
		if(!ListUtil.isEmpty(params)){
			streetNameAndNumber = params.get(0);
		}
		String city = null;
		params = parameters.get(CitizenConstants.USER_EDIT_CITY_PARAMETER);
		if(!ListUtil.isEmpty(params)){
			city = params.get(0);
		}
		String country = null;
		params = parameters.get(CitizenConstants.USER_EDIT_COUNTRY_PARAMETER);
		if(!ListUtil.isEmpty(params)){
			country = params.get(0);
			Locale locale = ICLocaleBusiness.getLocaleFromLocaleString(country);
			country = locale.getDisplayCountry(Locale.ENGLISH);
		}
		Integer postalCodeId = null;
		params = parameters.get(CitizenConstants.USER_EDIT_POSTAL_CODE_PARAMETER);
		if(!ListUtil.isEmpty(params)){
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
	
	/**
	 * This remote method returns a map of search status and users by request.
	 * @param request the request by which users will be searched
	 * @param maxAmount the max amount of users to return
	 * @param relationType the relationship type which will be specified in user table
	 * @return returns a Map that contains <br/>
	 * "status"`:		status of request (OK if ok, other if errors)
	 * "message":		message about search (For example error).
	 * "content":		collection of user search label (for showing data) followed by value.
	 */
	@RemoteMethod
	public Map<String,Collection<String>> getAutocompletedUsers(String request,int maxAmount,String relationType){
		request = request.toLowerCase();
		Collection <User> requestedUsers = (this.getUserHome().ejbAutocompleteRequest(request, -1, maxAmount, 0));
		UserApplicationEngine userApplicationEngine = this.getUserApplicationEngine();
		
		IWContext iwc = CoreUtil.getIWContext();
		IWBundle bundle = iwc.getIWMainApplication().getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		List <String>tables = new ArrayList<String>();
		Map<String,Collection<String>> response = new HashMap<String,Collection<String>>(); 
		FamilyLogic fl = null;
		if(relationType.equals("-1")){
			response.put("status", Arrays.asList("bad_request"));
			String message = iwrb.getLocalizedString("relationship_type_is_not_specified", "Relationship type is not specified");
			response.put("message", Arrays.asList(message));
			return response;
		}
		try {
			fl = this.getFamilyLogic(iwc);
		} catch (Exception e) {
			this.getLogger().log(Level.WARNING, "failed getting family logic", e);
			response.put("status", Arrays.asList("server_error"));
			String message = iwrb.getLocalizedString("server_error", "Server error");
			response.put("message", Arrays.asList(message));
			return response;
		}
		for(User user : requestedUsers){
			UserDataBean data =  userApplicationEngine.getUserInfo(user);
			Table2 table = CitizenProfile.getUserInfoView(data,relationType,fl,iwrb,iwb,true);
			String html = BuilderLogic.getInstance().getRenderedComponent(
					table, null).getHtml();
			tables.add(html);
			tables.add(request);
		}
		response.put("status", Arrays.asList("OK"));
		response.put("content", tables);
		return response;
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
	public ICLanguageHome getICLanguageHome() throws IDOLookupException {
		if (this.iCLanguageHome  == null) {
			this.iCLanguageHome = (ICLanguageHome) IDOLookup.getHome(ICLanguage.class);
		}
		return this.iCLanguageHome;
	}
	public LoginDataHome getLoginDataHome() throws IDOLookupException {
		if (this.loginDataHome  == null) {
			this.loginDataHome = (LoginDataHome) IDOLookup.getHome(LoginData.class);
		}
		return this.loginDataHome;
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
					" relation from user " + userId + "  with " + relatedId, e);
			return Boolean.FALSE;
		} catch (RemoveException e) {
			this.getLogger().log(Level.WARNING, "failed removing " + relationType + 
					" relation from user " + userId + "  with " + relatedId, e);
			return Boolean.FALSE;
		}catch(Exception e){
			this.getLogger().log(Level.WARNING, "failed removing " + relationType + 
					" relation from user " + userId + "  with " + relatedId, e);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	@RemoteMethod
	public Boolean removeLanguage(String userId,String languageId){
		if(userId == null || languageId == null){
			return Boolean.FALSE;
		}
		try{
			UserBusiness userBusiness = getUserBusiness();
			User user = userBusiness.getUser(Integer.valueOf(userId));
			ICLanguageHome icLanguageHome = getICLanguageHome();
			ICLanguage language = icLanguageHome.findByPrimaryKey(Integer.valueOf(languageId));
			try{
				user.removeLanguage(language);
			}catch(IDORemoveRelationshipException e){
				
			}
		}catch(Exception e){
			this.getLogger().log(Level.WARNING, "failed removing " + languageId + 
					" language from user " + userId, e);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	@RemoteMethod
	public Map <String,String> getUserLanguages(Integer userId){
		if(userId == null || userId.equals(-1)){
			return Collections.emptyMap();
		}
		UserBusiness userBusiness = getUserBusiness();
		try {
			User user = userBusiness.getUser(userId);
			if(user == null){
				return Collections.emptyMap();
			}
			Collection<ICLanguage> userLanguages = user.getLanguages();
			if(ListUtil.isEmpty(userLanguages)){
				return Collections.emptyMap();
			}
			Map <String,String> languages = new HashMap<String,String>();
			for(ICLanguage language : userLanguages){
				languages.put(language.getPrimaryKey().toString(), language.getName());
			}
			return languages;
		} catch (IDORelationshipException e) {
			this.getLogger().log(Level.WARNING, "Failed getting languages from user", e);
		} catch (RemoteException e) {
			this.getLogger().log(Level.WARNING, "Failed getting user with id" + userId, e);
		}catch (Exception e) {
			this.getLogger().log(Level.WARNING, "Something went wrong on getting user languages", e);
		}
		return Collections.emptyMap();
	}
	
	
	@RemoteMethod
	public Map <String,String> removeCurrentUserEmail(String id){
		HashMap<String, String> response = new HashMap<String, String>();
		if(StringUtil.isEmpty(id)){
			response.put("status", "OK");
			return response;
		}
		IWContext iwc = CoreUtil.getIWContext();
		IWBundle bundle = iwc.getIWMainApplication().getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		if(!iwc.isLoggedOn()){
			response.put("status","Unauthorized");
			response.put("message", iwrb.getLocalizedString("permission_denied", "Permission denied"));
			return response;
		}
		User user = iwc.getCurrentUser();
		try {
			removeUserEmail(user, id);
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "Erorr on removing user email", e);
			response.put("status","Internal Error");
			response.put("message", iwrb.getLocalizedString("error_while_removing_email", "Error while removing email"));
		}
		response.put("status", "OK");
		return response;
	}

	@RemoteMethod
	public Map <String,String> removeCurrentUserPhone(String id){
		HashMap<String, String> response = new HashMap<String, String>();
		if(StringUtil.isEmpty(id)){
			response.put("status", "OK");
			return response;
		}
		IWContext iwc = CoreUtil.getIWContext();
		IWBundle bundle = iwc.getIWMainApplication().getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		if(!iwc.isLoggedOn()){
			response.put("status","Unauthorized");
			response.put("message", iwrb.getLocalizedString("permission_denied", "Permission denied"));
			return response;
		}
		User user = iwc.getCurrentUser();
		try {
			removeUserPhone(user, id);
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "Erorr on removing user email", e);
			response.put("status","Internal Error");
			response.put("message", iwrb.getLocalizedString("error_while_removing_email", "Error while removing email"));
		}
		response.put("status", "OK");
		return response;
	}
	
	private boolean isValidEmail(String email){
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	
	private void removeUserEmails(Collection<Email> userEmails,Collection<?> emailsToRemoveIds) throws Exception{
		if(ListUtil.isEmpty(userEmails) || ListUtil.isEmpty(emailsToRemoveIds)){
			return;
		}
		for(Object id : emailsToRemoveIds){
			removeUserEmail(userEmails, id);
		}
	}
	
	private void removeUserEmail(User user, Object emailToRemoveId) throws Exception{
		Collection<Email> emails = user.getEmails();
		removeUserEmail(emails,emailToRemoveId);
	}
	
	private void removeUserEmail(Collection<Email> userEmails,Object emailToRemoveId) throws Exception{
		String id = String.valueOf(emailToRemoveId);
		for(Email email : userEmails){
			String primaryKey = String.valueOf(email.getPrimaryKey());
			if(primaryKey.equals(id)){
				email.remove();
				userEmails.remove(email);
				break;
			}
		}
	}
	
	private void removeUserPhones(Collection<Phone> userPhones,Collection<?> phonesToRemoveIds) throws Exception{
		if(ListUtil.isEmpty(userPhones) || ListUtil.isEmpty(phonesToRemoveIds)){
			return;
		}
		for(Object id : phonesToRemoveIds){
			removeUserPhone(userPhones, id);
		}
	}
	
	private void removeUserPhone(User user, Object phoneToRemoveId) throws Exception{
		Collection<Phone> userphPhones = user.getPhones();
		removeUserPhone(userphPhones, phoneToRemoveId);
	}
	private void removeUserPhone(Collection<Phone> userPhones,Object phoneToRemoveId) throws Exception{
		String id = String.valueOf(phoneToRemoveId);
		for(Phone phone : userPhones){
			String primaryKey = String.valueOf(phone.getPrimaryKey());
			if(primaryKey.equals(id)){
				phone.remove();
				userPhones.remove(phone);
				break;
			}
		}
	}
	
	private void saveEmails(Collection<Map<String,String>> emails,GroupBusiness groupBusiness,User user,Collection<Email> userEmails) throws Exception{
		if(ListUtil.isEmpty(emails) || (user == null) || (groupBusiness == null)){
			return;
		}
		EmailHome emailHome = (EmailHome) IDOLookup.getHome(Email.class);
		for(Map<String,String> emailData : emails){
			String id = emailData.get("id");
			String address = emailData.get("value");
			String purpose = emailData.get("purpose");
			if(!isValidEmail(address)){
				try{
					Integer key = Integer.valueOf(id);
					if(key > 0){
						Email emailToChange = emailHome.findByPrimaryKey(key);
						emailData.put("value", emailToChange.getEmailAddress());
					}else{
						emailData.put("id", "-1");
					}
				}catch (Exception e) {
					emailData.put("id", "-1");
				}
				continue;
			}
			Email email;
			if(StringUtil.isEmpty(id) || "-1".equals(id)){
				email = groupBusiness.updateGroupMail(user, address);
				if(email == null){
					continue;
				}
			}else{
				email = emailHome.findByPrimaryKey(id);
				if(!userEmails.contains(email)){
					continue;
				}
				email.setEmailAddress(address);
			}
			if(CoreConstants.EMPTY.equals(purpose) || "-1".equals(purpose)){
				purpose = null;
			}
			email.setContactPurpose(purpose);
			email.store();
			emailData.put("id", String.valueOf(email.getPrimaryKey()));
		}
	}
	
	private void savePhones(Collection<Map<String,String>> phones,GroupBusiness groupBusiness,User user,Collection<Phone> userPhones) throws Exception{
		if(ListUtil.isEmpty(phones) || (user == null) || (groupBusiness == null)){
			return;
		}
		PhoneHome phoneHome = (PhoneHome) IDOLookup.getHome(Phone.class);
		for(Map<String,String> contactData : phones){
			String id = contactData.get("id");
			String number = contactData.get("value");
			String purpose = contactData.get("purpose");
			int phoneTypeId;
			try{
				phoneTypeId = Integer.valueOf(contactData.get("phoneTypeId"));
			}catch(Exception e){
				phoneTypeId = -1;
			}
			if(StringUtil.isEmpty(number) || phoneTypeId < 0){
				try{
					Integer key = Integer.valueOf(id);
					if(key > 0){
						Phone phoneToDelete = phoneHome.findByPrimaryKey(key);
						if(userPhones.contains(phoneToDelete)){
							phoneToDelete.remove();
						}
					}
				}catch (Exception e) {
				}
				contactData.put("id", "-1");
				continue;
			}
			Phone phone;
			if(StringUtil.isEmpty(id) || "-1".equals(id)){
				if(phoneTypeId < 0){
					phoneTypeId = 1;
				}
				phone = groupBusiness.updateGroupPhone(user, phoneTypeId, number);
			}else{
				phone = phoneHome.findByPrimaryKey(id);
				if(!userPhones.contains(phone)){
					continue;
				}
				phone.setNumber(number);
				if(phoneTypeId >= 0){
					phone.setPhoneTypeId(phoneTypeId);
				}
			}
			if(CoreConstants.EMPTY.equals(purpose) || "-1".equals(purpose)){
				purpose = null;
			}
			phone.setContactPurpose(purpose);
			phone.store();
			contactData.put("id", String.valueOf(phone.getPrimaryKey()));
		}
	}
	
	@RemoteMethod
	public Map <String,Object> saveUserMessagesData(Map<String,Collection<Map<String,String>>> values, Map<String,Collection<String>> dataToRemove ){
		HashMap<String, Object> response = new HashMap<String, Object>();
		
		IWContext iwc = CoreUtil.getIWContext();
		IWBundle bundle = iwc.getIWMainApplication().getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		if(!iwc.isLoggedOn()){
			response.put("status","Unauthorized");
			response.put("message", iwrb.getLocalizedString("permission_denied", "Permission denied"));
			return response;
		}
		User user = iwc.getCurrentUser();
		try {
			GroupBusiness groupBusiness = IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			Collection<Email> userEmails = user.getEmails();
			Collection<Phone> userPhones = user.getPhones();
			
			Collection<String> DataToRemoveIds = dataToRemove.get("emailsToRemove");
			removeUserEmails(userEmails, DataToRemoveIds);
			
			DataToRemoveIds = dataToRemove.get("phonesToRemove");
			removeUserPhones(userPhones, DataToRemoveIds);
			
			Collection<Map<String,String>> emails = values.get("emails");
			saveEmails(emails, groupBusiness, user, userEmails);
			
			Collection<Map<String,String>> phones = values.get("phones");
			savePhones(phones, groupBusiness, user, userPhones);
			
			response.put("emails", emails);
			response.put("phones", phones);
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "Erorr on removing user email", e);
			response.put("status","Internal Error");
			response.put("message", iwrb.getLocalizedString("error_while_removing_contacts", "Error while removing contacts"));
		}
		response.put("status", "OK");
		response.put("message", iwrb.getLocalizedString("saved", "saved"));
		return response;
	}
	
	@RemoteMethod
	public Map <String,Object> saveCitizenCalendarSettings(Map<String,Collection<String>> settings){
		Map<String, Object> response = new HashMap<String, Object>();
		
		IWContext iwc = CoreUtil.getIWContext();
		IWBundle bundle = iwc.getIWMainApplication().getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		if (!iwc.isLoggedOn()) {
			response.put("status", "Unauthorized");
			response.put("message", iwrb.getLocalizedString("permission_denied", "Permission denied"));
			return response;
		}
		
		CalendarManagementService calendarService = getCalendarManagementService();
		if (calendarService == null) {
			response.put("status", "Error");
			response.put("message", iwrb.getLocalizedString("unable_to_use_calendar", "Unable to use calendar service"));
			return response;
		}
		
		User user = iwc.getCurrentUser();
		try {
			Collection<String> paths = settings.get("subscribedCalendars");
			if (!calendarService.subscribeCalendars(user, paths)) {
				response.put("status","Internal Error");
				response.put("message", iwrb.getLocalizedString(
						"error_while_subscribing", 
						"Error while subscribing."));
			}

			paths = settings.get("unsubscribedCalendars");
			if (!calendarService.unsubscribeCalendars(user, paths)) {
				response.put("status","Internal Error");
				response.put("message", iwrb.getLocalizedString(
						"error_while_unsubscribing", 
						"Error while unsubscribing."));
			}
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "Erorr on removing user email", e);
			response.put("status","Internal Error");
			response.put("message", iwrb.getLocalizedString("error_while_removing_contacts", "Error while removing contacts"));
		}
		response.put("status", "OK");
		response.put("message", iwrb.getLocalizedString("saved", "saved"));
		return response;
	}
	
	private CalendarManagementService getCalendarManagementService() {
		if (calendarManagementService == null)
			ELUtil.getInstance().autowire(this);
		return calendarManagementService;
	}
	
}