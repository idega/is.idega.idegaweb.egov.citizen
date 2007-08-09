package is.idega.idegaweb.egov.citizen.business.landsbankan;

import javax.ejb.CreateException;
import com.idega.business.IBOHome;
import java.rmi.RemoteException;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public interface SendLoginDataBusinessHome extends IBOHome {

	public SendLoginDataBusiness create() throws CreateException, RemoteException;
}