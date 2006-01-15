/*
 * $Id$
 * Created on Jan 15, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.citizen.presentation;

import java.sql.SQLException;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.user.data.User;


public class CitizenImage extends CitizenBlock {

	public void present(IWContext iwc) {
		IWBundle iwb = getBundle(iwc);
		
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("citizenImage");
		
		Image image = null;
		if (iwc.isLoggedOn()) {
			User user = iwc.getCurrentUser();
			if (user.getSystemImageID() > 0) {
				try {
					image = new Image(user.getSystemImageID());
				}
				catch (SQLException se) {
					se.printStackTrace();
				}
			}
		}
		
		if (image == null) {
			image = iwb.getImage("style/images/citizen_image.gif");
		}
		
		layer.add(image);
		
		add(layer);
	}
}