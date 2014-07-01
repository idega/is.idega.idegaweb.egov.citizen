/*
 * $Id$ Copyright (C) 2002 Idega hf. All Rights Reserved. This software is
 * the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.citizen.business;

import is.idega.idegaweb.egov.citizen.data.AccountApplication;
import is.idega.idegaweb.egov.citizen.data.CitizenAccount;
import is.idega.idegaweb.egov.citizen.data.CitizenAccountHome;
import is.idega.idegaweb.egov.message.business.CommuneMessageBusiness;

import java.rmi.RemoteException;
import java.util.logging.Level;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.idega.business.IBORuntimeException;
import com.idega.core.accesscontrol.business.UserHasLoginException;
import com.idega.core.accesscontrol.data.LoginInfo;
import com.idega.core.accesscontrol.data.LoginInfoHome;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.data.IDOException;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.Encrypter;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;

/**
 * Last modified: $Date$ by $Author$
 *
 * @author <a href="mail:palli@idega.is">Pall Helgason </a>
 * @author <a href="http://www.staffannoteberg.com">Staffan N?teberg </a>
 * @version $Revision$
 */
public class CitizenAccountBusinessBean extends AccountApplicationBusinessBean implements CitizenAccountBusiness, AccountBusiness {

	private static final long serialVersionUID = -8304259532337909367L;

	private boolean acceptApplicationOnCreation = true;

	protected CitizenAccountHome getCitizenAccountHome() throws RemoteException {
		return (CitizenAccountHome) IDOLookup.getHome(CitizenAccount.class);
	}

	/**
	 * Creates an application for CitizenAccount for a user with a personalId that is in the system.
	 *
	 * @param user
	 *          The user that makes the application
	 * @param ssn
	 *          The PersonalId of the User to apply for.
	 * @param email
	 *          Email of the user
	 * @param phoneHome
	 *          the Home phone of the user
	 * @param phoneWork
	 *          the Work phone of the user
	 * @return Integer appliaction id or null if insertion was unsuccessful
	 * @throws UserHasLoginException
	 *           If A User already has a login in the system.
	 */

	@Override
	public Integer insertApplication(IWContext iwc, User user, String ssn, String email, String phoneHome, String phoneWork, boolean sendEmail, boolean createLoginAndSendLetter, boolean sendSnailMail) throws UserHasLoginException {
		CitizenAccount application = null;
		UserTransaction transaction = null;
		// NBSLoginBusinessBean loginBusiness = new NBSLoginBusinessBean();
		// NBSLoggedOnInfo info = loginBusiness.getBankIDLoggedOnInfo(iwc);

		try {
			transaction = getSessionContext().getUserTransaction();
			transaction.begin();
			application = ((CitizenAccountHome) IDOLookup.getHome(CitizenAccount.class)).create();
			application.setSsn(ssn);
			if (user != null) {
				application.setOwner(user);
				if (user.getName() != null) {
					application.setApplicantName(user.getName());
				}
			}
			application.setPhoneHome(phoneHome);
			if (!"".equals(email)) {
				application.setEmail(email);
			}
			if (phoneWork != null) {
				application.setPhoneWork(phoneWork);
			}
			application.setCaseStatus(getCaseStatusOpen());

			application.store();

			transaction.commit();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error creating citizen account for " + ssn + ", email: " + email, e);

			if (transaction != null) {
				try {
					// application = null;
					transaction.rollback();
				}
				catch (SystemException se) {
					se.printStackTrace();
				}
			}
			if (e instanceof UserHasLoginException) {
				throw (UserHasLoginException) e;
			}

			return null;
		}

		try {
			if (application != null) {
				int applicationID = ((Integer) application.getPrimaryKey()).intValue();
				if (this.acceptApplicationOnCreation) {
					if (!createLoginAndSendLetter) {
						acceptApplication(applicationID, user, false);
					} else {
						acceptApplication(applicationID, user, shouldEmailBeSentWhenANewAccountIsInserted(), true, sendEmail, sendSnailMail);
					}
				} else {
					getLogger().info("It is configured not to accept citizen's application on creation. Applicant's personal ID:" + ssn);
				}
			}
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error accepting citizen's application for user with personal ID: " + ssn + ", email: " + email + ", send mail: " + sendEmail + ", send snail mail: " + sendSnailMail, e);
		}

		return (Integer) (application == null ? null : application.getPrimaryKey());
	}

	@Override
	public User getUserIcelandic(String ssn) {
		User user = null;
		try {
			StringBuffer userSsn = new StringBuffer(ssn);
			int i = ssn.indexOf('-');
			if (i != -1) {
				userSsn.deleteCharAt(i);
				ssn = userSsn.toString();
			}

			user = ((UserHome) IDOLookup.getHome(User.class)).findByPersonalID(userSsn.toString());
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			if (ssn.length() == 10) {
				StringBuffer userSsn = new StringBuffer("20");
				userSsn.append(ssn);
				try {
					user = ((UserHome) IDOLookup.getHome(User.class)).findByPersonalID(userSsn.toString());
				}
				catch (Exception ex) {
					return null;
				}
			}
		}

		return user;
	}

	@Override
	protected AccountApplication getApplication(int applicationID) throws FinderException {
		return getAccount(applicationID);
	}

