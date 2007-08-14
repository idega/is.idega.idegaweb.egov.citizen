package is.idega.idegaweb.egov.citizen.business.landsbankinn;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public class Time {

	public static final String query_data_and_time_field = "query_data_and_time";
	public static final String reply_data_and_time_field = "reply_data_and_time";
	
	private String query_data_and_time;
	private String reply_data_and_time;
	
	public String getQueryDataAndTime() {
		return query_data_and_time;
	}
	public void setQueryDataAndTime(String query_data_and_time) {
		this.query_data_and_time = query_data_and_time;
	}
	public String getReplyDataAndTime() {
		return reply_data_and_time;
	}
	public void setReplyDataAndTime(String reply_data_and_time) {
		this.reply_data_and_time = reply_data_and_time;
	}
}