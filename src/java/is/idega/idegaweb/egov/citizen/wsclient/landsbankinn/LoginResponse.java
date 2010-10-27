package is.idega.idegaweb.egov.citizen.wsclient.landsbankinn;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public class LoginResponse {

	public static final String session_id_field = "session_id";
	public static final String time_field = "time";
	
	private String session_id;
	private Time time;
	
	public String getSessionId() {
		return session_id;
	}
	public void setSessionId(String session_id) {
		this.session_id = session_id;
	}
	public Time getTime() {
		return time;
	}
	public void setTime(Time time) {
		this.time = time;
	}
}