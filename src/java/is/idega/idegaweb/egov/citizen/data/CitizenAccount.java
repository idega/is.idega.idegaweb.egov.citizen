package is.idega.idegaweb.egov.citizen.data;

import com.idega.block.process.data.Case;
import com.idega.data.IDOEntity;

public interface CitizenAccount extends IDOEntity, AccountApplication, Case {

	String getCaseCodeDescription();

	String getCaseCodeKey();

	String getApplicantName();

	String getSsn();

	String getEmail();

	String getPhoneHome();

	String getPhoneWork();

	String getCareOf();

	String getStreet();

	String getZipCode();

	String getCity();

	String getCivilStatus();

	boolean hasCohabitant();

	int getChildrenCount();

	String getApplicationReason();

	void setApplicantName(String name);

	void setSsn(String ssn);

	void setEmail(String email);

	void setPhoneHome(String phoneHome);

	void setPhoneWork(String phoneWork);

	void setCareOf(String careOf);

	void setStreet(String street);

	void setZipCode(String zipCode);

	void setCity(String city);

	void setCivilStatus(String civilStatus);

	void setHasCohabitant(boolean hasCohabitant);

	void setChildrenCount(int childrenCount);

	void setApplicationReason(String applicationReason);
}
