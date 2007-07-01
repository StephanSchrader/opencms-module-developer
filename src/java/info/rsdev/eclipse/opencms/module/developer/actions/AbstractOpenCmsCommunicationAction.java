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

import info.rsdev.eclipse.opencms.module.developer.ExceptionUtils;
import info.rsdev.eclipse.opencms.module.developer.Messages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.opencms.main.CommunicatorUtils;
import org.opencms.main.ICommunicator;

/**
 * @author Dave Schoorl
 *
 */
public abstract class AbstractOpenCmsCommunicationAction implements IObjectActionDelegate {

	protected ISelection selection;
	
	protected Shell shell = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public final void run(IAction action) {
		ICommunicator communicator = null;
		try {
			if (CommunicatorUtils.isProperlyConfigured()) {
				communicator = CommunicatorUtils.getCommunicator();
				execute(action, communicator);
				showOperationFinishedMessage();
			}
		} catch (CoreException t) {
			ExceptionUtils.showErrorDialog(t, shell);
		} finally {
			CommunicatorUtils.close(communicator, false);
		}
	}
	
	public abstract void execute(IAction action, ICommunicator communicator) throws CoreException ;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.shell = targetPart.getSite().getShell();
	}

	protected void showOperationFinishedMessage() {
		if (this.shell != null) {
			MessageDialog dialog = new MessageDialog(shell, Messages.operation_finished_title, null, Messages.operation_finished_message, MessageDialog.INFORMATION, new String[] {"OK"}, 0);
			dialog.open();
		}
	}
	
}
