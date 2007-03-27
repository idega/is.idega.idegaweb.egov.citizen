/*
 * $Id$ Created on Mar 27, 2006
 * 
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.idegaweb.egov.citizen.business.CitizenAccountBusiness;

import java.rmi.RemoteException;

import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Label;

public class CitizenAccountStatistics extends CitizenBlock {

	public void present(IWContext iwc) {
		try {
			IWResourceBundle iwrb = getResourceBundle(iwc);
			int citizenAccountCount = getCitizenAccountBusiness(iwc).getNumberOfApplications();

			Layer section = new Layer(Layer.DIV);
			section.setStyleClass("formSection");
			section.setStyleClass("statisticsLayer");
			add(section);

			Layer clearLayer = new Layer(Layer.DIV);
			clearLayer.setStyleClass("Clear");

			Heading1 heading = new Heading1(iwrb.getLocalizedString("citizen_account.statistics", "Citizen account statistics"));
			section.add(heading);

			Layer formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			Label label = new Label();
			label.add(new Text(iwrb.getLocalizedString("citizen_account.number_of_accounts", "Number of accounts")));
			Layer span = new Layer(Layer.SPAN);
			span.add(String.valueOf(citizenAccountCount));
			formItem.add(label);
			formItem.add(span);
			section.add(formItem);

			section.add(clearLayer);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	protected CitizenAccountBusiness getCitizenAccountBusiness(IWApplicationContext iwac) throws RemoteException {
		return (CitizenAccountBusiness) IBOLookup.getServiceInstance(iwac, CitizenAccountBusiness.class);
	}
}