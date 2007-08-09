package is.idega.idegaweb.egov.citizen.business.landsbankan;

import com.idega.business.IBOService;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public interface SendLoginDataBusiness extends IBOService {

	public abstract void send(String xml_str);
}