package is.idega.idegaweb.egov.citizen.data;


import com.idega.core.accesscontrol.data.LoginTable;
import java.sql.Timestamp;
import com.idega.data.IDOEntity;

public interface UnsentCitizenAccount extends IDOEntity {
	/**
	 * @see is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountBMPBean#getLogin
	 */
	public LoginTable getLogin();

	/**
	 * @see is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountBMPBean#getKey
	 */
	public String getKey();

	/**
	 * @see is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountBMPBean#getFailedSendDate
	 */
	public Timestamp getFailedSendDate();

	/**
	 * @see is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountBMPBean#getOriginalError
	 */
	public String getOriginalError();

	/**
	 * @see is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountBMPBean#getLastRetryDate
	 */
	public Timestamp getLastRetryDate();

	/**
	 * @see is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountBMPBean#getRetryError
	 */
	public String getRetryError();

	/**
	 * @see is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountBMPBean#setLogin
	 */
	public void setLogin(LoginTable login);

	/**
	 * @see is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountBMPBean#setKey
	 */
	public void setKey(String key);

	/**
	 * @see is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountBMPBean#setFailedSendDate
	 */
	public void setFailedSendDate(Timestamp failedSendDate);

	/**
	 * @see is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountBMPBean#setOriginalError
	 */
	public void setOriginalError(String error);

	/**
	 * @see is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountBMPBean#setLastRetryDate
	 */
	public void setLastRetryDate(Timestamp retrySendDate);

	/**
	 * @see is.idega.idegaweb.egov.citizen.data.UnsentCitizenAccountBMPBean#setRetryError
	 */
	public void setRetryError(String error);
}