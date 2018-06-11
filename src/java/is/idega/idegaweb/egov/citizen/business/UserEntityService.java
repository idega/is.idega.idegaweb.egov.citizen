package is.idega.idegaweb.egov.citizen.business;

import com.idega.user.data.User;

public interface UserEntityService {

	public User getUserForEntity(String entityPersonalId);

}