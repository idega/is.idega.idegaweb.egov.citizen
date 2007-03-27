package is.idega.idegaweb.egov.citizen.business;

import java.rmi.RemoteException;

import javax.ejb.CreateException;

import com.idega.business.IBOService;
import com.idega.core.accesscontrol.business.UserHasLoginException;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;

public interface CitizenAccountBusiness extends IBOService, AccountBusiness {

	/**
	 * @see is.idega.idegaweb.egov.citizen.business.CitizenAccountBusinessBean#insertApplication
	 */
	public Integer insertApplication(IWContext iwc, User user, String ssn, String email, String phoneHome, String phoneWork, boolean sendEmail, boolean createLoginAndSendLetter) throws UserHasLoginException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.citizen.business.CitizenAccountBusinessBean#getUserIcelandic
	 */
	public User getUserIcelandic(String ssn) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.citizen.business.CitizenAccountBusinessBean#acceptApplication
	 */
	public void acceptApplication(int applicationID, User performer, boolean createUserMessage, boolean createPasswordMessage, boolean sendEmail) throws CreateException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.citizen.business.CitizenAccountBusinessBean#changePasswordAndSendLetterOrEmail
	 */
	public void changePasswordAndSendLetterOrEmail(IWUserContext iwuc, LoginTable loginTable, User user, String newPassword, boolean sendLetter) throws CreateException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.citizen.business.CitizenAccountBusinessBean#getNumberOfApplications
	 */
	public int getNumberOfApplications() throws RemoteException, RemoteException;
}