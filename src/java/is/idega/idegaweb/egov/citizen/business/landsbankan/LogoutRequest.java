package is.idega.idegaweb.egov.citizen.business.landsbankan;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public class LogoutRequest {

	public static final String session_id_field = "session_id";
	public static final String xsi_no_nmspc_field = "xsi_no_nmspc";
	public static final String xsi_field = "xsi";
	public static final String version_field = "version";
	
	private String session_id;
	private String xsi_no_nmspc = "https://b2b. fbl.is/schema/LIUtskra.xsd";
	private String xsi = "http://www.w3.org/2001/XMLSchema-instance";
	private String version = "1.1";
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getXsi() {
		return xsi;
	}
	public void setXsi(String xsi) {
		this.xsi = xsi;
	}
	public String getSessionId() {
		return session_id;
	}
	public void setSessionId(String session_id) {
		this.session_id = session_id;
	}
	public String getXsiNoNmspc() {
		return xsi_no_nmspc;
	}
	public void setXsiNoNmspc(String xsi_no_nmspc) {
		this.xsi_no_nmspc = xsi_no_nmspc;
	}
}