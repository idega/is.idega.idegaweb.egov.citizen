package is.idega.idegaweb.egov.citizen.wsclient.landsbankinn;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public class SendingInData {

	public static final String session_id_field = "session_id";
	public static final String data_type_field = "data_type";
	public static final String data_field = "data";
	public static final String xsd_field = "xsd";
	public static final String xsi_field = "xsi";
	public static final String version_field = "version";
	
	private String session_id;
	private String data_type = "NOTANDI_LYKILORD";
	private String data;
	private String xsd = "http://www.w3.org/2001/XMLSchema";
	private String xsi = "http://www.w3.org/2001/XMLSchema-instance";
	private String version = "1.1";
	
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getDataType() {
		return data_type;
	}
	public void setDataType(String data_type) {
		this.data_type = data_type;
	}
	public String getSessionId() {
		return session_id;
	}
	public void setSessionId(String session_id) {
		this.session_id = session_id;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getXsd() {
		return xsd;
	}
	public void setXsd(String xsd) {
		this.xsd = xsd;
	}
	public String getXsi() {
		return xsi;
	}
	public void setXsi(String xsi) {
		this.xsi = xsi;
	}
}