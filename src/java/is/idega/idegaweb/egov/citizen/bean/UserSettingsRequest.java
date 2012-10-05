package is.idega.idegaweb.egov.citizen.bean;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.cal.business.CalendarManagementService;
import com.idega.block.cal.data.CalDAVCalendar;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
@Service(UserSettingsRequest.SERVICE)
@Scope("request")
public class UserSettingsRequest {
	public static final String SERVICE = "userSettingsRequest";
	
	@Autowired(required = false)
	CalendarManagementService calendarManagementService;
	
	List<CalDAVCalendar> availableCalendars = null;
	
	List<CalDAVCalendar> subscribedCalendars = null;
	

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
	
	public Collection<Phone> getPhones(){
		IWContext iwc = getIwc();
		if(!iwc.isLoggedOn()){
			return Collections.emptyList();
		}
		User user = iwc.getCurrentUser();
		@SuppressWarnings("unchecked")
		Collection<Phone> phones = user.getPhones();
		if(ListUtil.isEmpty(phones)){
			return Collections.emptyList();
		}
		return phones;
	}
	
	public Collection<CalDAVCalendar> getAvailableCalendars(){
		if(availableCalendars == null){
			User user = getIwc().getCurrentUser();
			try {
				availableCalendars = calendarManagementService.getVisibleSubscriptions(user, -1, -1);
			} catch (Exception e) {
				availableCalendars = Collections.emptyList();
			}
		}
		return availableCalendars;
	}
	
	public List<CalDAVCalendar> getSubscribedCalendars() {
		if(subscribedCalendars == null){
			User user = getIwc().getCurrentUser();
			try {
				subscribedCalendars = calendarManagementService.getSubscribedCalendars(user, -1, -1);
			} catch (Exception e) {
				e.printStackTrace();
				subscribedCalendars = Collections.emptyList();
			}
		}
		return subscribedCalendars;
	}
	
	private IWContext getIwc() {
		if(iwc==null){
			return CoreUtil.getIWContext();
		}
		return iwc;
	}
}
