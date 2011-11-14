package is.idega.idegaweb.egov.citizen.data;

import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.data.IDOEntity;
import com.idega.data.IDOFactory;
import com.idega.user.data.User;

public class LoginDataHomeImpl extends IDOFactory implements LoginDataHome{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7913091932006429632L;

	@Override
	protected Class<? extends IDOEntity> getEntityInterfaceClass() {
		return LoginData.class;
	}

	@Override
	public Collection<LoginData> getLoginData(User user) throws FinderException {
		LoginDataBMPBean entity = (LoginDataBMPBean)this.idoCheckOutPooledEntity();
		Collection <Integer> ids = entity.getLoginData(user);
		this.idoCheckInPooledEntity(entity);
		return getEntityCollectionForPrimaryKeys(ids);
	}
}
