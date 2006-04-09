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

import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.TextInput;


public class CitizenAccountApplicationForwarder extends Block {

	private final static String IW_BUNDLE_IDENTIFIER = "is.idega.idegaweb.egov.citizen";
	
	private ICPage iForwardPage;

	public void main(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);

		Form form = new Form();
		form.setID("accountApplicationForward");
		
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("label");
		layer.add(new Text(iwrb.getLocalizedString("personal_id", "Personal ID")));
		form.add(layer);
		
		TextInput input = new TextInput(CitizenAccountApplication.SSN_KEY);
		form.add(input);
		
		layer = new Layer(Layer.DIV);
		layer.setStyleClass("example");
		layer.add(new Text(iwrb.getLocalizedString("account_application.example", "(Example 0110012230)")));
		form.add(layer);
		
		Layer span = new Layer(Layer.SPAN);
		span.add(new Text(iwrb.getLocalizedString("next", "Next")));
		Link link = new Link(span);
		if (this.iForwardPage != null) {
			form.setPageToSubmitTo(this.iForwardPage);
			link.setToFormSubmit(form);
		}
		else {
			link.setURL("#");
			link.setOnClick("alert('No forward page set');");
		}
		form.add(link);
		
		add(form);
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	
	public void setForwardPage(ICPage forwardPage) {
		this.iForwardPage = forwardPage;
	}

}