package is.idega.idegaweb.egov.citizen.business;


import javax.security.auth.callback.UnsupportedCallbackException;
import javax.ejb.CreateException;
import com.idega.core.accesscontrol.data.LoginTable;
import java.rmi.RemoteException;
import com.idega.user.data.User;
import com.idega.business.IBOService;
import javax.security.auth.callback.Callback;
import com.idega.idegaweb.IWUserContext;
import javax.security.auth.callback.CallbackHandler;

public interface WSCitizenAccountBusiness extends IBOService,
		CitizenAccountBusiness, CallbackHandler {
	/**
	 * @see is.idega.idegaweb.egov.citizen.business.WSCitizenAccountBusinessBean#changePasswordAndSendLetterOrEmail
	 */
	public void changePasswordAndSendLetterOrEmail(IWUserContext iwuc,
			LoginTable loginTable, User user, String newPassword,
			boolean sendLetter) throws CreateException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.citizen.business.WSCitizenAccountBusinessBean#handle
	 */
	public void handle(Callback[] callbacks)
			throws UnsupportedCallbackException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.citizen.business.WSCitizenAccountBusinessBean#sendMessageToBank
	 */
	public boolean sendMessageToBank() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.citizen.business.WSCitizenAccountBusinessBean#sendLostPasswordMessage
	 */
	public void sendLostPasswordMessage(User citizen, String login,
			String password) throws RemoteException, CreateException,
			RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.citizen.business.WSCitizenAccountBusinessBean#getAcceptMessageSubject
	 */
	public String getAcceptMessageSubject(User owner) throws RemoteException;
}