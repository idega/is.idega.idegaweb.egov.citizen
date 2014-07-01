/*
 * $Id$
 *
 * Copyright (C) 2002 Idega hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 *
 */
package is.idega.idegaweb.egov.citizen.business;

import is.idega.idegaweb.egov.accounting.business.CitizenBusiness;
import is.idega.idegaweb.egov.citizen.IWBundleStarter;
import is.idega.idegaweb.egov.citizen.data.AccountApplication;
import is.idega.idegaweb.egov.message.business.CommuneMessageBusiness;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.logging.Level;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.idega.block.process.business.CaseBusinessBean;
import com.idega.core.accesscontrol.business.LoginCreateException;
import com.idega.core.accesscontrol.business.UserHasLoginException;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.PasswordNotKnown;
import com.idega.data.IDOCreateException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;

/**
 * @author <a href="mailto:tryggvi@idega.is">Tryggvi Larusson</a>
 * @version 1.0
 */
public abstract class AccountApplicationBusinessBean extends CaseBusinessBean implements AccountBusiness {

	private static final long serialVersionUID = 8181014498371937015L;

	protected abstract Class<?> getCaseEntityClass();

	@Override
	public CommuneMessageBusiness getMessageBusiness() throws RemoteException {
		return this.getServiceInstance(CommuneMessageBusiness.class);
	}

	protected AccountApplication getApplication(int applicationID) throws FinderException {
		return (AccountApplication) this.getCase(applicationID);
	}

	/**
	 * Accepts the application for an application with ID applicationID by User performer
	 *
	 * @param The
	 *          id of the application to be accepted
	 * @param performer
	 *          The User that accepts the application
	 * @param createUserMessage
	 * @param createPasswordMessage
	 * @throws CreateException
	 *           If there is an error creating data objects.
	 * @throws FinderException
	 *           If an application with applicationID is not found.
	 */
	@Override
	public void acceptApplication(int applicationID, User performer, boolean createUserMessage, boolean createPasswordMessage, boolean sendEmail, boolean sendSnailMail) throws CreateException {
		UserTransaction trans = null;
		User user = null;
		try {
			trans = this.getSessionContext().getUserTransaction();
			trans.begin();
			AccountApplication theCase = this.getApplication(applicationID);
			changeCaseStatus(theCase, getCaseStatusGranted().getStatus(), performer);
			user = theCase.getOwner();

			addUserToRootAcceptedCitizenGroup(user);

			createLoginAndSendMessage(theCase, createUserMessage, createPasswordMessage, sendEmail, sendSnailMail);

			trans.commit();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error accepting application by ID: " + applicationID + ", performer: " + performer + (performer == null ? CoreConstants.EMPTY : "(" + performer.getPersonalID() + ")") +
					", send email: " + sendEmail + ", send snail mail: " + sendSnailMail, e);

			if (trans != null) {
				try {
					trans.rollback();
				}
				catch (SystemException se) {
					se.printStackTrace();
				}
			}
			if (e instanceof UserHasLoginException) {
				throw new UserHasLoginException();
			}
			else {
				throw new CreateException("There was an error accepting the application. Message was: " + e.getMessage());
			}
		}

		if (user != null) {
			try {
				getUserBusiness().callAllUserGroupPluginAfterUserCreateOrUpdateMethod(user);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
			catch (CreateException e) {
				e.printStackTrace();
			}
		}

	}

