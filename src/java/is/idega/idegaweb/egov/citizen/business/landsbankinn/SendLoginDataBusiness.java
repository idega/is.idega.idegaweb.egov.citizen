package is.idega.idegaweb.egov.citizen.business.landsbankinn;


import java.rmi.RemoteException;
import com.idega.business.IBOService;

public interface SendLoginDataBusiness extends IBOService {
	/**
	 * @see is.idega.idegaweb.egov.citizen.business.landsbankinn.SendLoginDataBusinessBean#verifyBankAccount
	 */
	public boolean verifyBankAccount(String bankNumber, String accountType,
			String accountNumber, String personalID) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.citizen.business.landsbankinn.SendLoginDataBusinessBean#send
	 */
	public void send(String xml_str) throws RemoteException;
}