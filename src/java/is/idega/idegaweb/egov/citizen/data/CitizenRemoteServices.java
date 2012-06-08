package is.idega.idegaweb.egov.citizen.data;

import java.util.Collection;

import com.idega.data.IDOEntity;
import com.idega.data.IDORelationshipException;
import com.idega.user.data.User;

public interface CitizenRemoteServices extends IDOEntity {
	public String getServerName();
	public void setServerName(String name);
	public String getAddress();
	public void setAddress(String address);
	public Collection<User> getUsers() throws IDORelationshipException;
	public void addUser(User user)  throws IDORelationshipException;
	public void removeUser(User user)throws IDORelationshipException;
}