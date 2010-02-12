package is.idega.idegaweb.egov.citizen.data;

import java.sql.Timestamp;
import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.data.GenericEntity;
import com.idega.data.query.SelectQuery;
import com.idega.data.query.Table;

public class UnsentCitizenAccountBMPBean extends GenericEntity implements
		UnsentCitizenAccount {

	public static final String ENTITY_NAME = "comm_unsent_account";
	
	private static final String COLUMN_LOGIN = "login";
	private static final String COLUMN_KEY = "key";
	private static final String COLUMN_FAILED_SEND_DATE = "failed_date";
	private static final String COLUMN_ORIGINAL_ERROR = "error_message";
	private static final String COLUMN_LAST_RETRY = "last_retry";
	private static final String COLUMN_RETRY_ERROR = "retry_error_message";
	
	@Override
	public String getEntityName() {
		return ENTITY_NAME;
	}

	@Override
	public void initializeAttributes() {
		addAttribute(this.getIDColumnName());
		addManyToOneRelationship(COLUMN_LOGIN, LoginTable.class);
		addAttribute(COLUMN_KEY, "Key", String.class, 255);
		addAttribute(COLUMN_FAILED_SEND_DATE, "Date the send first failed on", Timestamp.class);
		addAttribute(COLUMN_ORIGINAL_ERROR, "Error", String.class, 1000);
		addAttribute(COLUMN_LAST_RETRY, "Last retry", Timestamp.class);
		addAttribute(COLUMN_RETRY_ERROR, "Retry error", String.class, 1000);
	}

	//getters
	public LoginTable getLogin() {
		return (LoginTable) getColumnValue(COLUMN_LOGIN);
	}
	
	public String getKey() {
		return getStringColumnValue(COLUMN_KEY);
	}
	
	public Timestamp getFailedSendDate() {
		return getTimestampColumnValue(COLUMN_FAILED_SEND_DATE);
	}
	
	public String getOriginalError() {
		return getStringColumnValue(COLUMN_ORIGINAL_ERROR);
	}
	
	public Timestamp getLastRetryDate() {
		return getTimestampColumnValue(COLUMN_LAST_RETRY);
	}
	
	public String getRetryError() {
		return getStringColumnValue(COLUMN_RETRY_ERROR);
	}
	
	//setters
	public void setLogin(LoginTable login) {
		setColumn(COLUMN_LOGIN, login);
	}
	
	public void setKey(String key) {
		setColumn(COLUMN_KEY, key);
	}
	
	public void setFailedSendDate(Timestamp failedSendDate) {
		setColumn(COLUMN_FAILED_SEND_DATE, failedSendDate);
	}
	
	public void setOriginalError(String error) {
		setColumn(COLUMN_ORIGINAL_ERROR, error);
	}

	public void setLastRetryDate(Timestamp retrySendDate) {
		setColumn(COLUMN_LAST_RETRY, retrySendDate);
	}
	
	public void setRetryError(String error) {
		setColumn(COLUMN_RETRY_ERROR, error);
	}
	
	//ejb
	public Collection ejbFindAll() throws FinderException {
		Table table = new Table(this);
		
		SelectQuery query = new SelectQuery(table);
		query.addColumn(table.getColumn(getIDColumnName()));
		
		return idoFindPKsByQuery(query);
	}
}