package is.idega.idegaweb.egov.citizen.wsclient.landsbankinn;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import com.ibm.icu.text.NumberFormat;
import com.idega.business.IBOServiceBean;
import com.idega.idegaweb.IWMainApplication;
import com.idega.util.CypherText;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.BaseException;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public class SendLoginDataBusinessBean extends IBOServiceBean implements SendLoginDataBusiness {

	private static final long serialVersionUID = 3509433414652752725L;

	private static Logger logger = Logger.getLogger(SendLoginDataBusinessBean.class.getName());

	private XStream login_xstream;
	private XStream logout_xstream;
	private XStream login_resp_xstream;
	private XStream err_xstream;
	private XStream send_data_xstream;
	private XStream send_data_response_err_xstream;
	private XStream verify_xstream;
	private XStream verify_resp_xstream;


	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>";
	private static final String DEFAULT_SERVICE_URL = "https://b2b.fbl.is/lib2b.dll?processXML";
	private static final String LANDSBANKINN_SERVICE_URL = "LANDSBANKINN_SERVICE_URL";

	private static final String landsbankinn_service_rvk_login_app_key = "landsbankinn.rvk.ws.login";
	private static final String landsbankinn_service_rvk_pass_app_key = "landsbankinn.rvk.ws.pass";

	private static final String ck = "8CTW4ktdt1oVAdve4I2GGTpyDkP4ROuztfxRcBzo2xTT8CGFqhMFxMrbtmCH1c3yUz8qYV9LRd8XTPzZj9YMLyP16eJyWOrZWKgQ";

	@Override
	public boolean verifyBankAccount(String bankNumber, String ledger, String accountNumber, String personalID) {
		if (bankNumber == null || ledger == null || accountNumber == null) {
			return false;
		}

		if (bankNumber.length() > 4 || ledger.length() > 2 || accountNumber.length() > 6) {
			return false;
		}

		int bankNumberInt = 0;
		int ledgerInt = 0;
		int accountNumberInt = 0;

		try {
			bankNumberInt = Integer.parseInt(bankNumber);
			ledgerInt = Integer.parseInt(ledger);
			accountNumberInt = Integer.parseInt(accountNumber);
		} catch (Exception e) {
			return false;
		}

		if (bankNumberInt == 9797 && ledgerInt == 97 && accountNumberInt == 979797) {
			return true;
		}



		NumberFormat f = NumberFormat.getIntegerInstance();
		f.setGroupingUsed(false);

		if (bankNumber.length() < 4) {
			f.setMinimumIntegerDigits(4);
			f.setMaximumIntegerDigits(4);

			bankNumber = f.format(bankNumberInt);
		}

		if (ledger.length() < 2) {
			f.setMinimumIntegerDigits(2);
			f.setMaximumIntegerDigits(2);

			ledger = f.format(ledgerInt);
		}

		if (accountNumber.length() < 6) {
			f.setMinimumIntegerDigits(6);
			f.setMaximumIntegerDigits(6);

			accountNumber = f.format(accountNumberInt);
		}

		String session_id = login();

		if (session_id == null) {
			throw new RuntimeException("Session id couldn't be retrieved while logging in");
		}

		VerifyBankAccount verify = new VerifyBankAccount();
		BankAccount account = new BankAccount();
		account.setBank(bankNumber);
		account.setAccount_type(ledger);
		account.setAccount_number(accountNumber);
		verify.setSession_id(session_id);
		verify.setPersonal_id(personalID);
		verify.setBank_account(account);

		PostMethod response = sendXMLData(verify, getVerifyBankAccountRequestXStream());

		InputStream respStream = null;

		String temp = null;
		try {
			temp = response.getResponseBodyAsString();
			respStream = response.getResponseBodyAsStream();
			VerifyBankAccountResponse resp = (VerifyBankAccountResponse) getVerifyBankAccountResponseXStream().fromXML(response.getResponseBodyAsStream());

			//Yes, it's supposed to be 0!! Ask Landsbankinn why.
			if (resp.getAccountExists() != null && ("TRUE".equalsIgnoreCase(resp.getAccountExists()) || "0".equals(resp.getAccountExists()))) {
				return true;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "error = " + temp, e);
			if (e instanceof BaseException) {
				handleResponseParseException(respStream, (BaseException) e);
			}
		} finally {
			response.releaseConnection();
		}

		return false;
	}

	@Override
	public void send(String xml_str) {

		if (xml_str == null) {
			throw new NullPointerException("XML data not provided");
		}

		String session_id = login();

		if (session_id == null) {
			throw new RuntimeException(
					"Session id couldn't be retrieved while logging in");
		}

		SendingInData data = new SendingInData();
		data.setSessionId(session_id);
		try {
			data.setData(new String(xml_str.getBytes("iso-8859-1"), "iso-8859-1"));
		}
		catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			data.setData(xml_str);
		}

		PostMethod response = sendXMLData(data, getSendDataXStream());

		SendingInDataResponse err = null;

		try {
			err = (SendingInDataResponse) getSendDataResponseErrorMessageXStream()
					.fromXML(response.getResponseBodyAsStream());

			if (err.getErrorMsg() == null && err.getErrorNumber() == null) {
				return;
			}

			logger.warning("ERROR SENDING TO LANDSBANKI: err = " + err.getErrorMsg() + ", errNumber = " + err.getErrorNumber() + ", xml =\n" + xml_str);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception while parsing sendXMLData response", e);

			RuntimeException ex = new RuntimeException("Exception while parsing sendXMLData response");
			ex.initCause(e);

			throw ex;
		} finally {
			response.releaseConnection();
			logout(session_id);
		}

		throw new RuntimeException("Error got from response:\n - err msg: "
				+ err.getErrorMsg() + "\n - err number: "
				+ err.getErrorNumber());
	}

	protected String login() {
		String[] loginAndPass = getLoginAndPassword();
		LoginRequest req = new LoginRequest();
		req.setLoginName(loginAndPass[0]);
		req.setLoginPassword(loginAndPass[1]);

		PostMethod response = sendXMLData(req, getLoginRequestXStream());

		InputStream respStream = null;

		try {
			respStream = response.getResponseBodyAsStream();
			LoginResponse resp = (LoginResponse) getLoginResponseXStream().fromXML(response.getResponseBodyAsStream());
			return resp.getSessionId();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception while loging-in", e);

			if (e instanceof BaseException) {
				handleResponseParseException(respStream, (BaseException) e);
			}
		} finally {
			response.releaseConnection();
		}

		return null;
	}

	protected void handleResponseParseException(InputStream responseStream, BaseException e) {
		if (responseStream == null) {
			return;
		}

		try {
			GeneralErrorMessage err = (GeneralErrorMessage) getGeneralErrorMessageXStream().fromXML(responseStream);
			logger.log(Level.SEVERE, "Error msg got from response: " + err.getErrorMsg());
		} catch (BaseException e2) {
			logger.log(Level.SEVERE, "Error while parsing error message", e2);
		}
	}

	protected void logout(String session_id) {
		if (session_id == null) {
			logger.log(Level.WARNING, "Null was provided as session id for logout.");
			return;
		}

		LogoutRequest req = new LogoutRequest();
		req.setSessionId(session_id);

		PostMethod response = null;

		try {
			response = sendXMLData(req, getLogoutRequestXStream());
		} finally {
			response.releaseConnection();
		}
	}

	protected PostMethod sendXMLData(Object req, XStream xstream) {
		PostMethod post = new PostMethod(getIWMainApplication().getSettings().getProperty(LANDSBANKINN_SERVICE_URL, DEFAULT_SERVICE_URL));

		String data = null;
		try {
			data = XML_HEADER + xstream.toXML(req);
			post.setRequestBody(data);
			post.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");

			HttpClient client = new HttpClient();

			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			int result = client.executeMethod(post);

            // Display status code
           	logger.info("Response status code: " + result);

            // Display response
           	logger.info("Response body: " + post.getResponseBodyAsString());

            return post;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception while sending xml data:\n" + data, e);
			return null;
		}
	}

	protected synchronized XStream getGeneralErrorMessageXStream() {

		if (err_xstream == null) {

			XStream xstream = new XStream(new XppDriver(
					new XmlFriendlyReplacer("$", "_")));
			xstream.alias("LI_Villa", GeneralErrorMessage.class);
			xstream.aliasField("villa", GeneralErrorMessage.class,
					GeneralErrorMessage.error_number_field);
			xstream.aliasField("villubod", GeneralErrorMessage.class,
					GeneralErrorMessage.error_msg_field);
			xstream.aliasField("dags-mottekid", GeneralErrorMessage.class,
					GeneralErrorMessage.query_date_and_time_field);
			xstream.aliasField("dags_svarad", GeneralErrorMessage.class,
					GeneralErrorMessage.reply_completion_date_and_time_field);

			err_xstream = xstream;
		}

		return err_xstream;
	}

	protected synchronized XStream getSendDataResponseErrorMessageXStream() {

		if (send_data_response_err_xstream == null) {

			XStream xstream = new XStream(new XppDriver(
					new XmlFriendlyReplacer("$", "_")));

			xstream.alias("LI_Innsending_gagna_svar",
					SendingInDataResponse.class);
			xstream.aliasField("timi", SendingInDataResponse.class,
					SendingInDataResponse.time_field);
			xstream.aliasField("villa", SendingInDataResponse.class,
					SendingInDataResponse.error_number_field);
			xstream.aliasField("villubod", SendingInDataResponse.class,
					SendingInDataResponse.error_msg_field);
			xstream.aliasField("dags_mottekid", Time.class,
					Time.query_data_and_time_field);
			xstream.aliasField("dags_svarad", Time.class,
					Time.reply_data_and_time_field);

			send_data_response_err_xstream = xstream;
		}

		return send_data_response_err_xstream;
	}

	protected synchronized XStream getLoginResponseXStream() {

		if (login_resp_xstream == null || true) {

			XStream xstream = new XStream(new XppDriver(
					new XmlFriendlyReplacer("$", "_")));

			xstream.alias("LI_Innskra_svar", LoginResponse.class);
			xstream.aliasField("seta", LoginResponse.class,
					LoginResponse.session_id_field);
			xstream.alias("timi", Time.class);
			xstream.aliasField("timi", LoginResponse.class,
					LoginResponse.time_field);
			xstream.aliasField("dags_mottekid", Time.class,
					Time.query_data_and_time_field);
			xstream.aliasField("dags_svarad", Time.class,
					Time.reply_data_and_time_field);

			login_resp_xstream = xstream;
		}

		return login_resp_xstream;
	}

	protected synchronized XStream getLoginRequestXStream() {

		if (login_xstream == null) {

			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("$", "_")));

			xstream.alias("LI_Innskra", LoginRequest.class);
			xstream.aliasField("notandanafn", LoginRequest.class, LoginRequest.login_name_field);
			xstream.aliasField("lykilord", LoginRequest.class, LoginRequest.login_password_field);

			xstream.useAttributeFor(LoginRequest.class, LoginRequest.xsd_field);
			xstream.aliasAttribute(LoginRequest.class, LoginRequest.xsd_field, "xmlns:xsi");
			xstream.useAttributeFor(LoginRequest.class, LoginRequest.xsi_field);
			xstream.aliasAttribute(LoginRequest.class, LoginRequest.xsi_field, "xsi:noNamespaceSchemeLocation");
			xstream.useAttributeFor(LoginRequest.class, LoginRequest.version_field);

			login_xstream = xstream;
		}

		return login_xstream;
	}

	protected synchronized XStream getLogoutRequestXStream() {

		if (logout_xstream == null) {

			XStream xstream = new XStream(new XppDriver(
					new XmlFriendlyReplacer("$", "_")));

			xstream.alias("LI_Utskra", LogoutRequest.class);
			xstream.aliasField("seta", LogoutRequest.class,
					LogoutRequest.session_id_field);

			xstream.useAttributeFor(LogoutRequest.class,
					LogoutRequest.xsi_field);
			xstream.aliasAttribute(LogoutRequest.class,
					LogoutRequest.xsi_field, "xmlns:xsi");
			xstream.useAttributeFor(LogoutRequest.class,
					LogoutRequest.xsi_no_nmspc_field);
			xstream.aliasAttribute(LogoutRequest.class,
					LogoutRequest.xsi_no_nmspc_field,
					"xsi:noNamespaceSchemaLocationi");
			xstream.useAttributeFor(LogoutRequest.class,
					LogoutRequest.version_field);

			logout_xstream = xstream;
		}

		return logout_xstream;
	}

	protected synchronized XStream getSendDataXStream() {

		if (send_data_xstream == null) {

			XStream xstream = new XStream(new XppDriver(
					new XmlFriendlyReplacer("$", "_")));

			xstream.alias("LI_Innsending_gagna", SendingInData.class);
			xstream.aliasField("seta", SendingInData.class,
					SendingInData.session_id_field);
			xstream.aliasField("tegund", SendingInData.class,
					SendingInData.data_type_field);
			xstream.aliasField("strengur", SendingInData.class,
					SendingInData.data_field);

			xstream.useAttributeFor(SendingInData.class,
					SendingInData.xsd_field);
			xstream.aliasAttribute(SendingInData.class,
					SendingInData.xsd_field, "xmlns:xsd");
			xstream.useAttributeFor(SendingInData.class,
					SendingInData.xsi_field);
			xstream.aliasAttribute(SendingInData.class,
					SendingInData.xsi_field, "xmlns:xsi");
			xstream.useAttributeFor(SendingInData.class,
					SendingInData.version_field);

			send_data_xstream = xstream;
		}

		return send_data_xstream;
	}

	protected synchronized XStream getVerifyBankAccountRequestXStream() {
		if (verify_xstream == null) {
			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("$", "_")));

			xstream.alias("LI_Fyrirspurn_er_reikningur_til", VerifyBankAccount.class);
			xstream.aliasField("seta", VerifyBankAccount.class, VerifyBankAccount.session_id_field);
			xstream.aliasField("kennitala", VerifyBankAccount.class, VerifyBankAccount.personal_id_field);
			xstream.aliasField("reikningur", VerifyBankAccount.class, VerifyBankAccount.bank_account_field);
			xstream.aliasField("utibu", BankAccount.class, BankAccount.bank_field);
			xstream.aliasField("hb", BankAccount.class, BankAccount.account_type_field);
			xstream.aliasField("reikningsnr", BankAccount.class, BankAccount.account_number_field);

			xstream.useAttributeFor(VerifyBankAccount.class, VerifyBankAccount.xsi_field);
			xstream.aliasAttribute(VerifyBankAccount.class, VerifyBankAccount.xsi_field, "xmlns:xsi");
			xstream.useAttributeFor(VerifyBankAccount.class, VerifyBankAccount.xsi_no_nmspc_field);
			xstream.aliasAttribute(VerifyBankAccount.class, VerifyBankAccount.xsi_no_nmspc_field, "xsi:noNamespaceSchemeLocation");
			xstream.useAttributeFor(VerifyBankAccount.class, VerifyBankAccount.version_field);

			verify_xstream = xstream;
		}

		return verify_xstream;
	}

	protected synchronized XStream getVerifyBankAccountResponseXStream() {
		if (verify_resp_xstream == null || true) {
			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("$", "_")));

			xstream.alias("LI_Fyrirspurn_er_reikningur_til_svar", VerifyBankAccountResponse.class);
			xstream.aliasField("er_til", VerifyBankAccountResponse.class, VerifyBankAccountResponse.exists_field);
			xstream.alias("timi", Time.class);
			xstream.aliasField("timi", VerifyBankAccountResponse.class, VerifyBankAccountResponse.time_field);
			xstream.aliasField("dags_mottekid", Time.class, Time.query_data_and_time_field);
			xstream.aliasField("dags_svarad", Time.class, Time.reply_data_and_time_field);

			verify_resp_xstream = xstream;
		}

		return verify_resp_xstream;
	}


	protected String[] getLoginAndPassword() {

		IWMainApplication iwma = IWMainApplication
				.getDefaultIWMainApplication();

		if (iwma != null) {

			String login = iwma.getSettings().getProperty(
					landsbankinn_service_rvk_login_app_key);
			String pass = iwma.getSettings().getProperty(
					landsbankinn_service_rvk_pass_app_key);

			if (login == null || pass == null) {
				return null;
			}

			CypherText ct = new CypherText();
			login = ct.doDeCypher(login, ck);
			pass = ct.doDeCypher(pass, ck);

			return new String[] { login, pass };
		}

		return null;
	}
}