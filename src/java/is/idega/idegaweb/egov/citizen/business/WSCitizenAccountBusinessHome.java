package is.idega.idegaweb.egov.citizen.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHome;
import java.rmi.RemoteException;

public interface WSCitizenAccountBusinessHome extends IBOHome {

	public WSCitizenAccountBusiness create() throws CreateException, RemoteException;
}