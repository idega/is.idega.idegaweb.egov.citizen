/*
 * $Id$
 * Created on Jan 15, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.citizen.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Locale;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQuery;
import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;
import com.idega.user.business.UserBusiness;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

import is.idega.idegaweb.egov.citizen.IWBundleStarter;


public abstract class CitizenBlock extends Block {

	@Autowired
	private JQuery jQuery;

	private JQuery getJQuery() {
		if (jQuery == null)
			ELUtil.getInstance().autowire(this);
		return jQuery;
	}

	@Override
	public String getBundleIdentifier() {
		return IWBundleStarter.IW_BUNDLE_IDENTIFIER;
	}

	@Override
	public void main(IWContext iwc) {
		PresentationUtil.addStyleSheetToHeader(iwc, iwc.getIWMainApplication().getBundle("is.idega.idegaweb.egov.application").getVirtualPathWithFileNameString("style/application.css"));
		PresentationUtil.addStyleSheetToHeader(iwc, iwc.getIWMainApplication().getBundle(IWBundleStarter.IW_BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("style/citizen.css"));
		PresentationUtil.addStyleSheetToHeader(iwc, iwc.getIWMainApplication().getBundle(IWBundleStarter.IW_BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("style/cropper.css"));
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getJQuery().getBundleURIToJQueryLib());
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, iwc.getIWMainApplication().getBundle(IWBundleStarter.IW_BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("javascript/cropper.js"));
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, iwc.getIWMainApplication().getBundle(IWBundleStarter.IW_BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("javascript/cropperhelper.js"));
		present(iwc);
	}

	/**
	 * Adds the errors encountered
	 * @param iwc
	 * @param errors
	 */
	protected void showErrors(IWContext iwc, Collection<String> errors) {
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("errorLayer");

		Layer image = new Layer(Layer.DIV);
		image.setStyleClass("errorImage");
		layer.add(image);

		Heading1 heading = new Heading1(getResourceBundle(iwc).getLocalizedString("application_errors_occured", "There was a problem with the following items"));
		layer.add(heading);

		Lists list = new Lists();
		layer.add(list);

		if (!ListUtil.isEmpty(errors)) {
			for (String error: errors) {
				ListItem item = new ListItem();
				item.add(new Text(error));

				list.add(item);
			}
		}

		add(layer);
	}

	protected UserBusiness getUserBusiness(IWApplicationContext iwac) throws RemoteException {
		return IBOLookup.getServiceInstance(iwac, UserBusiness.class);
	}

	protected boolean isValidPersonalId(String personalId, Locale locale) {
		try {
			locale = locale == null ? getIWUserContext().getCurrentLocale() : locale;
			UserBusiness userBusiness = getUserBusiness(getIWApplicationContext());
			boolean validPersonalId = userBusiness.validatePersonalId(personalId, locale);
			return validPersonalId;
		} catch (Exception e){
			getLogger().log(Level.WARNING, "Error validating personal ID: " + personalId + " and locale " + locale, e);
		}
		return false;
	}

	public abstract void present(IWContext iwc);
}