	private void addUserToRootAcceptedCitizenGroup(User user) throws RemoteException {
		Group acceptedCitizens;
		try {
			acceptedCitizens = getUserBusiness().getRootAcceptedCitizenGroup();
			acceptedCitizens.addGroup(user, IWTimestamp.getTimestampRightNow());
			if (user.getPrimaryGroup() == null) {
				user.setPrimaryGroup(acceptedCitizens);
				user.store();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * calls acceptApplication(int applicationID, User performer, Boolean createUserMessage, Boolean createPasswordMessage) with
	 * createUserMessage=shouldEmailBeSentWhenANewAccountIsInserted()
	 *
	 * @param The
	 *          id of the application to be accepted
	 * @param performer
	 *          The User that accepts the application
	 * @throws CreateException
	 *           If there is an error creating data objects.
	 * @throws FinderException
	 *           If an application with applicationID is not found.
	 */
	@Override
	public void acceptApplication(int applicationID, User performer, boolean createPasswordMessage) throws CreateException {
		acceptApplication(applicationID, performer, shouldEmailBeSentWhenANewAccountIsInserted(), createPasswordMessage, false, false);
	}

	protected String getAcceptMessageBody(AccountApplication theCase, String login, String password) {
		IWResourceBundle iwrb = this.getIWResourceBundleForUser(theCase.getOwner());

		Object[] arguments = { theCase.getApplicantName(), login, password, getApplicationLoginURL() };
		String body = iwrb.getLocalizedString("acc.app.appr.body", "Dear mr./ms./mrs. {0}\n\nYour application has been accepted and you have been given access to the system with the following login information:\n\nUserName: {1}\nPassword: {2}\n\nYou can log on via: {3}.");
		return MessageFormat.format(body, arguments);

	}

	@Override
	public String getAcceptMessageSubject(AccountApplication theCase) {
		IWResourceBundle iwrb = this.getIWResourceBundleForUser(theCase.getOwner());
		return iwrb.getLocalizedString("acc.app.appr.subj", "Your application has been approved");
	}

	protected String getApplicationLoginURL() {
		return getIWApplicationContext().getApplicationSettings().getProperty("app_url_login", "http://www.egov.is");
	}

	protected void sendAcceptMessage(AccountApplication accAppl, String subject, String body) throws RemoteException {
		sendAcceptEMailMessage(accAppl.getEmail(), subject, body);
	}

	protected void sendAcceptEMailMessage(String email, String subject, String body) throws RemoteException {
		if (email != null) {
			getMessageBusiness().sendMessage(email, subject, body);
		}
	}

	protected void sendRejectMessage(AccountApplication accAppl, String subject, String body) throws RemoteException {
		sendRejectEMailMessage(accAppl.getEmail(), subject, body);
	}

	protected void sendRejectEMailMessage(String email, String subject, String body) throws RemoteException {
		if (email != null) {
			getMessageBusiness().sendMessage(email, subject, body);
		}
	}

	/**
	 * Creates a Login for a user with application theCase and send a message to the user that applies if it is successful.
	 *
	 * @param theCase
	 *          The Account Application
	 * @throws CreateException
	 *           Error creating data objects.
	 * @throws LoginCreateException
	 *           If an error occurs creating login for the user.
	 */
	protected void createLoginAndSendMessage(AccountApplication theCase, boolean createUserMessage, boolean createPasswordMessage, boolean sendEmail, boolean sendSnailMail) throws RemoteException, CreateException, LoginCreateException {
		boolean sendLetter = false;
		LoginTable lt;
		String login;
		User citizen;
		citizen = theCase.getOwner();
		lt = getUserBusiness().generateUserLogin(citizen);
		login = lt.getUserLogin();
		try {
			String password = lt.getUnencryptedUserPassword();
			String messageBody = this.getAcceptMessageBody(theCase, login, password);
			String messageSubject = this.getAcceptMessageSubject(theCase);

			if (createUserMessage) {
				this.getMessageBusiness().createUserMessage(citizen, messageSubject, messageBody, sendLetter);
			}
			if (createPasswordMessage) {
				this.getMessageBusiness().createPasswordMessage(citizen, login, password);
			}

			createUserMessage = sendEmail;
		}
		catch (PasswordNotKnown e) {
			throw new IDOCreateException(e);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	protected CitizenBusiness getUserBusiness() throws RemoteException {
		return this.getServiceInstance(CitizenBusiness.class);
	}

	@Override
	public String getBundleIdentifier() {
		return IWBundleStarter.IW_BUNDLE_IDENTIFIER;
	}

	protected boolean shouldEmailBeSentWhenANewAccountIsInserted() {
		return true;
	}
}