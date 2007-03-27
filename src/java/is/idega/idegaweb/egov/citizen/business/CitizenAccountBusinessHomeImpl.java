package is.idega.idegaweb.egov.citizen.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHomeImpl;

public class CitizenAccountBusinessHomeImpl extends IBOHomeImpl implements CitizenAccountBusinessHome {

	public Class getBeanInterfaceClass() {
		return CitizenAccountBusiness.class;
	}

	public CitizenAccountBusiness create() throws CreateException {
		return (CitizenAccountBusiness) super.createIBO();
	}
}