package is.idega.idegaweb.egov.citizen.business.landsbankan;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import com.idega.business.IBOServiceBean;
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
	
	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final String DEFAULT_SERVICE_URL = "https://b2b.fbl.is/test/lib2btest.dll?processXML";
	private static final String LANDSBANKIN_SERVICE_URL = "LANDSBANKIN_SERVICE_URL";
	
	public void send(String xml_str) {
		
		if(xml_str == null)
			throw new NullPointerException("XML data not provided");
		
		String session_id = login();
		
		if(session_id == null)
			throw new RuntimeException("Session id couldn't be retrieved while logging in");
		
		SendingInData data = new SendingInData();
		data.setSessionId(session_id);
		data.setData(xml_str);
		
		PostMethod response = sendXMLData(data, getSendDataXStream());
		
		SendingInDataResponse err = null;
		
		try {
			err = (SendingInDataResponse)getSendDataResponseErrorMessageXStream().fromXML(response.getResponseBodyAsStream());
			 
			if(err.getErrorMsg() == null && err.getErrorNumber() == null)
				return;
		
		} catch (Exception e) {

//			TODO: provide response as a string
			RuntimeException ex = new RuntimeException("Exception while parsing sendXMLData response");
			ex.initCause(e);
			
			throw ex;
			
		} finally {
			
			response.releaseConnection();
			logout(session_id);
		}
		
		throw new RuntimeException("Error got from response:\n - err msg: "+err.getErrorMsg()+"\n - err number: "+err.getErrorNumber());
	}
	
	protected String login() {
		
		LoginRequest req = new LoginRequest();
		req.setLoginName("L621077B2B");
		req.setLoginPassword("L6192965");
		
		PostMethod response = sendXMLData(req, getLoginRequestXStream());
		
		InputStream respStream = null;
		
		try {
			respStream = response.getResponseBodyAsStream();
			LoginResponse resp = (LoginResponse)getLoginResponseXStream().fromXML(response.getResponseBodyAsStream());
			return resp.getSessionId();
			
		} catch (BaseException e) {
			
			handleResponseParseException(respStream, e);
		} catch (Exception e) {
			
			logger.log(Level.SEVERE, "Exception while reading login response", e);
			
		} finally {
			response.releaseConnection();
		}
		
		return null;
	}
	
	protected void handleResponseParseException(InputStream responseStream, BaseException e) {
		
		if(responseStream == null)
			return;
		
		try {
			
			GeneralErrorMessage err = (GeneralErrorMessage)getGeneralErrorMessageXStream().fromXML(responseStream);
			logger.log(Level.SEVERE, "Error msg got from response: "+err.getErrorMsg());
			
		} catch (BaseException e2) {
			logger.log(Level.SEVERE, "Error while parsing error message", e2);
		}
	}
	
	public static void main(String[] args) {
		new SendLoginDataBusinessBean().send("xmldata");
	}
	
	protected void logout(String session_id) {
		
		if(session_id == null) {
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
		
		PostMethod post = new PostMethod(DEFAULT_SERVICE_URL/*getIWMainApplication().getSettings().getProperty(LANDSBANKIN_SERVICE_URL, DEFAULT_SERVICE_URL)*/);
		
		try {
			StringPart userPart = new StringPart("processXML", XML_HEADER+xstream.toXML(req), "UTF-8");
			userPart.setContentType("text/xml");
			
			Part[] parts = {userPart};
			
			post.setRequestEntity(new MultipartRequestEntity(parts,
					post.getParams()));
			
			HttpClient client = new HttpClient();
			
			client.getHttpConnectionManager().getParams().setConnectionTimeout(
					5000);
			client.executeMethod(post);
			
			return post;
			
		} catch (Exception e) {
			
			logger.log(Level.SEVERE, "Exception while sending xml data.", e);
			return null;
		}
	}
	
	protected synchronized XStream getGeneralErrorMessageXStream() {
		
		if(err_xstream == null) {
			
			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("_", "_")));
			xstream.alias("LIVilla", GeneralErrorMessage.class);
			xstream.aliasField("villa", GeneralErrorMessage.class, GeneralErrorMessage.error_number_field);
			xstream.aliasField("villubod", GeneralErrorMessage.class, GeneralErrorMessage.error_msg_field);
			xstream.aliasField("dagsMottekid", GeneralErrorMessage.class, GeneralErrorMessage.query_date_and_time_field);
			xstream.aliasField("dagsSvarad", GeneralErrorMessage.class, GeneralErrorMessage.reply_completion_date_and_time_field);
			
			err_xstream = xstream;
		}
		
		return err_xstream;
	}
	
	protected synchronized XStream getSendDataResponseErrorMessageXStream() {
		
		if(send_data_response_err_xstream == null) {
			
			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("_", "_")));
			
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
		
		if(login_resp_xstream == null) {
			
			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("_", "_")));
			
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
		
		if(login_xstream == null) {
			
			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("_", "_")));
			
			xstream.alias("LI_Innskra", LoginRequest.class);
			xstream.aliasField("notandanafn", LoginRequest.class, LoginRequest.login_name_field);
			xstream.aliasField("lykilord", LoginRequest.class, LoginRequest.login_password_field);
			
			xstream.useAttributeFor(LoginRequest.class, LoginRequest.xsd_field);
			xstream.aliasAttribute(LoginRequest.class, LoginRequest.xsd_field, "xmlns:xsd");
			xstream.useAttributeFor(LoginRequest.class, LoginRequest.xsi_field);
			xstream.aliasAttribute(LoginRequest.class, LoginRequest.xsi_field, "xmlns:xsi");
			xstream.useAttributeFor(LoginRequest.class, LoginRequest.version_field);
			
			login_xstream = xstream;
		}
		
		return login_xstream;
	}
	
	protected synchronized XStream getLogoutRequestXStream() {
		
		if(logout_xstream == null) {
			
			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("_", "_")));
			
			xstream.alias("LI_Utskra", LogoutRequest.class);
			xstream.aliasField("seta", LogoutRequest.class, LogoutRequest.session_id_field);
			
			xstream.useAttributeFor(LogoutRequest.class, LogoutRequest.xsi_field);
			xstream.aliasAttribute(LogoutRequest.class, LogoutRequest.xsi_field, "xmlns:xsi");
			xstream.useAttributeFor(LogoutRequest.class, LogoutRequest.xsi_no_nmspc_field);
			xstream.aliasAttribute(LogoutRequest.class, LogoutRequest.xsi_no_nmspc_field, "xsi:noNamespaceSchemaLocationi");
			xstream.useAttributeFor(LogoutRequest.class, LogoutRequest.version_field);
			
			logout_xstream = xstream;
		}
		
		return logout_xstream;
	}
	
	protected synchronized XStream getSendDataXStream() {
		
		if(send_data_xstream == null) {
			
			XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("_", "_")));
			
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
}