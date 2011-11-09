package is.idega.idegaweb.egov.citizen.bean;

import java.io.Serializable;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.dwr.business.DWRAnnotationPersistance;

@DataTransferObject
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LoginDataBean implements DWRAnnotationPersistance, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7714280371858491076L;
	@RemoteProperty
	private String address = null;
	@RemoteProperty
	private String userName = null;
	@RemoteProperty
	private String password = null;
	@RemoteProperty
	private String loginId = null;
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getLoginId() {
		return loginId;
	}
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}
}