	private CitizenAccount getAccount(int id) throws FinderException {
		try {
			CitizenAccountHome home = (CitizenAccountHome) IDOLookup.getHome(CitizenAccount.class);
			return (CitizenAccount) home.findByPrimaryKeyIDO(new Integer(id));
		}
		catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	@Override
	protected Class<CitizenAccount> getCaseEntityClass() {
		return CitizenAccount.class;
	}

	@Override
	public void acceptApplication(int applicationID, User performer, boolean createUserMessage, boolean createPasswordMessage, boolean sendEmail, boolean sendSnailMail) throws CreateException {
		UserTransaction transaction = null;
		try {
			transaction = getSessionContext().getUserTransaction();
			transaction.begin();
			CitizenAccount applicant = getAccount(applicationID);
			User user = getUserBusiness().getUser(applicant.getSsn());
			getUserBusiness().storeUserEmail(user, applicant.getEmail(), true);

			getUserBusiness().updateUserHomePhone(user, applicant.getPhoneHome());
			getUserBusiness().updateUserMobilePhone(user, applicant.getPhoneWork());

			applicant.setOwner(user);
			applicant.store();
			super.acceptApplication(applicationID, performer, createUserMessage, createPasswordMessage, sendEmail, sendSnailMail);
			transaction.commit();
		}
		catch (Exception e) {
			if (transaction != null) {
				try {
					transaction.rollback();
				}
				catch (SystemException se) {
					se.printStackTrace();
				}
			}

			if (e instanceof UserHasLoginException) {
				throw (UserHasLoginException) e;
			}
			else {
				e.printStackTrace();
				throw new CreateException(e.getMessage());
			}
		}
	}

	/**
	 * Changes a password for CitizenAccount for a user and sends a letter and/or email.
	 *
	 * @param loginTable
	 *          LoginTable of the user
	 * @param user
	 *          User
	 * @param newPassword
	 *          Password in plain text (not already encrypted)
	 * @param sendLetter
	 *          True if a letter should be sent else false
	 * @param sendEmail
	 *          True if an email should be sent else false
	 * @throws CreateException
	 *           If changing of the password failed.
	 */
	@Override
	public void changePasswordAndSendLetterOrEmail(IWUserContext iwuc, LoginTable loginTable, User user, String newPassword, boolean sendLetter) throws CreateException {

		UserTransaction trans = null;
		try {
			trans = this.getSessionContext().getUserTransaction();
			trans.begin();

			// encrypt new password
			String encryptedPassword = Encrypter.encryptOneWay(newPassword);
			// store new password
			loginTable.setUserPassword(encryptedPassword, newPassword);
			loginTable.store();

			LoginInfo loginInfo = ((LoginInfoHome) IDOLookup.getHome(LoginInfo.class)).findByPrimaryKey(loginTable.getPrimaryKey());
			loginInfo.setFailedAttemptCount(0);
			loginInfo.setAccessClosed(false);
			loginInfo.setAccountEnabled(true);
			loginInfo.store();

			// set content of letter
			String userName = user.getName();
			String loginName = loginTable.getUserLogin();
			String messageSubject = getNewPasswordWasCreatedSubject(user);
			String messageBody = getNewPasswordWasCreatedMessageBody(user, userName, loginName, newPassword);

			// send letter or email to user
			CommuneMessageBusiness messageBusiness = getMessageBusiness();
			if (sendLetter) {
				messageBusiness.createPasswordMessage(user, loginName, newPassword);
			}
			else {
				messageBusiness.createUserMessage(user, messageSubject, messageBody, true);
			}

			trans.commit();
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			// e.printStackTrace();
			if (trans != null) {
				try {
					trans.rollback();
				}
				catch (SystemException se) {
					se.printStackTrace();
				}
			}
			throw new CreateException("There was an error changing the password. Message was: " + e.getMessage());
		}
	}

	@Override
	public int getNumberOfApplications() throws RemoteException {
		try {
			return getCitizenAccountHome().getTotalCount();
		}
		catch (IDOException ie) {
			return 0;
		}
	}

	protected String getNewPasswordWasCreatedSubject(User user) {
		IWResourceBundle iwrb = this.getIWResourceBundleForUser(user);
		String subject = iwrb.getLocalizedString("acc.app.acc.fp.subj", "New password for your account");
		if (subject.indexOf("{4}") != -1) {
			String replace = CoreUtil.getIWContext().getDomain().getName();
			subject = StringHandler.replace(subject, "{4}", StringUtil.isEmpty(replace) ? CoreConstants.EMPTY : replace);
		}
		return subject;
	}

	protected String getNewPasswordWasCreatedMessageBody(User user, String userName, String loginName, String password) {
		IWResourceBundle iwrb = this.getIWResourceBundleForUser(user);

		String body = iwrb.getLocalizedString("acc.app.acc.body1", "Dear mr./ms./mrs. ");
		body += userName + "\n";
		body += iwrb.getLocalizedString("acc.app.acc.fp.body2", "A new password was created for your account.") + "\n\n";
		body += iwrb.getLocalizedString("acc.app.acc.body3", "You have been given access to the system with username: ");
		body += " \"" + loginName + "\" ";
		body += iwrb.getLocalizedString("acc.app.acc.body4", " and password: ");
		body += " \"" + password + "\"";
		body += "\n\n";
		body += iwrb.getLocalizedString("acc.app.acc.body5", "You can log on via:") + " ";
		body += getApplicationLoginURL();

		if (body.indexOf("{4}") != -1) {
			String replace = CoreUtil.getIWContext().getDomain().getName();
			body = StringHandler.replace(body, "{4}", StringUtil.isEmpty(replace) ? CoreConstants.EMPTY : replace);
		}

		return body;
	}

}
