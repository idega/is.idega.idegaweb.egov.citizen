/**
 * 
 */
package is.idega.idegaweb.egov.citizen.business;




import com.idega.business.IBOHome;

/**
 * @author bluebottle
 *
 */
public interface WSCitizenAccountBusinessHome extends IBOHome {
	public WSCitizenAccountBusiness create() throws javax.ejb.CreateException,
			java.rmi.RemoteException;

}
