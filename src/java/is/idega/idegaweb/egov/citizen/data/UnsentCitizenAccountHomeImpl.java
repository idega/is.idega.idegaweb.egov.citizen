package is.idega.idegaweb.egov.citizen.data;


import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import com.idega.data.IDOEntity;
import com.idega.data.IDOFactory;

public class UnsentCitizenAccountHomeImpl extends IDOFactory implements
		UnsentCitizenAccountHome {
	public Class getEntityInterfaceClass() {
		return UnsentCitizenAccount.class;
	}

	public UnsentCitizenAccount create() throws CreateException {
		return (UnsentCitizenAccount) super.createIDO();
	}

	public UnsentCitizenAccount findByPrimaryKey(Object pk)
			throws FinderException {
		return (UnsentCitizenAccount) super.findByPrimaryKeyIDO(pk);
	}

	public Collection findAll() throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((UnsentCitizenAccountBMPBean) entity).ejbFindAll();
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}
}