package is.idega.idegaweb.egov.citizen.business;

import is.idega.idegaweb.egov.citizen.data.AccountApplication;
import is.idega.idegaweb.egov.message.business.CommuneMessageBusiness;

public interface AccountBusiness extends com.idega.business.IBOService {

	public void acceptApplication(int p0, com.idega.user.data.User p1, boolean p2) throws java.rmi.RemoteException, javax.ejb.CreateException;

	public void acceptApplication(int p0, com.idega.user.data.User p1, boolean p2, boolean p3, boolean p4) throws java.rmi.RemoteException, javax.ejb.CreateException;

	public java.lang.String getAcceptMessageSubject(AccountApplication theCase) throws java.rmi.RemoteException;

	public java.lang.String getBundleIdentifier() throws java.rmi.RemoteException;

	public CommuneMessageBusiness getMessageBusiness() throws java.rmi.RemoteException, java.rmi.RemoteException;

}
