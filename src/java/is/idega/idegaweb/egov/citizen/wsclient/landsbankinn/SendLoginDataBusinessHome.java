package is.idega.idegaweb.egov.citizen.wsclient.landsbankinn;


import javax.ejb.CreateException;
import java.rmi.RemoteException;
import com.idega.business.IBOHome;

public interface SendLoginDataBusinessHome extends IBOHome {
	public SendLoginDataBusiness create() throws CreateException,
			RemoteException;
}