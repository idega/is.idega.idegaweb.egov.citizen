package is.idega.idegaweb.egov.citizen.business;

import is.idega.idegaweb.egov.citizen.IWBundleStarter;
import is.idega.idegaweb.egov.citizen.business.landsbankinn.SendLoginDataBusiness;
import is.idega.idegaweb.egov.citizen.data.AccountApplication;
import is.idega.idegaweb.egov.citizen.wsclient.BirtingakerfiWSLocator;
import is.idega.idegaweb.egov.citizen.wsclient.BirtingakerfiWSSoap_PortType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.rpc.ServiceException;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.encoding.Base64;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;

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
import com.idega.user.data.User;
import com.idega.util.FileUtil;
import com.idega.util.IWTimestamp;

public class WSCitizenAccountBusinessBean extends CitizenAccountBusinessBean implements WSCitizenAccountBusiness, CitizenAccountBusiness, CallbackHandler {

	private static final long serialVersionUID = -8824721140289363380L;

	protected static final String BANK_SEND_REGISTRATION = "BANK_SEND_REGISTRATION";
	
	protected static final String USE_LANDSBANKINN = "USE_LANDSBANKINN";

	protected static final String BANK_SENDER_PIN = "BANK_SENDER_PIN";

	protected static final String BANK_SENDER_USER_ID = "BANK_SENDER_USER_ID";

	protected static final String BANK_SENDER_USER_PASSWORD = "BANK_SENDER_USER_PW";

	protected static final String BANK_SENDER_PAGELINK = "BANK_SENDER_PAGELINK";

	protected static final String BANK_SENDER_LOGOLINK = "BANK_SENDER_LOGOLINK";

	protected static final String BANK_SENDER_TYPE = "BANK_SENDER_TYPE";
	
	protected static final String BANK_SENDER_TYPE_VERSION = "BANK_SENDER_TYPE_VERSION";

	protected static final String USER_CREATION_TYPE = "RVKLB";
	

	protected static final String SERVICE_URL = "https://ws.isb.is/adgerdirv1/birtingakerfi.asmx";

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
	@Override
	protected void createLoginAndSendMessage(AccountApplication theCase, boolean createUserMessage, boolean createPasswordMessage, boolean sendEmail, boolean sendSnailMail) throws RemoteException, CreateException, LoginCreateException {

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
				String pageLink = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_PAGELINK);
				String logoLink = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_LOGOLINK);
				String ssn = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_PIN);
				String user3 = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_TYPE);
				String user3version = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_TYPE_VERSION, "001");

				String xml = getXML(login, password, pageLink, logoLink, sendUsingLandsbankan() ? "1" : citizen.getPrimaryKey().toString(), citizen.getPersonalID(), user3, user3version);
				
				if(sendUsingLandsbankan()) {
					
					SendLoginDataBusiness send_data = (SendLoginDataBusiness)getServiceInstance(SendLoginDataBusiness.class);
					
					send_data.send(xml);
					
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
			}
			else if (createUserMessage) {
				this.getMessageBusiness().createUserMessage(citizen, messageSubject, messageBody, sendLetter);
			}

		}
		catch (PasswordNotKnown e) {
			// e.printStackTrace();
			throw new IDOCreateException(e);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private boolean sendUsingLandsbankan() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(USE_LANDSBANKINN, false);
	}

	private String getXML(String login, String password, String pageLink, String logo, String xkey, String user1, String user3, String user3version) {
		
		String pin = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_PIN);

		String definitionName = "idega.is";
		String acct = pin + user1;
		if (user3version == null || user3version.equals("")) {
			user3version = "001";
		}
		user3 = user3 + "-" + user3version;
		String user4 = acct + xkey;
		
		String encoding = sendUsingLandsbankan() ? "UTF-8" : "iso-8859-1";

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
		xml.append("\t\t\t<Field Name=\"UserName\">");
		xml.append(login);
		xml.append("</Field>\n");
		xml.append("\t\t\t<Field Name=\"Password\">");
		xml.append(password);
		xml.append("</Field>\n");
		xml.append("\t\t\t<Field Name=\"PageLink\">");
		
		if(pageLink != null)
			xml.append(pageLink);
		xml.append("</Field>\n");
		xml.append("\t\t\t<Field Name=\"Logo\">");
		if(logo != null)
			xml.append(logo);
		xml.append("</Field>\n");
		xml.append("\t\t</Section>\n");
		xml.append("\t</Statement>\n");
		xml.append("</XML-S>");

		return xml.toString();
	}
	
	private void encodeAndSendXML(String xml, String filename, String personalID) {
		String userId = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_USER_ID);

		try {
			StringBuffer file = new StringBuffer(this.getIWMainApplication().getBundle(IWBundleStarter.IW_BUNDLE_IDENTIFIER).getResourcesRealPath());
			file.append(FileUtil.getFileSeparator());
			// Do not change the name of this file because the stupid autodeployer will start it up otherwise.
			file.append("deploy_client.wsdd");

			EngineConfiguration config = new FileProvider(new FileInputStream(file.toString()));
			BirtingakerfiWSLocator locator = new BirtingakerfiWSLocator(config);
			BirtingakerfiWSSoap_PortType port = locator.getBirtingakerfiWSSoap(new URL(SERVICE_URL));

			Stub stub = (Stub) port;
			stub._setProperty(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
			stub._setProperty(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
			stub._setProperty(WSHandlerConstants.USER, userId);
			stub._setProperty(WSHandlerConstants.PW_CALLBACK_CLASS, this.getClass().getName());

			port.sendaSkra(filename, Base64.encode(xml.getBytes()), personalID);
		}
		catch (ServiceException e) {
			e.printStackTrace();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
		String userId = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_USER_ID);
		String passwd = getIWApplicationContext().getApplicationSettings().getProperty(BANK_SENDER_USER_PASSWORD);

		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof WSPasswordCallback) {
				WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
				if (pc.getIdentifer().equals(userId)) {
					pc.setPassword(passwd);
				}
			}
			else {
				throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
			}
		}
	}

	public boolean sendMessageToBank() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(BANK_SEND_REGISTRATION, false);
	}
	
	private LoginInfoHome getLoginInfoHome() {
		try {
			return (LoginInfoHome) IDOLookup.getHome(LoginInfo.class);
		}
		catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}