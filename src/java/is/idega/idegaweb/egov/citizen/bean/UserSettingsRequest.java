package is.idega.idegaweb.egov.citizen.bean;

import java.util.Collection;
import java.util.Collections;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.core.contact.data.Email;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
@Service(UserSettingsRequest.SERVICE)
@Scope("request")
public class UserSettingsRequest {
	public static final String SERVICE = "userSettingsRequest";
	
	private  IWContext iwc = null;
	
	public Collection<Email> getEmails(){
		IWContext iwc = getIwc();
		if(!iwc.isLoggedOn()){
			return Collections.emptyList();
		}
		User user = iwc.getCurrentUser();
		@SuppressWarnings("unchecked")
		Collection<Email> emails = user.getEmails();
		if(ListUtil.isEmpty(emails)){
			return Collections.emptyList();
		}
		return emails;
	}
	
	public IWContext getIwc() {
		if(iwc==null){
			return CoreUtil.getIWContext();
		}
		return iwc;
	}
}
