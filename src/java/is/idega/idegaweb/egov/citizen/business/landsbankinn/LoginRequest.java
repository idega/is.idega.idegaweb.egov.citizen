package is.idega.idegaweb.egov.citizen.business.landsbankinn;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public class LoginRequest {

	public static final String login_name_field = "login_name";
	public static final String login_password_field = "login_password";
	public static final String xsd_field = "xsd";
	public static final String xsi_field = "xsi";
	public static final String version_field = "version";
	
	private String login_name;
	private String login_password;
	private String xsd = "http://www.w3.org/2001/XMLSchema";
	private String xsi = "http://www.w3.org/2001/XMLSchema-instance";
	private String version = "1.1";
	
	public String getLoginName() {
		return login_name;
	}
	public void setLoginName(String login_name) {
		this.login_name = login_name;
	}
	public String getLoginPassword() {
		return login_password;
	}
	public void setLoginPassword(String login_password) {
		this.login_password = login_password;
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