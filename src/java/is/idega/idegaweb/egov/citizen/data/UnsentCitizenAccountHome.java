package is.idega.idegaweb.egov.citizen.data;


import java.util.Collection;
import javax.ejb.CreateException;
import com.idega.data.IDOHome;
import javax.ejb.FinderException;

public interface UnsentCitizenAccountHome extends IDOHome {
	public UnsentCitizenAccount create() throws CreateException;

	public UnsentCitizenAccount findByPrimaryKey(Object pk)
			throws FinderException;

	public Collection findAll() throws FinderException;
}