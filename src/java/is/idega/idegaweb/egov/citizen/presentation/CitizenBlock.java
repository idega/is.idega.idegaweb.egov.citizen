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

import java.util.Collection;
import java.util.Iterator;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;


public abstract class CitizenBlock extends Block {

	private final static String IW_BUNDLE_IDENTIFIER = "is.idega.idegaweb.egov.citizen";

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public void main(IWContext iwc) {
		present(iwc);
	}
	
	/**
	 * Adds the errors encountered
	 * @param iwc
	 * @param errors
	 */
	protected void showErrors(IWContext iwc, Collection errors) {
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("errorLayer");
		
		Layer image = new Layer(Layer.DIV);
		image.setStyleClass("errorImage");
		layer.add(image);
		
		Heading1 heading = new Heading1(getResourceBundle(iwc).getLocalizedString("application_errors_occured", "There was a problem with the following items"));
		layer.add(heading);
		
		Lists list = new Lists();
		layer.add(list);
		
		Iterator iter = errors.iterator();
		while (iter.hasNext()) {
			String element = (String) iter.next();
			ListItem item = new ListItem();
			item.add(new Text(element));
			
			list.add(item);
		}
		
		add(layer);
	}

	public abstract void present(IWContext iwc);
}