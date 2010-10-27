package is.idega.idegaweb.egov.citizen;

import is.idega.idegaweb.egov.citizen.wsclient.landsbankinn.BankAccount;
import is.idega.idegaweb.egov.citizen.wsclient.landsbankinn.GeneralErrorMessage;
import is.idega.idegaweb.egov.citizen.wsclient.landsbankinn.LoginRequest;
import is.idega.idegaweb.egov.citizen.wsclient.landsbankinn.LoginResponse;
import is.idega.idegaweb.egov.citizen.wsclient.landsbankinn.LogoutRequest;
import is.idega.idegaweb.egov.citizen.wsclient.landsbankinn.SendingInData;
import is.idega.idegaweb.egov.citizen.wsclient.landsbankinn.SendingInDataResponse;
import is.idega.idegaweb.egov.citizen.wsclient.landsbankinn.Time;
import is.idega.idegaweb.egov.citizen.wsclient.landsbankinn.VerifyBankAccount;
import is.idega.idegaweb.egov.citizen.wsclient.landsbankinn.VerifyBankAccountResponse;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import com.idega.util.CypherText;
import com.idega.util.IWTimestamp;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.BaseException;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class TestClient {
	
	private XStream login_xstream;
	private XStream logout_xstream;
	private XStream login_resp_xstream;
	private XStream err_xstream;
	private XStream send_data_xstream;
	private XStream send_data_response_err_xstream;
	private XStream verify_xstream;
	private XStream verify_resp_xstream;

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private static final String ck = "8CTW4ktdt1oVAdve4I2GGTpyDkP4ROuztfxRcBzo2xTT8CGFqhMFxMrbtmCH1c3yUz8qYV9LRd8XTPzZj9YMLyP16eJyWOrZWKgQ";


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IWTimestamp dateNow = new IWTimestamp();
		dateNow.setAsDate();
		
		IWTimestamp currentDay = new IWTimestamp(new IWTimestamp().toSQLDateString());
		
		System.out.println("dateNow = " + dateNow.toString());
		System.out.println("currentDay = " + currentDay.toString());
		
		IWTimestamp now = new IWTimestamp();
		now.setAsTime();
		now.setHour(16);
		now.setMinute(11);
		
		IWTimestamp groupStamp = new IWTimestamp();
		groupStamp.setAsTime();
		groupStamp.setHour(16);
		groupStamp.setMinute(10);

		System.out.println("now = " + now.toString());
		System.out.println("groupStamp = " + groupStamp.toString());

		
		boolean isOpen = dateNow.isLaterThan(currentDay) || (dateNow.isEqualTo(currentDay) && now.isLaterThan(groupStamp));

		System.out.println("isOpen = " + isOpen);
		//TestClient client = new TestClient();
		//client.printOutXML();
	}

	/*private void printOutXML() {
		String bankNumber = "115";
		String ledger = "5";
		String accountNumber = "1234";
		NumberFormat f = NumberFormat.getIntegerInstance();
		f.setGroupingUsed(false);
		if (bankNumber.length() < 4) {
			f.setMinimumIntegerDigits(4);
			f.setMaximumIntegerDigits(4);

			bankNumber = f.format(Integer.parseInt(bankNumber));
		}
		
		if (ledger.length() < 2) {
			f.setMinimumIntegerDigits(2);
			f.setMaximumIntegerDigits(2);

			ledger = f.format(Integer.parseInt(ledger));
		}
		
		if (accountNumber.length() < 6) {
			f.setMinimumIntegerDigits(6);
			f.setMaximumIntegerDigits(6);

			accountNumber = f.format(Integer.parseInt(accountNumber));
		}

		System.out.println(bankNumber + "-" + ledger + "-" + accountNumber);
		
		
		String session_id = login();

		if (session_id == null) {
			throw new RuntimeException("Session id couldn't be retrieved while logging in");
		}

		VerifyBankAccount verify = new VerifyBankAccount();
		BankAccount account = new BankAccount();
		account.setBank("0115");
		account.setAccount_type("26");
		account.setAccount_number("000156");
		verify.setSession_id(session_id);
		verify.setPersonal_id("6210779029");
		verify.setBank_account(account);
		
		//String data = XML_HEADER + getVerifyBankAccountRequestXStream().toXML(verify);
		//System.out.println(data);
		
		PostMethod response = sendXMLData(verify, getVerifyBankAccountRequestXStream());

		InputStream respStream = null;
		
		String temp = null;
		try {
			temp = response.getResponseBodyAsString();
			respStream = response.getResponseBodyAsStream();
			VerifyBankAccountResponse resp = (VerifyBankAccountResponse) getVerifyBankAccountResponseXStream().fromXML(response.getResponseBodyAsStream());
			//return resp.getSessionId();
			System.out.println("sent = " + resp.getTime().getQueryDataAndTime());
			System.out.println("response = " + resp.getTime().getReplyDataAndTime());
			System.out.println("exists = " + resp.getAccountExists());
		}
		catch (BaseException e) {
			e.printStackTrace();
			System.out.println("error = " + temp);
			handleResponseParseException(respStream, e);
		}
		catch (Exception e) {
			e.printStackTrace();

		}
		finally {
			response.releaseConnection();
		}

	}*/
	
	/*private void sendPassword() {
		String login = "0610703899";

		String password = "lykilordid";

		try {
			String pageLink = "http://rafraen.reykjavik.is/pages/";
			String logoLink = "	http://rafraen.reykjavik.is/content/files/public/style/images/rvk-thjonustafyrirthig.gif";
			String user3 = "RVKP";
			String user3version = "004";

			String xml = getXML(login, password, pageLink, logoLink,
					"1", "0610703899", user3,
					user3version);

			send(xml);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}*/
	
	/*private String getXML(String login, String password, String pageLink,
			String logo, String xkey, String user1, String user3,
			String user3version) {

		String pin = "4404720609";

		String definitionName = "idega.is";
		String acct = pin + user1;
		if (user3version == null || user3version.equals("")) {
			user3version = "001";
		}
		user3 = user3 + "-" + user3version;
		String user4 = acct + xkey;

		String encoding = "UTF-8";

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

		if (pageLink != null)
			xml.append(pageLink);
		xml.append("</Field>\n");
		xml.append("\t\t\t<Field Name=\"Logo\">");
		if (logo != null)
			xml.append(logo);
		xml.append("</Field>\n");
		xml.append("\t\t</Section>\n");
		xml.append("\t</Statement>\n");
		xml.append("</XML-S>");

		return xml.toString();
	}*/

	public void send(String xml_str) {

		if (xml_str == null) {
			throw new NullPointerException("XML data not provided");
		}

		String session_id = login();

		if (session_id == null) {
			throw new RuntimeException("Session id couldn't be retrieved while logging in");
		}

		SendingInData data = new SendingInData();
		data.setSessionId(session_id);
		data.setData(xml_str);

		PostMethod response = sendXMLData(data, getSendDataXStream());

		SendingInDataResponse err = null;

		try {
			err = (SendingInDataResponse) getSendDataResponseErrorMessageXStream().fromXML(response.getResponseBodyAsStream());

			if (err.getErrorMsg() == null && err.getErrorNumber() == null) {
				return;
			}
			
			System.out.println("ERROR SENDING TO LANDSBANKI");
			System.out.println("err = " + err.getErrorMsg());
			System.out.println("errNumber = " + err.getErrorNumber());
			System.out.println("xml = " + xml_str);

		}
		catch (Exception e) {
			e.printStackTrace();
			
			RuntimeException ex = new RuntimeException("Exception while parsing sendXMLData response");
			ex.initCause(e);

			throw ex;

		}
		finally {

			response.releaseConnection();
			logout(session_id);
		}

		throw new RuntimeException("Error got from response:\n - err msg: " + err.getErrorMsg() + "\n - err number: " + err.getErrorNumber());
	}

	protected String login() {

		String[] loginAndPass = getLoginAndPassword();
		LoginRequest req = new LoginRequest();
		req.setLoginName(loginAndPass[0]);
		req.setLoginPassword(loginAndPass[1]);

		PostMethod response = sendXMLData(req, getLoginRequestXStream());

		InputStream respStream = null;
		
		String temp = null;
		try {
			temp = response.getResponseBodyAsString();
			respStream = response.getResponseBodyAsStream();
			LoginResponse resp = (LoginResponse) getLoginResponseXStream().fromXML(response.getResponseBodyAsStream());
			return resp.getSessionId();

		}
		catch (BaseException e) {
			e.printStackTrace();
			System.out.println("error = " + temp);
			handleResponseParseException(respStream, e);
		}
		catch (Exception e) {
			e.printStackTrace();

		}
		finally {
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
			System.out.println(err.toString());

		}
		catch (BaseException e2) {
			e2.printStackTrace();
		}
	}

	protected void logout(String session_id) {

		if (session_id == null) {
			System.out.println("Null was provided as session id for logout.");
			return;
		}

		LogoutRequest req = new LogoutRequest();
		req.setSessionId(session_id);

		PostMethod response = null;

		try {
			response = sendXMLData(req, getLogoutRequestXStream());
		}
		finally {
			response.releaseConnection();
		}
	}

	protected PostMethod sendXMLData(Object req, XStream xstream) {
		PostMethod post = new PostMethod("https://b2b.fbl.is/lib2b.dll?processXML");
		//PostMethod post = new PostMethod("https://b2b.fbl.is/test/lib2btest.dll?processXML");

		try {
			String data = XML_HEADER + xstream.toXML(req);
			//StringPart userPart = new StringPart("processXML", XML_HEADER + xstream.toXML(req), "UTF-8");
			//userPart.setContentType("text/xml");

			//Part[] parts = { userPart };
			
			//post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
			post.setRequestBody(data);

			HttpClient client = new HttpClient();

			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			
			//System.out.print("post = ");
			//post.getRequestEntity().writeRequest(System.out);
			
			client.executeMethod(post);

			return post;

		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected synchronized XStream getGeneralErrorMessageXStream() {

		if (err_xstream == null) {

			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("$", "_")));
			xstream.alias("LI_Villa", GeneralErrorMessage.class);
			xstream.aliasField("villa", GeneralErrorMessage.class, GeneralErrorMessage.error_number_field);
			xstream.aliasField("villubod", GeneralErrorMessage.class, GeneralErrorMessage.error_msg_field);
			xstream.aliasField("dags_mottekid", GeneralErrorMessage.class, GeneralErrorMessage.query_date_and_time_field);
			xstream.aliasField("dags_svarad", GeneralErrorMessage.class, GeneralErrorMessage.reply_completion_date_and_time_field);

			err_xstream = xstream;
		}

		return err_xstream;
	}

	protected synchronized XStream getSendDataResponseErrorMessageXStream() {

		if (send_data_response_err_xstream == null) {

			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("$", "_")));

			xstream.alias("LI_Innsending_gagna_svar", SendingInDataResponse.class);
			xstream.aliasField("timi", SendingInDataResponse.class, SendingInDataResponse.time_field);
			xstream.aliasField("villa", SendingInDataResponse.class, SendingInDataResponse.error_number_field);
			xstream.aliasField("villubod", SendingInDataResponse.class, SendingInDataResponse.error_msg_field);
			xstream.aliasField("dags_mottekid", Time.class, Time.query_data_and_time_field);
			xstream.aliasField("dags_svarad", Time.class, Time.reply_data_and_time_field);

			send_data_response_err_xstream = xstream;
		}

		return send_data_response_err_xstream;
	}

	protected synchronized XStream getLoginResponseXStream() {

		if (login_resp_xstream == null || true) {

			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("$", "_")));

			xstream.alias("LI_Innskra_svar", LoginResponse.class);
			xstream.aliasField("seta", LoginResponse.class, LoginResponse.session_id_field);
			xstream.alias("timi", Time.class);
			xstream.aliasField("timi", LoginResponse.class, LoginResponse.time_field);
			xstream.aliasField("dags_mottekid", Time.class, Time.query_data_and_time_field);
			xstream.aliasField("dags_svarad", Time.class, Time.reply_data_and_time_field);

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


	protected synchronized XStream getLogoutRequestXStream() {

		if (logout_xstream == null) {

			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("$", "_")));

			xstream.alias("LI_Utskra", LogoutRequest.class);
			xstream.aliasField("seta", LogoutRequest.class, LogoutRequest.session_id_field);

			xstream.useAttributeFor(LogoutRequest.class, LogoutRequest.xsi_field);
			xstream.aliasAttribute(LogoutRequest.class, LogoutRequest.xsi_field, "xmlns:xsi");
			xstream.useAttributeFor(LogoutRequest.class, LogoutRequest.xsi_no_nmspc_field);
			xstream.aliasAttribute(LogoutRequest.class, LogoutRequest.xsi_no_nmspc_field, "xsi:noNamespaceSchemaLocation");
			xstream.useAttributeFor(LogoutRequest.class, LogoutRequest.version_field);

			logout_xstream = xstream;
		}

		return logout_xstream;
	}

	protected synchronized XStream getSendDataXStream() {

		if (send_data_xstream == null) {

			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("$", "_")));

			xstream.alias("LI_Innsending_gagna", SendingInData.class);
			xstream.aliasField("seta", SendingInData.class, SendingInData.session_id_field);
			xstream.aliasField("tegund", SendingInData.class, SendingInData.data_type_field);
			xstream.aliasField("strengur", SendingInData.class, SendingInData.data_field);

			xstream.useAttributeFor(SendingInData.class, SendingInData.xsd_field);
			xstream.aliasAttribute(SendingInData.class, SendingInData.xsd_field, "xmlns:xsd");
			xstream.useAttributeFor(SendingInData.class, SendingInData.xsi_field);
			xstream.aliasAttribute(SendingInData.class, SendingInData.xsi_field, "xmlns:xsi");
			xstream.useAttributeFor(SendingInData.class, SendingInData.version_field);

			send_data_xstream = xstream;
		}

		return send_data_xstream;
	}

	protected String[] getLoginAndPassword() {

			String login = "3HeH568lfi";
			String pass = "KQb88fi";

			if (login == null || pass == null) {
				return null;
			}

			CypherText ct = new CypherText();
			login = ct.doDeCypher(login, ck);
			pass = ct.doDeCypher(pass, ck);
			
			//login = "L621077B2B";
			//pass = "L6192965";

			return new String[] { login, pass };
	}

}