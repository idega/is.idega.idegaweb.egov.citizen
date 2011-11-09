package is.idega.idegaweb.egov.citizen.data;

import java.util.Collection;
import java.util.logging.Level;

import javax.ejb.FinderException;

import com.idega.data.GenericEntity;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;

public class LoginDataBMPBean extends GenericEntity implements LoginData  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6922632599950269149L;
	
	private static final String TABLE_NAME = "comm_login_data";
	
	private static final String COLUMN_PERSONAL_ID= "PERSONAL_ID";
	private static final String COLUMN_USERNAME = "USERNAME";
	private static final String COLUMN_PASSWORD = "PASSWORD";
	private static final String COLUMN_SERVICE = "SERVICES";
	
	private CitizenRemoteServicesHome citizenRemoteServicesHome = null;
	
	@Override
	public void initializeAttributes() {
		addAttribute(getIDColumnName());
		
		addAttribute(COLUMN_PERSONAL_ID, "Personal id",String.class);
		addAttribute(COLUMN_USERNAME, "userName",String.class);
		addAttribute(COLUMN_PASSWORD, "password", String.class);
		addAttribute(COLUMN_SERVICE, "service", true, true, Integer.class, ONE_TO_ONE, CitizenRemoteServices.class);
	}
	
	@Override
	public String getPersonalId() {
		return getStringColumnValue(COLUMN_PERSONAL_ID);
	}

	@Override
	public void setPersonalId(String personalId) {
		setColumn(COLUMN_PERSONAL_ID, personalId);
	}

	
	@Override
	public String getUserName() {
		return getStringColumnValue(COLUMN_USERNAME);
	}

	@Override
	public void setUserName(String userName) {
		setColumn(COLUMN_USERNAME, userName);
	}

	@Override
	public String getPassword() {
		return getStringColumnValue(COLUMN_PASSWORD);
	}

	@Override
	public void setPassword(String password) {
		setColumn(COLUMN_PASSWORD, password);
	}

	@Override
	public String getEntityName() {
		return TABLE_NAME;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Integer> getLoginData(User user) throws FinderException{
		StringBuilder query = new StringBuilder("SELECT l.").append(getIDColumnName()).append(" FROM ")
			.append(TABLE_NAME).append(" l WHERE ").append(" l.").append(COLUMN_PERSONAL_ID).append(" = '")
			.append(user.getPersonalID()).append(CoreConstants.QOUTE_SINGLE_MARK);
		return idoFindPKsBySQL(query.toString());
	}

	@Override
	public CitizenRemoteServices getService() throws FinderException {
		int serviceId = getIntColumnValue(COLUMN_SERVICE);
		return getCitizenRemoteServicesHome().findByPrimaryKeyIDO((Object)serviceId);
	}

	@Override
	public void setService(CitizenRemoteServices service) {
		setColumn(COLUMN_SERVICE, service);
	}
	@Override
	public void setService(Integer serviceId) {
		setColumn(COLUMN_SERVICE, serviceId);
	}
	
	 private CitizenRemoteServicesHome getCitizenRemoteServicesHome(){
		 if(citizenRemoteServicesHome == null){
			 try{
				 citizenRemoteServicesHome = (CitizenRemoteServicesHome)IDOLookup.getHome(CitizenRemoteServices.class);
			 }catch(IDOLookupException e){
				 this.getLogger().log(Level.WARNING, "Failed getting MultimediaHome", e);
			 }
		 }
		 return citizenRemoteServicesHome;
	 }
}
