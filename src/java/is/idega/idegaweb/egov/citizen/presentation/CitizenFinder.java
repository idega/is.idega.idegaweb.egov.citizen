/*
 * $Id$ Created on Jan 23, 2006
 * 
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.idegaweb.egov.accounting.business.CitizenBusiness;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.data.ICPage;
import com.idega.core.contact.data.Email;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.PostalCode;
import com.idega.event.IWPageEventListener;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.UserSession;
import com.idega.user.data.User;
import com.idega.util.Age;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.Name;
import com.idega.util.text.TextSoap;

public class CitizenFinder extends CitizenBlock implements IWPageEventListener {

	protected static final String PARAMETER_FIRST_NAME = "cul_pfn";
	protected static final String PARAMETER_MIDDLE_NAME = "cul_pmn";
	protected static final String PARAMETER_LAST_NAME = "cul_pln";
	protected static final String PARAMETER_PERSONAL_ID = "cul_pid";
	protected static final String PARAMETER_SEARCH = "cul_search";

	private static final String PARAMETER_USER_PK = "cf_user_pk";
	private static final String PARAMETER_USER_UNIQUE_ID = "cf_user_unique_id";
	private static final String PROPERTY_MINIMUM_AGE_CITIZEN_FINDER = "citizen.finder.minimum.age";

	private ICPage iPage;
	private Collection users;
	protected IWResourceBundle iwrb;

	public void present(IWContext iwc) {
		this.iwrb = getResourceBundle(iwc);
		parseAction(iwc);

		add(getSearchForm(iwc));
		if (this.users != null) {
			add(getUserTable(iwc));
		}
	}

	private Collection getUsers(IWContext iwc, String firstName, String middleName, String lastName, String pid) throws RemoteException {
		return getUserBusiness(iwc).findUsersByConditions(firstName, middleName, lastName, pid);
	}

	protected String getHeading(IWContext iwc) {
		return this.iwrb.getLocalizedString("citizen_finder", "Citizen finder");
	}

	protected Form getSearchForm(IWContext iwc) {
		Form form = new Form();
		form.setID("citizenFinderForm");
		form.setStyleClass("citizenForm");

		Layer header = new Layer(Layer.DIV);
		header.setStyleClass("header");
		form.add(header);

		Heading1 heading = new Heading1(getHeading(iwc));
		header.add(heading);

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		TextInput personalID = new TextInput(PARAMETER_PERSONAL_ID);
		personalID.keepStatusOnAction(true);

		TextInput firstName = new TextInput(PARAMETER_FIRST_NAME);
		firstName.keepStatusOnAction(true);

		TextInput middleName = new TextInput(PARAMETER_MIDDLE_NAME);
		middleName.keepStatusOnAction(true);

		TextInput lastName = new TextInput(PARAMETER_LAST_NAME);
		lastName.keepStatusOnAction(true);

		Layer helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(this.iwrb.getLocalizedString("citizen_finder_helper_text", "Please fill in personal ID and/or names and click 'Search'.")));
		section.add(helpLayer);

		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		Label label = new Label(this.iwrb.getLocalizedString("personalID", "Personal ID"), personalID);
		formItem.add(label);
		formItem.add(personalID);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(this.iwrb.getLocalizedString("first_name", "First name"), firstName);
		formItem.add(label);
		formItem.add(firstName);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(this.iwrb.getLocalizedString("middle_name", "Middle name"), middleName);
		formItem.add(label);
		formItem.add(middleName);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(this.iwrb.getLocalizedString("last_name", "Last name"), lastName);
		formItem.add(label);
		formItem.add(lastName);
		section.add(formItem);

		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");
		section.add(clearLayer);

		Layer buttonLayer = new Layer(Layer.DIV);
		buttonLayer.setStyleClass("buttonLayer");
		form.add(buttonLayer);

		Layer span = new Layer(Layer.SPAN);
		span.add(new Text(this.iwrb.getLocalizedString("search", "Search")));
		Link send = new Link(span);
		send.setStyleClass("sendLink");
		send.setToFormSubmit(form);
		buttonLayer.add(send);

		return form;
	}

	private Table2 getUserTable(IWContext iwc) {
		Table2 table = new Table2();
		table.setStyleClass("adminTable");
		table.setStyleClass("ruler");
		table.setWidth("100%");
		table.setCellpadding(0);
		table.setCellspacing(0);

		TableRowGroup group = table.createHeaderRowGroup();
		TableRow row = group.createRow();
		TableCell2 cell = row.createHeaderCell();
		cell.setStyleClass("firstColumn");
		cell.setStyleClass("name");
		cell.add(new Text(this.iwrb.getLocalizedString("name", "Name")));

		cell = row.createHeaderCell();
		cell.setStyleClass("personalID");
		cell.add(new Text(this.iwrb.getLocalizedString("personal_id", "Personal ID")));

		cell = row.createHeaderCell();
		cell.setStyleClass("address");
		cell.add(new Text(this.iwrb.getLocalizedString("address", "Address")));

		cell = row.createHeaderCell();
		cell.setStyleClass("postalCode");
		cell.add(new Text(this.iwrb.getLocalizedString("postal_code", "Postal code")));

		cell = row.createHeaderCell();
		cell.setStyleClass("lastColumn");
		cell.setStyleClass("email");
		cell.add(new Text(this.iwrb.getLocalizedString("email", "Email")));

		group = table.createBodyRowGroup();
		int iRow = 1;

		Iterator iter = this.users.iterator();
		while (iter.hasNext()) {
			User user = (User) iter.next();
			if (user.getPersonalID() == null || user.getPersonalID().length() == 0 || user.getPersonalID().indexOf("/") != -1) {
				continue;
			}

			Address address = null;
			try {
				address = getUserBusiness(iwc).getUsersMainAddress(user);
			}
			catch (RemoteException re) {
				throw new IBORuntimeException(re);
			}

			PostalCode postal = null;
			if (address != null) {
				postal = address.getPostalCode();
			}

			Email email = null;
			try {
				email = getUserBusiness(iwc).getUsersMainEmail(user);
			}
			catch (RemoteException re) {
				throw new IBORuntimeException(re);
			}
			catch (NoEmailFoundException nefe) {
				// No email found...
			}

			row = group.createRow();

			Name name = new Name(user.getFirstName(), user.getMiddleName(), user.getLastName());
			cell = row.createCell();
			cell.setStyleClass("firstColumn");
			cell.setStyleClass("name");

			Link nameLink = new Link(name.getName(iwc.getCurrentLocale()));
			if (isOfAge(iwc, user)) {
				nameLink.setEventListener(this.getClass());
				if (user.getUniqueId() != null) {
					nameLink.addParameter(PARAMETER_USER_UNIQUE_ID, user.getUniqueId());
				}
				else {
					nameLink.addParameter(PARAMETER_USER_PK, user.getPrimaryKey().toString());
				}
				if (this.iPage != null) {
					nameLink.setPage(this.iPage);
				}
			}
			else {
				nameLink.setURL("#");
				nameLink.setOnClick("alert('" + this.iwrb.getLocalizedString("citizen_finder.not_of_age", "The citizen is not old enough.") + "');");
			}
			cell.add(nameLink);

			cell = row.createCell();
			cell.setStyleClass("personalID");
			cell.add(new Text(PersonalIDFormatter.format(user.getPersonalID(), iwc.getCurrentLocale())));

			cell = row.createCell();
			cell.setStyleClass("address");
			if (address != null) {
				cell.add(new Text(address.getStreetAddress()));
			}
			else {
				cell.add(new Text("-"));
			}

			cell = row.createCell();
			cell.setStyleClass("postalCode");
			if (postal != null) {
				cell.add(new Text(postal.getPostalAddress()));
			}
			else {
				cell.add(new Text("-"));
			}

			cell = row.createCell();
			cell.setStyleClass("lastColumn");
			cell.setStyleClass("email");
			if (email != null && email.getEmailAddress() != null) {
				cell.add(new Text(email.getEmailAddress()));
			}
			else {
				cell.add(new Text("-"));
			}

			if (iRow % 2 == 0) {
				row.setStyleClass("evenRow");
			}
			else {
				row.setStyleClass("oddRow");
			}

			iRow++;
		}

		return table;
	}

	private void parseAction(IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_FIRST_NAME) || iwc.isParameterSet(PARAMETER_MIDDLE_NAME) || iwc.isParameterSet(PARAMETER_LAST_NAME) || iwc.isParameterSet(PARAMETER_PERSONAL_ID)) {
			String pid = iwc.getParameter(PARAMETER_PERSONAL_ID);
			String first = iwc.getParameter(PARAMETER_FIRST_NAME);
			String middle = iwc.getParameter(PARAMETER_MIDDLE_NAME);
			String last = iwc.getParameter(PARAMETER_LAST_NAME);

			first = TextSoap.capitalize(first);
			middle = TextSoap.capitalize(middle);
			last = TextSoap.capitalize(last);
			pid = pid.replaceAll("-", "");

			try {
				this.users = getUsers(iwc, first, middle, last, pid);
				this.users = filterResults(iwc, this.users);
			}
			catch (RemoteException re) {
				throw new IBORuntimeException(re);
			}
		}
	}

	protected Collection filterResults(IWContext iwc, Collection users) {
		return users;
	}

	protected boolean isOfAge(IWContext iwc, User user) {
		boolean isOfAge = true;
		if (user.getDateOfBirth() != null) {
			Age age = new Age(user.getDateOfBirth());
			int minimumAge = Integer.parseInt(iwc.getApplicationSettings().getProperty(PROPERTY_MINIMUM_AGE_CITIZEN_FINDER, "18"));
			if (minimumAge > 0 && age.getYears() < minimumAge) {
				isOfAge = false;
			}
		}

		return isOfAge;
	}

	public boolean actionPerformed(IWContext iwc) {
		try {
			if (iwc.isParameterSet(PARAMETER_USER_UNIQUE_ID)) {
				getUserSession(iwc).setUser(getUserBusiness(iwc).getUserByUniqueId(iwc.getParameter(PARAMETER_USER_UNIQUE_ID)));
				return true;
			}
			else if (iwc.isParameterSet(PARAMETER_USER_PK)) {
				getUserSession(iwc).setUser(getUserBusiness(iwc).getUser(new Integer(iwc.getParameter(PARAMETER_USER_PK))));
				return true;
			}
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
		}
		return false;
	}

	protected CitizenBusiness getUserBusiness(IWApplicationContext iwc) {
		try {
			return (CitizenBusiness) IBOLookup.getServiceInstance(iwc, CitizenBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	protected UserSession getUserSession(IWUserContext iwuc) {
		try {
			return (UserSession) IBOLookup.getSessionInstance(iwuc, UserSession.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	public void setResponsePage(ICPage page) {
		this.iPage = page;
	}
}