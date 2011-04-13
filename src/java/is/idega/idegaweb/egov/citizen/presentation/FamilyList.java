package is.idega.idegaweb.egov.citizen.presentation;

import is.idega.block.family.business.FamilyLogic;
import is.idega.block.family.business.NoChildrenFound;
import is.idega.block.family.business.NoSpouseFound;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;
import com.idega.user.data.User;
import com.idega.user.util.UserComparator;
import com.idega.util.text.Name;

public class FamilyList extends CitizenBlock {

	@Override
	public void present(IWContext iwc) {
		try {
			if (iwc.isLoggedOn()) {
				List<User> family = new ArrayList<User>();
				family.add(iwc.getCurrentUser());
				
				try {
					family.addAll(getFamilyLogic(iwc).getChildrenInCustodyOf(iwc.getCurrentUser()));
				}
				catch (NoChildrenFound e) {
					//No children for user...
				}
				
				try {
					family.add(getFamilyLogic(iwc).getSpouseFor(iwc.getCurrentUser()));
				}
				catch (NoSpouseFound e) {
					//No spouse for user...
				}
				
				Collections.sort(family, new UserComparator(iwc.getCurrentLocale()));
				
				Lists list = new Lists();
				list.setStyleClass("familyList");
				add(list);
				
				for (User user : family) {
					ListItem item = new ListItem();
					item.add(new Text(new Name(user.getFirstName(), user.getMiddleName(), user.getLastName()).getName(iwc.getCurrentLocale())));
					list.add(item);
				}
			}
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	private FamilyLogic getFamilyLogic(IWApplicationContext iwac) {
		try {
			return (FamilyLogic) IBOLookup.getServiceInstance(iwac, FamilyLogic.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}