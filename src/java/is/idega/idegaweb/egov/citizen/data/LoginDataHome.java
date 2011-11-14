package is.idega.idegaweb.egov.citizen.data;

import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.data.IDOHome;
import com.idega.user.data.User;

public interface LoginDataHome extends IDOHome {
	Collection <LoginData> getLoginData(User user) throws FinderException;
}
