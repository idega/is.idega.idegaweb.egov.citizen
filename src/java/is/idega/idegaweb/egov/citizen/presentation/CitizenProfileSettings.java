package is.idega.idegaweb.egov.citizen.presentation;

import com.idega.presentation.IWContext;

public class CitizenProfileSettings extends CitizenAccountPreferences {

	@Override
	protected void viewForm(IWContext iwc) throws Exception {
		
	}
	
	@Override
	protected boolean updatePreferences(IWContext iwc) throws Exception {
		if (!super.updatePreferences(iwc))
			return false;
		
		//	TODO
		return true;
	}
	
}