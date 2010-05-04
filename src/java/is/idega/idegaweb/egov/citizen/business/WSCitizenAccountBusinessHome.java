package is.idega.idegaweb.egov.citizen.business;


import javax.ejb.CreateException;
import java.rmi.RemoteException;
import com.idega.business.IBOHome;

public interface WSCitizenAccountBusinessHome extends IBOHome {
	public WSCitizenAccountBusiness create() throws CreateException,
			RemoteException;
}