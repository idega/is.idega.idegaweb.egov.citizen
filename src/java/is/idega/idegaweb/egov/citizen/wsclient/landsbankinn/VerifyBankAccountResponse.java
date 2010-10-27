package is.idega.idegaweb.egov.citizen.wsclient.landsbankinn;

public class VerifyBankAccountResponse {
	public static final String exists_field = "exists";
	public static final String time_field = "time";
	
	private String exists;
	private Time time;
	
	public String getAccountExists() {
		return exists;
	}
	public void setAccountExists(String exists) {
		this.exists = exists;
	}
	public Time getTime() {
		return time;
	}
	public void setTime(Time time) {
		this.time = time;
	}
}