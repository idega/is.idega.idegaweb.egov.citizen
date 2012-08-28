/*
 * $Id$
 *
 * Copyright (C) 2002 Idega hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 *
 */
package is.idega.idegaweb.egov.citizen.data;

import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.block.process.data.AbstractCaseBMPBean;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseStatus;
import com.idega.data.IDOException;
import com.idega.data.IDOQuery;
import com.idega.data.query.MatchCriteria;
import com.idega.data.query.SelectQuery;
import com.idega.data.query.Table;
import com.idega.data.query.WildCardColumn;

/**
 * @author <a href="mail:palli@idega.is">Pall Helgason</a>
 * @author <a href="http://www.staffannoteberg.com">Staffan Nöteberg</a>
 * @version 1.0
 */
public class CitizenAccountBMPBean extends AbstractCaseBMPBean implements CitizenAccount, AccountApplication, Case {

	private static final long serialVersionUID = 5771772346529015263L;

	private final static String ENTITY_NAME = "comm_cit_acc";
	private final static String CASE_CODE_KEY = "MBANSKO";
	private final static String CASE_CODE_KEY_DESC = "Request for citizen account";

	private final static String NAME = "name";
	private final static String SSN = "ssn";
	private final static String EMAIL = "email";
	private final static String PHONE_HOME = "phone_home";
	private final static String PHONE_WORK = "phone_work";
	private final static String CAREOF = "careof";
	private final static String STREET = "street";
	private final static String ZIP_CODE = "zip_code";
	private final static String CITY = "city";
	private final static String CIVIL_STATUS = "civil_status";
	private final static String HAS_COHABITANT = "has_cohabitant";
	private final static String CHILDREN_COUNT = "children_count";
	private final static String APPLICATION_REASON = "application_reason";

	@Override
	public void initializeAttributes() {
		super.addGeneralCaseRelation();
		addAttribute(NAME, "Name", true, true, String.class, 100);
		addAttribute(SSN, "SSN", true, true, String.class, 40);
		addAttribute(EMAIL, "E-mail", true, true, String.class, 255);
		addAttribute(PHONE_HOME, "Home phone", true, true, String.class, 20);
		addAttribute(PHONE_WORK, "Work phone", true, true, String.class, 20);
		addAttribute(CAREOF, "careof", true, true, String.class, 40);
		addAttribute(STREET, "street", true, true, String.class, 40);
		addAttribute(ZIP_CODE, "zipCode", true, true, String.class, 40);
		addAttribute(CITY, "city", true, true, String.class, 40);
		addAttribute(CIVIL_STATUS, "Civil Status", true, true, String.class, 40);
		addAttribute(HAS_COHABITANT, "Has Cohabitant", true, true, Boolean.class);
		addAttribute(CHILDREN_COUNT, "Children Count", true, true, Integer.class);
		addAttribute(APPLICATION_REASON, "Application Reason", true, true, String.class, 40);

		super.initializeAttributes();
	}

	@Override
	public String getEntityName() {
		return ENTITY_NAME;
	}

	@Override
	public String getCaseCodeKey() {
		return CASE_CODE_KEY;
	}

	@Override
	public String getCaseCodeDescription() {
		return CASE_CODE_KEY_DESC;
	}

	// get methods for bean properties

	@Override
	public String getApplicantName() {
		return getStringColumnValue(NAME);
	}

	@Override
	public String getSsn() {
		return getStringColumnValue(SSN);
	}

	@Override
	public String getEmail() {
		return getStringColumnValue(EMAIL);
	}

	@Override
	public String getPhoneHome() {
		return getStringColumnValue(PHONE_HOME);
	}

	@Override
	public String getPhoneWork() {
		return getStringColumnValue(PHONE_WORK);
	}

	@Override
	public String getCareOf() {
		return getStringColumnValue(CAREOF);
	}

	@Override
	public String getStreet() {
		return getStringColumnValue(STREET);
	}

	@Override
	public String getZipCode() {
		return getStringColumnValue(ZIP_CODE);
	}

