package is.idega.idegaweb.egov.citizen.business;

import java.rmi.RemoteException;

import javax.ejb.CreateException;

import com.idega.core.accesscontrol.business.LoginCreateException;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.PasswordNotKnown;
import com.idega.data.IDOCreateException;
import com.idega.user.data.User;

import se.idega.idegaweb.commune.account.citizen.business.CitizenAccountBusiness;
import se.idega.idegaweb.commune.account.citizen.business.CitizenAccountBusinessBean;
import se.idega.idegaweb.commune.account.data.AccountApplication;

public class WSCitizenAccountBusinessBean extends CitizenAccountBusinessBean
		implements WSCitizenAccountBusiness, CitizenAccountBusiness {
	
	/**
	 * Creates a Login for a user with application theCase and send a message to the user that applies if it is successful.
	 * @param theCase The Account Application
	 * @throws CreateException Error creating data objects.
	 * @throws LoginCreateException If an error occurs creating login for the user.
	 */
	protected void createLoginAndSendMessage(AccountApplication theCase, boolean createUserMessage, boolean createPasswordMessage, boolean sendEmail) throws RemoteException, CreateException, LoginCreateException
	{	
		boolean sendLetter = false;
		LoginTable lt;
		String login;
		User citizen;
		citizen = theCase.getOwner();
		lt = getUserBusiness().generateUserLogin(citizen);
		login = lt.getUserLogin();
		try
		{
			String password = lt.getUnencryptedUserPassword();
			String messageBody = this.getAcceptMessageBody(theCase, login, password);
			String messageSubject = this.getAcceptMessageSubject();

			if (createUserMessage){
				this.getMessageBusiness().createUserMessage(citizen, messageSubject, messageBody,sendLetter);
			}			
			if(createPasswordMessage){
				this.getMessageBusiness().createPasswordMessage(citizen,login,password);			
			}
			
			createUserMessage = sendEmail;
		}
		catch (PasswordNotKnown e)
		{
			//e.printStackTrace();
			throw new IDOCreateException(e);
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
	}
}