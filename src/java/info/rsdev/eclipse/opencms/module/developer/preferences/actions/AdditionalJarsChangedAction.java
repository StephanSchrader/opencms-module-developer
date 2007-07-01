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
package info.rsdev.eclipse.opencms.module.developer.preferences.actions;

import info.rsdev.eclipse.opencms.module.developer.loader.OpenCmsClasspathManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Dave Schoorl
 *
 */
public class AdditionalJarsChangedAction implements PreferenceChangedAction {

	public AdditionalJarsChangedAction() {
		//System.out.println("Initializing AdditionalJarsChangedAction");
	}

	/* (non-Javadoc)
	 * @see info.rsdev.eclipse.opencms.module.developer.actions.PreferenceChangedAction#execute(java.lang.String, java.lang.String)
	 */
	public void execute(String oldValue, String newValue) {
		List oldFileNames = new ArrayList();
		List newFileNames = new ArrayList();
		//transform String to List for both old and new value
		if ((oldValue != null) && (oldValue.length() > 0)) {
			oldFileNames.addAll(Arrays.asList(oldValue.split("\\?")));
		}
		if ((newValue != null) && (newValue.length() > 0)) {
			newFileNames.addAll(Arrays.asList(newValue.split("\\?")));
		}
		
		updateClassLoader(oldFileNames, newFileNames);
		updateOpenCmsLibrary(oldFileNames, newFileNames);
	}
	
	private void updateClassLoader(List oldFileNames, List newFileNames) {
		OpenCmsClasspathManager.getInstance().changeAdditionalJars(oldFileNames, newFileNames);
	}
	
	private void updateOpenCmsLibrary(List oldFileNames, List newFileNames) {
		//TODO: go through all projects in the workspace and update the container
		//IWorkspaceRoot root = null;
	}
	
}
