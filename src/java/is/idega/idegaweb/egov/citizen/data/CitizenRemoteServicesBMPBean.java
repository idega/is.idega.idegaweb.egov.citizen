package is.idega.idegaweb.egov.citizen.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;

import javax.ejb.FinderException;

import com.idega.data.GenericEntity;
import com.idega.data.IDORelationshipException;
import com.idega.data.query.Column;
import com.idega.data.query.MatchCriteria;
import com.idega.data.query.SelectQuery;
import com.idega.data.query.Table;
import com.idega.user.data.User;
import com.idega.user.data.UserBMPBean;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.informix.util.stringUtil;

public class CitizenRemoteServicesBMPBean extends GenericEntity implements CitizenRemoteServices  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2148128643625647883L;

	private static final String TABLE_NAME = "comm_remote_services";
	
	private static final String COLUMN_NAME= "NAME";
	private static final String COLUMN_ADDRESS = "ADDRESS";
	
	private static final String COLUMN_USERS = "comm_services_users";
	@Override
	public void initializeAttributes() {
		addAttribute(getIDColumnName());
		
		addManyToManyRelationShip(User.class, COLUMN_USERS);
		addAttribute(COLUMN_NAME, "Name",String.class);
		addAttribute(COLUMN_ADDRESS, "address", String.class);
	}
	@Override
	public String getEntityName() {
		return TABLE_NAME;
	}
	@Override
	public String getAddress() {
		return getStringColumnValue(COLUMN_ADDRESS);
	}

	@Override
	public void setAddress(String address) {
		setColumn(COLUMN_ADDRESS, address);
	}
	@Override
	public String getServerName() {
		return getStringColumnValue(COLUMN_NAME);
	}
	@Override
	public void setServerName(String name) {
		setColumn(COLUMN_NAME, name);
	}
	

	@SuppressWarnings("unchecked")
	public Collection<Integer> ejbFindByServiceNames(Collection<String> names) {
		if(ListUtil.isEmpty(names)){
			return Collections.emptyList();
		}
		StringBuilder namesStringBuilder =  new StringBuilder();
		for(Iterator<String>iter = names.iterator();iter.hasNext();){
			String name = iter.next();
			if(!StringUtil.isEmpty(name)){
				namesStringBuilder.append(name);
				if(iter.hasNext()){
					namesStringBuilder.append(CoreConstants.COMMA);
				}
			}
		}
		String namesString = namesStringBuilder.toString();
		if(stringUtil.isANum(namesString)){
			return Collections.emptyList();
		}
		StringBuilder query = new StringBuilder("Select * FROM ").append(TABLE_NAME).append(" r WHERE r.")
				.append(COLUMN_NAME).append(" IN (").append(namesString).append(")");
		try {
			return idoFindPKsBySQL(query.toString());
		} catch (FinderException e) {
			getLogger().log(Level.WARNING, "failed finding remote services by names "+namesString);
			return Collections.emptyList();
		}
	}
	
	public Collection<Integer> ejbFindByServices(int maxAmount) throws FinderException {
		String query = "SELECT * FROM "  + TABLE_NAME;
		if(maxAmount > 0){
			return super.idoFindPKsBySQL(query, maxAmount);
		}else{
			return super.idoFindPKsBySQL(query);
		}
	}
	@Override
	@SuppressWarnings("unchecked")
	public Collection<User> getUsers()  throws IDORelationshipException {
		return idoGetRelatedEntities(User.class);
	}
	@Override
	public void AddUser(User user)  throws IDORelationshipException {
		idoAddTo(user, COLUMN_USERS);
	}
	@Override
	public void removeUser(User user) throws IDORelationshipException {
		idoRemoveFrom(user, COLUMN_USERS);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Integer> getServicesByUserId(String userId) throws IDORelationshipException, FinderException{
		SelectQuery query = idoSelectQuery();
		Table services = new Table(this);
		Table users = new Table(User.class);
		Table servicesUsers = new Table(COLUMN_USERS);
		
		query.addManyToManyJoin(services, users);
//		query.addCriteria(join);
		Column userIdColumn = users.getColumn(UserBMPBean.getColumnNameUserID());
		MatchCriteria userMatch = new MatchCriteria(userIdColumn, MatchCriteria.EQUALS, userId);
		query.addCriteria(userMatch);
		
		return super.idoFindPKsByQuery(query);
	}
}
