package is.idega.idegaweb.egov.citizen.data;

import javax.ejb.FinderException;

import com.idega.data.IDOEntity;

public interface LoginData extends IDOEntity {
	public String getPersonalId();
	public void setPersonalId(String personalId);
	public CitizenRemoteServices getService() throws FinderException;
	public void setService(CitizenRemoteServices service);
	public void setService(Integer serviceId);
	public String getUserName();
	public void setUserName(String userName);
	public String getPassword();
	public void setPassword(String password);
}
