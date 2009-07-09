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

import is.idega.idegaweb.egov.citizen.IWBundleStarter;

import java.util.Collection;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;


public abstract class CitizenBlock extends Block {

	@Override
	public String getBundleIdentifier() {
		return IWBundleStarter.IW_BUNDLE_IDENTIFIER;
	}

	@Override
	public void main(IWContext iwc) {
		PresentationUtil.addStyleSheetToHeader(iwc, iwc.getIWMainApplication().getBundle("is.idega.idegaweb.egov.application").getVirtualPathWithFileNameString("style/application.css"));
		PresentationUtil.addStyleSheetToHeader(iwc, iwc.getIWMainApplication().getBundle(IWBundleStarter.IW_BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("style/citizen.css"));
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

	public abstract void present(IWContext iwc);
}