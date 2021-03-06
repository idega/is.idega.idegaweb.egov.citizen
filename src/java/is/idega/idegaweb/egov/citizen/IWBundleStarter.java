/*
 * $Id$
 * Created on Oct 30, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.citizen;

import is.idega.idegaweb.egov.citizen.data.CitizenRemoteServices;
import is.idega.idegaweb.egov.citizen.data.CitizenRemoteServicesHome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.CreateException;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.core.contact.data.ContactPurpose;
import com.idega.core.contact.data.ContactPurposeHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.util.ListUtil;

public class IWBundleStarter implements IWBundleStartable {

	public final static String IW_BUNDLE_IDENTIFIER = "is.idega.idegaweb.egov.citizen";
	
	@Override
	public void start(IWBundle starterBundle) {
		addDefaultRemoteServices(starterBundle);
		try{
			addDefaultContactPurposes();
		}catch(Exception e){
			Logger.getLogger(IWBundleStarter.class.getName()).log(Level.WARNING, "failed adding default contact purposes for ehub", e);
		}
	}

	@Override
	public void stop(IWBundle starterBundle) {
	}
	
	private void addDefaultContactPurposes() throws CreateException, IDOLookupException{
		ContactPurposeHome contactPurposeHome = (ContactPurposeHome) IDOLookup.getHome(ContactPurpose.class);
		Collection<ContactPurpose> contacts = contactPurposeHome.getContactPurposes(1);
		if(!ListUtil.isEmpty(contacts)){
			return;
		}
		ContactPurpose contactPurpose = contactPurposeHome.create();
		contactPurpose.setDisplayName("eHUB_messages");
		contactPurpose.store();
		contactPurpose = contactPurposeHome.create();
		contactPurpose.setDisplayName("eHUB_messages_SMS");
		contactPurpose.store();
	}
	
	
	private Collection<AdvancedProperty> getDefaultRemoteServices(){
		Collection <AdvancedProperty> defaultRemoteServices = new ArrayList<AdvancedProperty>();
		AdvancedProperty service = new AdvancedProperty("http://impratest.sidan.is","Innovation Center");
		defaultRemoteServices.add(service);
		service = new AdvancedProperty("http://biladev.sidan.is","Reykjavík City");
		defaultRemoteServices.add(service);
		service = new AdvancedProperty("http://felixtest.sidan.is","ÍSÍ");
		defaultRemoteServices.add(service);
		return defaultRemoteServices;
	}
	
	private void addDefaultRemoteServices(IWBundle starterBundle){
		CitizenRemoteServicesHome citizenRemoteServicesHome = null;
		try {
			citizenRemoteServicesHome = (CitizenRemoteServicesHome) IDOLookup.getHome(CitizenRemoteServices.class);
		} catch (IDOLookupException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "failed adding default remote services", e);
			return;
		}
		Collection<CitizenRemoteServices> services = citizenRemoteServicesHome.getRemoteServices(1);
		if(!ListUtil.isEmpty(services)){
			return;
		}
		
		Collection<AdvancedProperty> defaultServices = getDefaultRemoteServices();
		for(AdvancedProperty service : defaultServices){
			CitizenRemoteServices citizenRemoteServices =  null;
			try {
				citizenRemoteServices = (CitizenRemoteServices) citizenRemoteServicesHome.createIDO();
				citizenRemoteServices.setAddress(service.getId());
				citizenRemoteServices.setServerName(service.getValue());
				citizenRemoteServices.store();
			} catch (Exception e) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "failed adding default remote service" + service, e);
			}
		}
	}
}