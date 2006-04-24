package is.idega.idegaweb.egov.citizen.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHomeImpl;

public class WSCitizenAccountBusinessHomeImpl extends IBOHomeImpl implements WSCitizenAccountBusinessHome {

	public Class getBeanInterfaceClass() {
		return WSCitizenAccountBusiness.class;
	}

	public WSCitizenAccountBusiness create() throws CreateException {
		return (WSCitizenAccountBusiness) super.createIBO();
	}
}