	@Override
	public String getCity() {
		return getStringColumnValue(CITY);
	}

	@Override
	public String getCivilStatus() {
		return getStringColumnValue(CIVIL_STATUS);
	}

	@Override
	public boolean hasCohabitant() {
		boolean result = false;
		try {
			result = getBooleanColumnValue(HAS_COHABITANT);
		}
		catch (Exception e) {
			// nothing
		}

		return result;
	}

	@Override
	public int getChildrenCount() {
		Integer result = null;
		try {
			result = getIntegerColumnValue(CHILDREN_COUNT);
		}
		catch (Exception e) {
			// nothing
		}

		return result != null ? result.intValue() : 0;
	}

	@Override
	public String getApplicationReason() {
		return getStringColumnValue(APPLICATION_REASON);
	}

	// set methods for bean properties

	@Override
	public void setApplicantName(final String name) {
		setColumn(NAME, name);
	}

	@Override
	public void setSsn(final String ssn) {
		setColumn(SSN, ssn);
	}

	@Override
	public void setEmail(final String email) {
		setColumn(EMAIL, email);
	}

	@Override
	public void setPhoneHome(final String phone) {
		setColumn(PHONE_HOME, phone);
	}

	@Override
	public void setPhoneWork(final String phone) {
		setColumn(PHONE_WORK, phone);
	}

	@Override
	public void setCareOf(final String careOf) {
		setColumn(CAREOF, careOf);
	}

	@Override
	public void setStreet(final String street) {
		setColumn(STREET, street);
	}

	@Override
	public void setZipCode(final String rawZipCode) {
		final StringBuffer digitOnlyZipCode = new StringBuffer();
		for (int i = 0; i < rawZipCode.length(); i++) {
			if (Character.isDigit(rawZipCode.charAt(i))) {
				digitOnlyZipCode.append(rawZipCode.charAt(i));
			}
		}
		setColumn(ZIP_CODE, digitOnlyZipCode.toString());
	}

	@Override
	public void setCity(final String city) {
		setColumn(CITY, city);
	}

	@Override
	public void setCivilStatus(final String civilStatus) {
		setColumn(CIVIL_STATUS, civilStatus);
	}

	@Override
	public void setHasCohabitant(final boolean hasCohabitant) {
		setColumn(HAS_COHABITANT, new Boolean(hasCohabitant));
	}

	@Override
	public void setChildrenCount(final int childrenCount) {
		setColumn(CHILDREN_COUNT, childrenCount);
	}

	@Override
	public void setApplicationReason(final String applicationReason) {
		setColumn(APPLICATION_REASON, applicationReason);
	}

	/**
	 * Finds all cases for all users with the specified caseStatus and the associated caseCode
	 */
	@Override
	public Collection ejbFindAllCasesByStatus(CaseStatus caseStatus) throws FinderException {
		return super.ejbFindAllCasesByStatus(caseStatus.getStatus());
	}

	/**
	 * Finds all cases for all users with the specified caseStatus and the associated caseCode
	 */
	@Override
	public Collection ejbFindAllCasesByStatus(String caseStatus) throws FinderException {
		return super.ejbFindAllCasesByStatus(caseStatus);
	}

	/**
	 * Returns the total count of all CitizenAccountApplications
	 *
	 * @return int Number of records
	 * @throws IDOException
	 */
	public int ejbHomeGetTotalCount() throws IDOException {
		IDOQuery query = idoQuery();
		query.appendSelectCountFrom(this);
		return this.idoGetNumberOfRecords(query);
	}

	public int ejbHomeGetCount(String personalID, String status) throws IDOException {
		Table table = new Table(this);
		Table pCase = new Table(Case.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(new WildCardColumn(table));
		query.addJoin(table, pCase);
		query.addCriteria(new MatchCriteria(table, SSN, MatchCriteria.EQUALS, personalID));
		query.addCriteria(new MatchCriteria(pCase, "case_status", MatchCriteria.EQUALS, status));

		return idoGetNumberOfRecords(query);
	}

}