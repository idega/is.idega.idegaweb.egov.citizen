package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.idegaweb.egov.citizen.CitizenConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

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
import com.idega.webface.WFUtil;

public class CitizenMessages  extends IWBaseComponent{

	
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
		FaceletComponent facelet = (FaceletComponent)iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(bundle.getFaceletURI("citizen-messages.xhtml"));
		
		add(facelet);
		
		addFiles(iwc);
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

//			scripts.addAll(web2.getBundleURIsToFancyBoxScriptFiles());
//			styles.add(web2.getBundleURIToFancyBoxStyleFile());

			scripts.add(web2.getBundleUriToHumanizedMessagesScript());
			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());
			

		}else{
			Logger.getLogger(CitizenMessages.class.getName()).log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(CitizenConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/itizenMessagesHelper.js"));
		styles.add(iwb.getVirtualPathWithFileNameString("style/citizen.css"));
		scripts.add("/dwr/interface/CitizenServices.js");
	}
}
