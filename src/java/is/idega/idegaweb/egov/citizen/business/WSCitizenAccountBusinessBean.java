package is.idega.idegaweb.egov.citizen.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.rpc.ServiceException;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.encoding.Base64;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;

import com.idega.business.IBORuntimeException;
import com.idega.core.accesscontrol.business.LoginCreateException;
import com.idega.core.accesscontrol.data.LoginInfo;
import com.idega.core.accesscontrol.data.LoginInfoHome;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.PasswordNotKnown;
import com.idega.core.idgenerator.business.IdGenerator;
import com.idega.core.idgenerator.business.IdGeneratorFactory;
import com.idega.data.IDOCreateException;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.user.data.User;
import com.idega.util.Encrypter;
import com.idega.util.FileUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.text.Name;

import is.idega.idegaweb.egov.citizen.data.AccountApplication;
import is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccount;
import is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountHome;
import is.idega.idegaweb.egov.citizen.wsclient.islandsbanki.BirtingakerfiWSLocator;
import is.idega.idegaweb.egov.citizen.wsclient.islandsbanki.BirtingakerfiWSSoap_PortType;
import is.idega.idegaweb.egov.citizen.wsclient.landsbankinn.SendLoginDataBusiness;

public class WSCitizenAccountBusinessBean extends CitizenAccountBusinessBean
		implements WSCitizenAccountBusiness, CitizenAccountBusiness, CallbackHandler {

	private static final long serialVersionUID = -8824721140289363380L;

	public static final String BANK_SEND_REGISTRATION = "BANK_SEND_REGISTRATION";

	protected static final String USE_LANDSBANKINN = "USE_LANDSBANKINN";

	protected static final String BANK_SENDER_PIN = "BANK_SENDER_PIN";

	protected static final String BANK_SENDER_USER_ID = "BANK_SENDER_USER_ID";

	protected static final String BANK_SENDER_USER_PASSWORD = "BANK_SENDER_USER_PW";

	public static final String BANK_SENDER_PAGELINK = "BANK_SENDER_PAGELINK";

	public static final String BANK_SENDER_LOGOLINK = "BANK_SENDER_LOGOLINK";

	protected static final String BANK_SENDER_TYPE = "BANK_SENDER_TYPE";

	protected static final String BANK_SENDER_TYPE_VERSION = "BANK_SENDER_TYPE_VERSION";

	protected static final String USER_CREATION_TYPE = "RVKLB";

	public static final String CITIZEN_MAYOR_NAME = "citizen.mayor.name";
	public static final String CITIZEN_MAYOR_SIGNATURE_URL = "citizen.mayor.signature.url";

	protected static final String SERVICE_URL = "https://ws.isb.is/adgerdirv1/birtingakerfi.asmx";

	/**
	 * Creates a Login for a user with application theCase and send a message to
	 * the user that applies if it is successful.
	 *
	 * @param theCase
	 *            The Account Application
	 * @throws CreateException
	 *             Error creating data objects.
	 * @throws LoginCreateException
	 *             If an error occurs creating login for the user.
	 */
	@Override
	protected void createLoginAndSendMessage(AccountApplication theCase, boolean createUserMessage,
			boolean createPasswordMessage, boolean sendEmail, boolean sendSnailMail)
					throws RemoteException, CreateException, LoginCreateException {

		boolean sendLetter = false;
		User citizen = theCase.getOwner();
		LoginTable lt = getUserBusiness().generateUserLogin(citizen);
		String login = lt.getUserLogin();

		try {
			String password = lt.getUnencryptedUserPassword();

			String messageBody = this.getAcceptMessageBody(theCase, login, password);
			String messageSubject = this.getAcceptMessageSubject(theCase);

			if (createPasswordMessage && sendSnailMail)
				this.getMessageBusiness().createPasswordMessage(citizen, login, password);

			createUserMessage = sendEmail;

			boolean sendMessageToBank = sendMessageToBank();

			if (sendMessageToBank) {
				try {
					String pageLink = getIWApplicationContext().getApplicationSettings()
							.getProperty(BANK_SENDER_PAGELINK);
					String logoLink = getIWApplicationContext().getApplicationSettings()
							.getProperty(BANK_SENDER_LOGOLINK);
					String ssn = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_PIN);
					String user3 = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_TYPE);
					String user3version = getIWApplicationContext().getApplicationSettings()
							.getProperty(BANK_SENDER_TYPE_VERSION, "001");

					String xml = getXML(
							new Name(citizen.getFirstName(), citizen.getMiddleName(), citizen.getLastName()).getName(),
							login, password, pageLink, logoLink,
							sendToLandsbankinn() ? "1" : citizen.getPrimaryKey().toString(), citizen.getPersonalID(),
							user3, user3version);

					if (sendToLandsbankinn()) {

						SendLoginDataBusiness send_data = getServiceInstance(SendLoginDataBusiness.class);

						send_data.send(xml);

						// System.out.println("xml = " + xml);

						try {
							LoginInfo loginInfo = getLoginInfoHome().findByPrimaryKey(lt.getPrimaryKey());
							loginInfo.setCreationType(USER_CREATION_TYPE);
							loginInfo.store();

						} catch (Exception e) {
							throw new RuntimeException("Failed to flag secure user registration", e);
						}

					} else {

						StringBuffer filename = new StringBuffer(user3.toLowerCase());
						filename.append("sunnan3");
						IdGenerator uidGenerator = IdGeneratorFactory.getUUIDGenerator();
						filename.append(uidGenerator.generateId());
						filename.append(".xml");

						encodeAndSendXML(xml, filename.toString(), ssn);
					}
				} catch (Exception e) {
					UnsentCitizenAccount unsent = getUnsentCitizenAccountHome().create();
					unsent.setLogin(lt);
					unsent.setKey(password);
					if (e.getMessage().length() > 1000) {
						unsent.setOriginalError(e.getMessage().substring(0, 1000));
					} else {
						unsent.setOriginalError(e.getMessage());
					}
					unsent.setFailedSendDate(new IWTimestamp().getTimestamp());

					unsent.store();
				}
			} else if (createUserMessage) {
				this.getMessageBusiness().createUserMessage(citizen, messageSubject, messageBody, sendLetter);
			}

		} catch (PasswordNotKnown e) {
			// e.printStackTrace();
			throw new IDOCreateException(e);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Changes a password for CitizenAccount for a user and sends a letter
	 * and/or email.
	 *
	 * @param loginTable
	 *            LoginTable of the user
	 * @param user
	 *            User
	 * @param newPassword
	 *            Password in plain text (not already encrypted)
	 * @param sendLetter
	 *            True if a letter should be sent else false
	 * @param sendEmail
	 *            True if an email should be sent else false
	 * @throws CreateException
	 *             If changing of the password failed.
	 */
	@Override
	public void changePasswordAndSendLetterOrEmail(IWUserContext iwuc, LoginTable loginTable, User user,
			String newPassword, boolean sendLetter) throws CreateException {
		if (sendMessageToBank() && sendToLandsbankinn()) {
			UserTransaction trans = null;
			try {
				trans = this.getSessionContext().getUserTransaction();
				trans.begin();

				int bankCount = loginTable.getBankCount();

				// encrypte new password
				String encryptedPassword = Encrypter.encryptOneWay(newPassword);
				// store new password
				loginTable.setUserPassword(encryptedPassword, newPassword);
				loginTable.setBankCount(bankCount + 1);
				loginTable.store();

				LoginInfo loginInfo = ((LoginInfoHome) IDOLookup.getHome(LoginInfo.class))
						.findByPrimaryKey(loginTable.getPrimaryKey());
				loginInfo.setFailedAttemptCount(0);
				loginInfo.setAccessClosed(false);
				loginInfo.setAccountEnabled(true);
				loginInfo.store();

				try {
					String pageLink = getIWApplicationContext().getApplicationSettings()
							.getProperty(BANK_SENDER_PAGELINK);
					String logoLink = getIWApplicationContext().getApplicationSettings()
							.getProperty(BANK_SENDER_LOGOLINK);
					String user3 = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_TYPE);
					String user3version = getIWApplicationContext().getApplicationSettings()
							.getProperty(BANK_SENDER_TYPE_VERSION, "001");

					String xml = getXML(
							new Name(user.getFirstName(), user.getMiddleName(), user.getLastName()).getName(),
							loginTable.getUserLogin(), newPassword, pageLink, logoLink, Integer.toString(bankCount),
							user.getPersonalID(), user3, user3version);

					SendLoginDataBusiness send_data = getServiceInstance(SendLoginDataBusiness.class);

					send_data.send(xml);

					// System.out.println("xml (forgotten) = " + xml);

					loginInfo.setCreationType(USER_CREATION_TYPE);
					loginInfo.store();
				} catch (Exception e) {
					UnsentCitizenAccount unsent = getUnsentCitizenAccountHome().create();
					unsent.setLogin(loginTable);
					unsent.setKey(newPassword);
					if (e.getMessage().length() > 1000) {
						unsent.setOriginalError(e.getMessage().substring(0, 1000));
					} else {
						unsent.setOriginalError(e.getMessage());
					}
					unsent.setFailedSendDate(new IWTimestamp().getTimestamp());

					unsent.store();
				}

				trans.commit();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				// e.printStackTrace();
				if (trans != null) {
					try {
						trans.rollback();
					} catch (SystemException se) {
						se.printStackTrace();
					}
				}
				throw new CreateException("There was an error changing the password. Message was: " + e.getMessage());
			}
		} else {
			super.changePasswordAndSendLetterOrEmail(iwuc, loginTable, user, newPassword, sendLetter);
		}
	}

	private boolean sendToLandsbankinn() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(USE_LANDSBANKINN, false);
	}

	private String getXML(String name, String login, String password, String pageLink, String logo, String xkey,
			String user1, String user3, String user3version) {

		String pin = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_PIN);

		String mayor = getIWApplicationContext().getApplicationSettings().getProperty(CITIZEN_MAYOR_NAME);
		String signature = getIWApplicationContext().getApplicationSettings().getProperty(CITIZEN_MAYOR_SIGNATURE_URL);

		String definitionName = "idega.is";
		String acct = pin + user1;
		if (user3version == null || user3version.equals("")) {
			user3version = "001";
		}
		user3 = user3 + "-" + user3version;
		String user4 = acct + xkey;

		String encoding = /* sendToLandsbankinn() ? "UTF-8" : */"iso-8859-1";

		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"");
		xml.append(encoding);
		xml.append("\"?>\n");
		xml.append("<!DOCTYPE XML-S SYSTEM \"XML-S.dtd\"[]>\n");
		xml.append("<XML-S>\n");
		xml.append("\t<Statement Acct=\"");
		xml.append(acct);
		xml.append("\" Date=\"");
		xml.append(IWTimestamp.RightNow().getDateString("yyyy/MM/dd"));
		xml.append("\" XKey=\"");
		xml.append(xkey);
		xml.append("\">\n");
		xml.append("\t\t<?bgls.BlueGill.com DefinitionName=");
		xml.append(definitionName);
		xml.append("?>\n");
		xml.append("\t\t<?bgls.BlueGill.com User1=");
		xml.append(user1);
		xml.append("?>\n");
		xml.append("\t\t<?bgls.BlueGill.com User3=");
		xml.append(user3);
		xml.append("?>\n");
		xml.append("\t\t<?bgls.BlueGill.com User4=");
		xml.append(user4);
		xml.append("?>\n");
		xml.append("\t\t<Section Name=\"IDEGA\" Occ=\"1\">\n");
		xml.append("\t\t\t<Field Name=\"Company\">");
		xml.append(false);
		xml.append("</Field>\n");
		xml.append("\t\t\t<Field Name=\"Name\">");
		xml.append(name);
		xml.append("</Field>\n");
		xml.append("\t\t\t<Field Name=\"UserName\">");
		xml.append(login);
		xml.append("</Field>\n");
		xml.append("\t\t\t<Field Name=\"Password\">");
		xml.append(password);
		xml.append("</Field>\n");
		xml.append("\t\t\t<Field Name=\"PageLink\">");
		if (pageLink != null)
			xml.append(pageLink);
		xml.append("</Field>\n");
		xml.append("\t\t\t<Field Name=\"Logo\">");
		if (logo != null)
			xml.append(logo);
		xml.append("</Field>\n");
		xml.append("\t\t\t<Field Name=\"Mayor\">");
		if (mayor != null)
			xml.append(mayor);
		xml.append("</Field>\n");
		xml.append("\t\t\t<Field Name=\"Signature\">");
		if (signature != null)
			xml.append(signature);
		xml.append("</Field>\n");
		xml.append("\t\t</Section>\n");
		xml.append("\t</Statement>\n");
		xml.append("</XML-S>");

		return xml.toString();
	}

	private void encodeAndSendXML(String xml, String filename, String personalID) {
		String userId = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_USER_ID);

		try {
			File file = FileUtil.getFileFromWorkspace(
					getResourceRealPath(getBundle(getIWApplicationContext()), null) + "deploy_client.wsdd");

			EngineConfiguration config = new FileProvider(new FileInputStream(file));
			BirtingakerfiWSLocator locator = new BirtingakerfiWSLocator(config);
			BirtingakerfiWSSoap_PortType port = locator.getBirtingakerfiWSSoap(new URL(SERVICE_URL));

			Stub stub = (Stub) port;
			stub._setProperty(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
			stub._setProperty(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
			stub._setProperty(WSHandlerConstants.USER, userId);
			stub._setProperty(WSHandlerConstants.PW_CALLBACK_CLASS, this.getClass().getName());

			port.sendaSkra(filename, Base64.encode(xml.getBytes()), personalID);
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private IWBundle getBundle(IWApplicationContext iwac) {
		return iwac.getIWMainApplication().getBundle(getBundleIdentifier());
	}

	protected String getResourceRealPath(IWBundle iwb, Locale locale) {
		if (locale != null) {
			return iwb.getResourcesRealPath(locale) + "/";
		} else {
			return iwb.getResourcesRealPath() + "/";
		}
	}

	@Override
	public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
		String userId = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_USER_ID);
		String passwd = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_USER_PASSWORD);

		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof WSPasswordCallback) {
				WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
				if (pc.getIdentifier().equals(userId)) {
					pc.setPassword(passwd);
				}
			} else {
				throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
			}
		}
	}

	@Override
	public boolean sendMessageToBank() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(BANK_SEND_REGISTRATION, false);
	}

	private LoginInfoHome getLoginInfoHome() {
		try {
			return (LoginInfoHome) IDOLookup.getHome(LoginInfo.class);
		} catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	private UnsentCitizenAccountHome getUnsentCitizenAccountHome() {
		try {
			return (UnsentCitizenAccountHome) IDOLookup.getHome(UnsentCitizenAccount.class);
		} catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	@Override
	public void sendLostPasswordMessage(User citizen, String login, String password)
			throws RemoteException, CreateException {
		String messageBody = this.getAcceptMessageBody(citizen, login, password);
		String messageSubject = this.getAcceptMessageSubject(citizen);

		this.getMessageBusiness().createUserMessage(null, citizen, null, null, messageSubject, messageBody, messageBody,
				false, null, false, false);

		this.getMessageBusiness().createPasswordMessage(citizen, login, password);
	}

	protected String getAcceptMessageBody(User owner, String login, String password) {
		IWResourceBundle iwrb = this.getIWResourceBundleForUser(owner);

		Object[] arguments = { owner.getName(), login, password, getApplicationLoginURL() };
		String body = iwrb.getLocalizedString("lost.pass.body",
				"Dear mr./ms./mrs. {0}\n\nYour can now login to the system with the following login information:\n\nUserName: {1}\nPassword: {2}\n\nYou can log on via: {3}.");
		return MessageFormat.format(body, arguments);

	}

	@Override
	public String getAcceptMessageSubject(User owner) {
		IWResourceBundle iwrb = this.getIWResourceBundleForUser(owner);
		return iwrb.getLocalizedString("lost.pass.subj", "New password");
	}

}