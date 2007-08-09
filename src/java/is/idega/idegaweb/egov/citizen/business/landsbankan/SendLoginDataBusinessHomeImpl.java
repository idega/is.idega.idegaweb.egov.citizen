package is.idega.idegaweb.egov.citizen.business.landsbankan;

import javax.ejb.CreateException;
import com.idega.business.IBOHomeImpl;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public class SendLoginDataBusinessHomeImpl extends IBOHomeImpl implements SendLoginDataBusinessHome {

	private static final long serialVersionUID = 3558898843474014929L;

	public Class getBeanInterfaceClass() {
		return SendLoginDataBusiness.class;
	}

	public SendLoginDataBusiness create() throws CreateException {
		return (SendLoginDataBusiness) super.createIBO();
	}
}