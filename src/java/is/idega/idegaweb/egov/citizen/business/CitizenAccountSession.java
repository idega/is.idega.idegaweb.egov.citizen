package is.idega.idegaweb.egov.citizen.business;

import java.rmi.RemoteException;

import com.idega.business.IBOSession;

/**
 * @author alindman
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface CitizenAccountSession extends IBOSession {

	public boolean getIfUserUsesCOAddress() throws RemoteException;
	public void setIfUserUsesCOAddress(boolean preference) throws RemoteException;
}
