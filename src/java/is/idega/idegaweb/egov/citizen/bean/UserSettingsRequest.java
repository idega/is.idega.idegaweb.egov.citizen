package is.idega.idegaweb.egov.citizen.bean;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.cal.business.CalendarManagementService;
import com.idega.block.cal.data.CalDAVCalendar;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;

@Service(UserSettingsRequest.SERVICE)
@Scope("request")
public class UserSettingsRequest extends DefaultSpringBean {

	public static final String SERVICE = "userSettingsRequest";

	@Autowired(required = false)
	private CalendarManagementService calendarManagementService;

	private List<CalDAVCalendar> availableCalendars = null,
								subscribedCalendars = null;

	private CalendarManagementService getCalendarManagementService() {
		try {
			if (calendarManagementService == null) {
				ELUtil.getInstance().autowire(this);
			}
			return calendarManagementService;
		} catch (Exception e) {}
		return null;
	}

	private IWContext iwc = null;

	public Collection<Email> getEmails(){
		IWContext iwc = getIwc();
		if (!iwc.isLoggedOn()) {
			return Collections.emptyList();
		}
		User user = iwc.getCurrentUser();
		Collection<Email> emails = user.getEmails();
		if (ListUtil.isEmpty(emails)) {
			return Collections.emptyList();
		}
		return emails;
	}

	public Collection<Phone> getPhones(){
		IWContext iwc = getIwc();
		if (!iwc.isLoggedOn()) {
			return Collections.emptyList();
		}

		User user = iwc.getCurrentUser();
		Collection<Phone> phones = user.getPhones();
		if (ListUtil.isEmpty(phones)) {
			return Collections.emptyList();
		}
		return phones;
	}

	public Collection<CalDAVCalendar> getAvailableCalendars() {
		if (availableCalendars == null) {
			try {
				CalendarManagementService calendarManagementService = getCalendarManagementService();
				availableCalendars = calendarManagementService == null ? null : calendarManagementService.getVisibleSubscriptions(getIwc().getCurrentUser(), -1, -1);
			} catch (Exception e) {
				getLogger().log(Level.WARNING, "Error getting available calendars for current user", e);
				availableCalendars = Collections.emptyList();
			}
		}

		return availableCalendars;
	}

	public List<CalDAVCalendar> getSubscribedCalendars() {
		if (subscribedCalendars == null) {
			try {
				CalendarManagementService calendarManagementService = getCalendarManagementService();
				subscribedCalendars = calendarManagementService == null ? null : calendarManagementService.getSubscribedCalendars(getIwc().getCurrentUser(), -1, -1);
			} catch (Exception e) {
				getLogger().log(Level.WARNING, "Error getting subscribed calendars for current user", e);
				subscribedCalendars = Collections.emptyList();
			}
		}

		return subscribedCalendars;
	}

	private IWContext getIwc() {
		if (iwc == null)
			iwc =  CoreUtil.getIWContext();

		return iwc;
	}
}