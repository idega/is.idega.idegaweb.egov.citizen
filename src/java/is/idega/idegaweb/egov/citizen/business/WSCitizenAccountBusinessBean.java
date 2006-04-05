package is.idega.idegaweb.egov.citizen.business;

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

import se.idega.idegaweb.commune.account.citizen.business.CitizenAccountBusiness;
import se.idega.idegaweb.commune.account.citizen.business.CitizenAccountBusinessBean;
import se.idega.idegaweb.commune.account.data.AccountApplication;

import com.idega.core.accesscontrol.business.LoginCreateException;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.PasswordNotKnown;
import com.idega.data.IDOCreateException;
import com.idega.user.data.User;
import com.idega.util.FileUtil;
import com.idega.util.IWTimestamp;
import com.idega.xml.XMLDocType;
import com.idega.xml.XMLDocument;
import com.idega.xml.XMLElement;
import com.idega.xml.XMLOutput;

public class WSCitizenAccountBusinessBean extends CitizenAccountBusinessBean
		implements WSCitizenAccountBusiness, CitizenAccountBusiness, CallbackHandler {

	protected static final String SEND_REGISTRATION_TO_BANK = "SEND_REGISTRATION_TO_BANK";
	
	protected static final String SERVICE_URL = "https://ws-test.isb.is/adgerdirv1/birtingakerfi.asmx";
	
	private static final String TEST_USER_ID = "idegatest";
	
	private static final String TEST_USER_PW = "iwccJi432s";
	
	private static final String TEST_USER_PIN = "0904649069";

	private static final String XML_ROOT = "XML-S";
	
	private static final String XML_STATEMENT = "Statement";
	
	private static final String XML_STATEMENT_ACCT = "Acct";
	
	private static final String XML_STATEMENT_DATE = "Date";
	
	private static final String XML_STATEMENT_XKEY = "XKey";
	
	private static final String XML_BGLS = "?bgls.Bluegill.com";

	private static final String XML_BGLS_DEFINITION = "DefinitionName";

	private static final String XML_BGLS_USER1 = "User1";

	private static final String XML_BGLS_USER3 = "User3";

	private static final String XML_BGLS_USER4 = "User4";
	
	private static final String XML_SECTION = "Section";

	private static final String XML_SECTION_NAME = "Name";

	private static final String XML_SECTION_OCC = "Occ";
	
	private static final String XML_FIELD = "Field";

	private static final String XML_FIELD_NAME = "NAME";
	
	private static final String XML_FIELD_NAME_USERNAME = "UserName";

	private static final String XML_FIELD_NAME_PASSWORD = "Password";

	private static final String XML_FIELD_NAME_PAGELINK = "PageLink";

	private static final String XML_FIELD_NAME_LOGO = "Logo";

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
	protected void createLoginAndSendMessage(AccountApplication theCase,
			boolean createUserMessage, boolean createPasswordMessage,
			boolean sendEmail) throws RemoteException, CreateException,
			LoginCreateException {
		boolean sendLetter = false;
		LoginTable lt;
		String login;
		User citizen;
		citizen = theCase.getOwner();
		lt = getUserBusiness().generateUserLogin(citizen);
		login = lt.getUserLogin();
		try {
			String password = lt.getUnencryptedUserPassword();
			String messageBody = this.getAcceptMessageBody(theCase, login,
					password);
			String messageSubject = this.getAcceptMessageSubject();

			if (createPasswordMessage) {
				this.getMessageBusiness().createPasswordMessage(citizen, login,
						password);
			}

			createUserMessage = sendEmail;

			String sendMessageToBank = getIWApplicationContext()
					.getApplicationSettings().getProperty(
							SEND_REGISTRATION_TO_BANK, "false");
			
			if (!"false".equals(sendMessageToBank)) {
				String xml = getXML(login, password, "pagelink", "logolink");
				encodeAndSendXML(xml, "filename", citizen.getPersonalID());
			}
			else if (createUserMessage) {
				this.getMessageBusiness().createUserMessage(citizen,
						messageSubject, messageBody, sendLetter);
			}
			
		} catch (PasswordNotKnown e) {
			// e.printStackTrace();
			throw new IDOCreateException(e);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private String getXML(String login, String password, String pageLink, String logo) {
		/*
		<?xml version="1.0" encoding="iso-8859-1"?>
		<!DOCTYPE XML-S SYSTEM "XML-S.dtd"[]>
		<XML-S>
			<Statement Acct="56789012341234567890" Date="2006/03/30" XKey="267">
				<?bgls.BlueGill.com DefinitionName=idega.is?>
				<?bgls.BlueGill.com User1=1234567890?>
				<?bgls.BlueGill.com User3=ABCD-001?>
				<?bgls.BlueGill.com User4=56789012341234567890267?>
				<Section Name="IDEGA" Occ="1">
					<Field Name="UserName">1234567890</Field>
					<Field Name="Password">1234abcd</Field>
					<Field Name="PageLink">http://www.sunnan3.is/</Field>
					<Field Name="Logo">http://www.sunnan3.is/logo.jpg</Field>
		        </Section>
	         </Statement>
	     </XML-S>
		 */
		
		XMLDocType type = new XMLDocType("XML-S", "XML-S.dtd");
		XMLDocument doc = new XMLDocument(new XMLElement(XML_ROOT), type);

		XMLElement root = doc.getRootElement();
		XMLElement statement = new XMLElement(XML_STATEMENT);
		statement.setAttribute(XML_STATEMENT_ACCT, "1233453");
		statement.setAttribute(XML_STATEMENT_DATE, IWTimestamp.RightNow().getDateString("yyyy/MM/dd"));
		statement.setAttribute(XML_STATEMENT_XKEY, "123");
		root.addContent(statement);
		
		//XMLElement bgls1 = new XMLElement(XML_BGLS);
		
		XMLElement section = new XMLElement(XML_SECTION);
		section.setAttribute(XML_SECTION_NAME, "IDEGA");
		section.setAttribute(XML_SECTION_OCC, "1");
		statement.addContent(section);
		
		XMLElement field1 = new XMLElement(XML_FIELD);
		field1.setAttribute(XML_FIELD_NAME, XML_FIELD_NAME_USERNAME);
		field1.addContent(login);

		XMLElement field2 = new XMLElement(XML_FIELD);
		field2.setAttribute(XML_FIELD_NAME, XML_FIELD_NAME_PASSWORD);
		field2.addContent(password);

		XMLElement field3 = new XMLElement(XML_FIELD);
		field3.setAttribute(XML_FIELD_NAME, XML_FIELD_NAME_PAGELINK);
		field3.addContent(pageLink);

		XMLElement field4 = new XMLElement(XML_FIELD);
		field4.setAttribute(XML_FIELD_NAME, XML_FIELD_NAME_LOGO);
		field4.addContent(logo);

		section.addContent(field1);
		section.addContent(field2);
		section.addContent(field3);
		section.addContent(field4);
		
		try {
			XMLOutput output = new XMLOutput();
			output.setLineSeparator(System.getProperty("line.separator"));
			output.setTextNormalize(true);
			output.setEncoding("ISO-8859-1");
			output.setSkipEncoding(false);
			return output.outputString(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void encodeAndSendXML(String xml, String filename, String personalID) {
		try {
			StringBuffer file = new StringBuffer(getBundle().getResourcesRealPath());
			file.append(FileUtil.getFileSeparator());
			file.append("client_deploy.wsdd");
			
			EngineConfiguration config = new FileProvider(new FileInputStream(file.toString()));
			BirtingakerfiWSLocator locator = new BirtingakerfiWSLocator(config);
			BirtingakerfiWSSoap_PortType port = locator
					.getBirtingakerfiWSSoap(new URL(SERVICE_URL));

			Stub stub = (Stub) port;
			stub._setProperty(WSHandlerConstants.ACTION,
					WSHandlerConstants.USERNAME_TOKEN);
			stub._setProperty(WSHandlerConstants.PASSWORD_TYPE,
					WSConstants.PW_TEXT);
			stub._setProperty(WSHandlerConstants.USER, TEST_USER_ID);
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
		}
	}

	public void handle(Callback[] callbacks)
			throws UnsupportedCallbackException {

		for (int i = 0; i < callbacks.length; i++) {
			System.out.println("callbacks.class = "
					+ callbacks[i].getClass().getName());
			if (callbacks[i] instanceof WSPasswordCallback) {
				WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
				/*
				 * here call a function/method to lookup the password for the
				 * given identifier (e.g. a user name or keystore alias) e.g.:
				 * pc.setPassword(passStore.getPassword(pc.getIdentfifier)) for
				 * testing we supply a fixed name/fixed key here.
				 */
				if (pc.getIdentifer().equals(TEST_USER_ID)) {
					pc.setPassword(TEST_USER_PW);
				}
			} else {
				throw new UnsupportedCallbackException(callbacks[i],
						"Unrecognized Callback");
			}
		}
	}

	public static void main(String args[]) {
		WSCitizenAccountBusinessBean bean = new WSCitizenAccountBusinessBean();
		String xml = bean.getXML("1234567890","1234abcd", "http://www.sunnan3.is/", "http://www.sunnan3.is/logo.jpg");
		System.out.println("xml = " + xml);
	}
}