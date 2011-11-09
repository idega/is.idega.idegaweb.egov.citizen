package is.idega.idegaweb.egov.citizen.data;

import com.idega.data.IDOEntity;

public interface CitizenRemoteServices extends IDOEntity {
	public String getServerName();
	public void setServerName(String name);
	public String getAddress();
	public void setAddress(String address);
}
