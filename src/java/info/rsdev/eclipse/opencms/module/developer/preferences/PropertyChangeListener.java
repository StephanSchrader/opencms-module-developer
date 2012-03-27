/**
 * Copyright (C) 2007 Red Star Development
 * All rights reserved. This source code and accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Star Development - initial API and implementation
 */
package info.rsdev.eclipse.opencms.module.developer.preferences;

import info.rsdev.eclipse.opencms.module.developer.preferences.actions.AdditionalJarsChangedAction;
import info.rsdev.eclipse.opencms.module.developer.preferences.actions.PreferenceChangedAction;
import info.rsdev.eclipse.opencms.module.developer.preferences.actions.WebInfDirChangedAction;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class PropertyChangeListener implements IPropertyChangeListener {
	
	private Map<String, PreferenceChangedAction> actions = new HashMap<String, PreferenceChangedAction>();
	
	public PropertyChangeListener() {
		//register the actions that must be executed when a certain preference changes
		actions.put(OpenCmsModuleDeveloperPreferencePage.OPENCMS_ADDITIONAL_JARS, new AdditionalJarsChangedAction());
		actions.put(OpenCmsModuleDeveloperPreferencePage.OPENCMS_WEBINF_DIR, new WebInfDirChangedAction());
	}

	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getProperty();
		String oldValue = event.getOldValue().toString();
		String newValue = event.getNewValue().toString();
		
		if ((actions != null) && (actions.containsKey(propertyName))) {
			PreferenceChangedAction action = (PreferenceChangedAction)actions.get(propertyName);
			action.execute(oldValue, newValue);
		}
	}
	
}
