package is.idega.idegaweb.egov.citizen.data;

import java.util.Collection;

import com.idega.data.IDOHome;
import com.idega.data.IDORelationshipException;

public interface CitizenRemoteServicesHome extends IDOHome {
	public Collection<CitizenRemoteServices> getRemoteServicesByNames(Collection <String> names);
	public Collection<CitizenRemoteServices> getRemoteServices(int maxAmount);
	public Collection<CitizenRemoteServices> getRemoteServicesByUserId(String userId) throws IDORelationshipException ;
}
