package is.idega.idegaweb.egov.citizen.wsclient.landsbankinn;

public class BankAccount {
	public static final String bank_field = "bank";
	public static final String account_type_field = "account_type";
	public static final String account_number_field = "account_number";

	private String bank;
	private String account_type;
	private String account_number;
	
	public String getBank() {
		return bank;
	}
	public void setBank(String bank) {
		this.bank = bank;
	}
	public String getAccount_type() {
		return account_type;
	}
	public void setAccount_type(String accountType) {
		account_type = accountType;
	}
	public String getAccount_number() {
		return account_number;
	}
	public void setAccount_number(String accountNumber) {
		account_number = accountNumber;
	}
}
