/**
 * 
 */
package is.idega.idegaweb.egov.citizen.business;




import com.idega.business.IBOHomeImpl;

/**
 * @author bluebottle
 *
 */
public class WSCitizenAccountBusinessHomeImpl extends IBOHomeImpl implements
		WSCitizenAccountBusinessHome {
	protected Class getBeanInterfaceClass() {
		return WSCitizenAccountBusiness.class;
	}

	public WSCitizenAccountBusiness create() throws javax.ejb.CreateException {
		return (WSCitizenAccountBusiness) super.createIBO();
	}

}
