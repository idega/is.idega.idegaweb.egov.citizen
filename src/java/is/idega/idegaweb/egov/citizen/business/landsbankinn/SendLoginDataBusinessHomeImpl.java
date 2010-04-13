package is.idega.idegaweb.egov.citizen.business.landsbankinn;


import javax.ejb.CreateException;
import com.idega.business.IBOHomeImpl;

public class SendLoginDataBusinessHomeImpl extends IBOHomeImpl implements
		SendLoginDataBusinessHome {
	public Class getBeanInterfaceClass() {
		return SendLoginDataBusiness.class;
	}

	public SendLoginDataBusiness create() throws CreateException {
		return (SendLoginDataBusiness) super.createIBO();
	}
}