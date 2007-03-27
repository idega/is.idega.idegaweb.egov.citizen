package is.idega.idegaweb.egov.citizen.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHome;
import java.rmi.RemoteException;

public interface CitizenAccountBusinessHome extends IBOHome {

	public CitizenAccountBusiness create() throws CreateException, RemoteException;
}