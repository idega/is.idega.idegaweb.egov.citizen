package is.idega.idegaweb.egov.citizen.data;

import java.util.Collection;

import com.idega.data.IDOHome;

public interface CitizenRemoteServicesHome extends IDOHome {
	public Collection<CitizenRemoteServices> getRemoteServicesByNames(Collection <String> names);
	public Collection<CitizenRemoteServices> getRemoteServices(int maxAmount);
}
