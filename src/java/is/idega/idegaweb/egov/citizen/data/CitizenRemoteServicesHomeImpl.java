package is.idega.idegaweb.egov.citizen.data;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.FinderException;

import com.idega.data.IDOEntity;
import com.idega.data.IDOFactory;

public class CitizenRemoteServicesHomeImpl extends IDOFactory implements CitizenRemoteServicesHome{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3041594743194171003L;

	@Override
	protected Class<? extends IDOEntity> getEntityInterfaceClass() {
		return CitizenRemoteServices.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<CitizenRemoteServices> getRemoteServicesByNames(Collection<String> names) {
		com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection<Integer> ids = ((CitizenRemoteServicesBMPBean)entity).ejbFindByServiceNames(names);
		this.idoCheckInPooledEntity(entity);
		try {
			return findByPrimaryKeyCollection(ids);
		} catch (FinderException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "failed finding remote services by names "+names);
			return Collections.emptyList();
		}
	}

	@Override
	public Collection<CitizenRemoteServices> getRemoteServices(int maxAmount) {
		com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
		try {
			Collection<Integer> ids = ((CitizenRemoteServicesBMPBean)entity).ejbFindByServices(maxAmount);
			this.idoCheckInPooledEntity(entity);
			return findByPrimaryKeyCollection(ids);
		} catch (FinderException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "failed finding any remote services");
			return Collections.emptyList();
		}
	}

}
