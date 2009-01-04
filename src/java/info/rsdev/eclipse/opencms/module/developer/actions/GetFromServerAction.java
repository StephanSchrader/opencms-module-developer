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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.opencms.main.ICommunicator;

/**
 * @author Dave Schoorl
 *
 */
public class GetFromServerAction extends AbstractOpenCmsCommunicationAction {

	public void execute(IResource resource, ICommunicator communicator, IProgressMonitor progressMonitor) throws CoreException {
		communicator.getFromServer(resource, progressMonitor);
	}

}
