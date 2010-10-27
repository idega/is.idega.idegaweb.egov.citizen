package is.idega.idegaweb.egov.citizen.wsclient.landsbankinn;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public class GeneralErrorMessage {

	private String error_number;
	private String error_msg;
	private String query_date_and_time;
	private String reply_completion_date_and_time;
	
	public static final String error_number_field = "error_number";
	public static final String error_msg_field = "error_msg";
	public static final String query_date_and_time_field = "query_date_and_time";
	public static final String reply_completion_date_and_time_field = "reply_completion_date_and_time";
	
	public String getErrorMsg() {
		return error_msg;
	}
	public void setErrorMsg(String error_msg) {
		this.error_msg = error_msg;
	}
	public String getErrorNumber() {
		return error_number;
	}
	public void setErrorNumber(String error_number) {
		this.error_number = error_number;
	}
	public String getQueryDateAndTime() {
		return query_date_and_time;
	}
	public void setQueryDateAndTime(String query_date_and_time) {
		this.query_date_and_time = query_date_and_time;
	}
	public String getReplyCompletionDateAndTime() {
		return reply_completion_date_and_time;
	}
	public void setReplyCompletionDateAndTime(
			String reply_completion_date_and_time) {
		this.reply_completion_date_and_time = reply_completion_date_and_time;
	}
	
	public String toString() {
		
		StringBuffer b = new StringBuffer("GeneralErrorMessage:");
		
		if(getErrorMsg() != null) {
			b.append("\nError msg: ")
			.append(getErrorMsg());
		}
		if(getErrorNumber() != null) {
			b.append("\nError number: ")
			.append(getErrorNumber());
		}
			
		if(getQueryDateAndTime() != null) {
			b.append("\nQuery date and time: ")
			.append(getQueryDateAndTime());
		}
			
		if(getReplyCompletionDateAndTime() != null) {
			b.append("\nReply completion data and time: ")
			.append(getReplyCompletionDateAndTime());
		}
		
		return b.toString();
	}
}