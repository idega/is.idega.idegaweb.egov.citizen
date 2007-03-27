package is.idega.idegaweb.egov.citizen.business;

import java.rmi.RemoteException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.idega.business.IBOService;

public interface WSCitizenAccountBusiness extends IBOService, CitizenAccountBusiness, CallbackHandler {

	/**
	 * @see is.idega.idegaweb.egov.citizen.business.WSCitizenAccountBusinessBean#handle
	 */
	public void handle(Callback[] callbacks) throws UnsupportedCallbackException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.citizen.business.WSCitizenAccountBusinessBean#sendMessageToBank
	 */
	public boolean sendMessageToBank() throws RemoteException;
}