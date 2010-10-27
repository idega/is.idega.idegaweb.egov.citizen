package is.idega.idegaweb.egov.citizen.wsclient.landsbankinn;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public class SendingInDataResponse {

	public static final String time_field = "time";
	public static final String error_number_field = "error_number";
	public static final String error_msg_field = "error_msg";
	
	private Time time;
	private String error_number;
	private String error_msg;

	public Time getTime() {
		return time;
	}
	public void setTime(Time time) {
		this.time = time;
	}
	public String getErrorMsg() {
		return error_msg;
	}
	public void setErrorMsg(String error_msg) {
		this.error_msg = error_msg;
	}
	public String getErrorNumber() {
		return error_number;
	}
}