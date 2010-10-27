package is.idega.idegaweb.egov.citizen.wsclient.landsbankinn;

public class VerifyBankAccount {
	public static final String session_id_field = "session_id";
	public static final String personal_id_field = "personal_id";
	public static final String bank_account_field = "bank_account";

	public static final String xsi_no_nmspc_field = "xsi_no_nmspc";
	public static final String xsi_field = "xsi";
	public static final String version_field = "version";
	
	private String session_id;
	private String personal_id;
	private BankAccount bank_account;
	private String xsi_no_nmspc = "https://b2b.fbl.is/schema/LI_Fyrirspurn_er_reikningur_til.xsd";
	private String xsi = "http://www.w3.org/2001/XMLSchema-instance";
	private String version = "1.1";
	
	public String getSession_id() {
		return session_id;
	}
	public void setSession_id(String sessionId) {
		session_id = sessionId;
	}
	public String getPersonal_id() {
		return personal_id;
	}
	public void setPersonal_id(String personalId) {
		personal_id = personalId;
	}
	public BankAccount getBank_account() {
		return bank_account;
	}
	public void setBank_account(BankAccount bankAccount) {
		bank_account = bankAccount;
	}
	public String getXsi_no_nmspc() {
		return xsi_no_nmspc;
	}
	public void setXsi_no_nmspc(String xsiNoNmspc) {
		xsi_no_nmspc = xsiNoNmspc;
	}
	public String getXsi() {
		return xsi;
	}
	public void setXsi(String xsi) {
		this.xsi = xsi;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

}
