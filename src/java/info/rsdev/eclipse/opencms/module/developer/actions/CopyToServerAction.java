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
package info.rsdev.eclipse.opencms.module.developer.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.opencms.main.ICommunicator;

/**
 * @author Dave Schoorl
 *
 */
public class CopyToServerAction extends AbstractOpenCmsCommunicationAction {
	
	public void execute(IAction action, ICommunicator communicator) throws CoreException {
		
		if (selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection)selection;
			Iterator iterator = structuredSelection.iterator();
			while (iterator.hasNext()) {
				Object selectedItem = iterator.next();
				if (selectedItem instanceof IProject) {
					communicator.copyToServer((IProject)selectedItem);
				}
			}
		}
	}

}
