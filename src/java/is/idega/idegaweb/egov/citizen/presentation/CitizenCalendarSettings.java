package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.idegaweb.egov.citizen.CitizenConstants;
import is.idega.idegaweb.egov.citizen.bean.UserSettingsRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import com.idega.block.cal.data.CalDAVCalendar;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.facelets.ui.FaceletComponent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class CitizenCalendarSettings extends IWBaseComponent{

	
	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		
		IWContext iwc = IWContext.getIWContext(context);
		IWBundle bundle = iwc.getIWMainApplication().getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		
		if(!iwc.isLoggedOn()){
			Layer layer = new Layer();
			add(layer);
			layer.addText(iwrb.getLocalizedString("not_logged_on", "Not logged on"));
			return;
		}
		
		add(getInitializeScriptDiv());
		
		FaceletComponent facelet = (FaceletComponent)iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(bundle.getFaceletURI("citizen-calendar-settings.xhtml"));
		
		add(facelet);
		
		addFiles(iwc);
	}
	
	private Layer getInitializeScriptDiv(){
		Layer layer = new Layer();
		UserSettingsRequest userSettingsRequest = ELUtil.getInstance().getBean(UserSettingsRequest.SERVICE);
		Collection<CalDAVCalendar> subscribed = userSettingsRequest.getSubscribedCalendars();
		StringBuilder script = new StringBuilder("jQuery(document).ready(function(){");
		script.append("\n\tvar data = {};");
		script.append("\n\tdata.subscriptionsFieldsetClass = 'citizen-calendar-subscription';");
		script.append("\n\tdata.subscribedCalendors = [");
		
		if(!ListUtil.isEmpty(subscribed)){
			script.append(CoreConstants.QOUTE_SINGLE_MARK);
			for(Iterator<CalDAVCalendar> iter = subscribed.iterator();iter.hasNext();){
				CalDAVCalendar calendar = iter.next();
				script.append(calendar.getPath());
				if(iter.hasNext()){
					script.append(CoreConstants.JS_STR_PARAM_SEPARATOR);
				}
			}
			script.append(CoreConstants.QOUTE_SINGLE_MARK);
		}
		script.append("];");
		script.append("\n\tCitizenCalendarSettingsHelper.initialize(data);");
		script.append("\n});");
		
		String action = PresentationUtil.getJavaScriptAction(script.toString());
		layer.add(action);
		return layer;
	}
	
	private void addFiles(IWContext iwc){
		List<String> scripts = new ArrayList<String>();
		List<String> styles = new ArrayList<String>();

		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();
			scripts.add(jQuery.getBundleURIToJQueryLib());

			scripts.add(web2.getBundleUriToHumanizedMessagesScript());
			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());
			

		}else{
			Logger.getLogger(CitizenMessages.class.getName()).log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/CitizenCalendarSettingsHelper.js"));
		styles.add(iwb.getVirtualPathWithFileNameString("style/citizen.css"));
		scripts.add("/dwr/interface/CitizenServices.js");
		
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		PresentationUtil.addStyleSheetsToHeader(iwc, styles);
	}
}
