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
package info.rsdev.eclipse.opencms.actions;

import info.rsdev.eclipse.opencms.ExceptionUtils;
import info.rsdev.eclipse.opencms.Messages;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
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
	public final void run(final IAction action) {
		try {
			if ((selection instanceof StructuredSelection) && (CommunicatorUtils.isProperlyConfigured())) {
				ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(shell);
				monitorDialog.run(true, true, new IRunnableWithProgress() {
					
					ICommunicator communicator = null;
					
					public void run(IProgressMonitor progressMonitor) throws InvocationTargetException {
						/* Setup the progress monitor: 5000 units of work for connecting to OpenCms
						 * and 1000 units of work for each project
						 */
						StructuredSelection structuredSelection = (StructuredSelection)selection;
						int numberOfSelectedProjects = structuredSelection.size();
						progressMonitor.beginTask(action.getDescription(), 5000+1000*numberOfSelectedProjects);
						try {
							communicator = CommunicatorUtils.getCommunicator(progressMonitor);
							progressMonitor.worked(5000);
							
							/* iterate over the selected projects and execute the action of the subclass 
							 * implementeation on each project
							 */
							@SuppressWarnings("unchecked")
                            Iterator<Object> iterator = structuredSelection.iterator();
							while (iterator.hasNext()) {
								Object selectedItem = iterator.next();
								if (selectedItem instanceof IResource) {
									execute((IResource)selectedItem, communicator, progressMonitor);
								}
								progressMonitor.worked(1000);
							}
						} catch (CoreException ce) {
							throw new InvocationTargetException(ce);
						}
						finally {
							CommunicatorUtils.close(communicator, false);
						}
					}
				});
			}
		} catch (CoreException t) {
			ExceptionUtils.showErrorDialog(t, shell);
		} catch (Exception e) {
			CoreException ce = null;
			if (e.getCause() instanceof CoreException) {
				ce = (CoreException)e.getCause();
			} else {
				ce = ExceptionUtils.makeCoreException(e);
			}
			ExceptionUtils.showErrorDialog(ce, shell);
		}
	}
	
	public abstract void execute(IResource resource, ICommunicator communicator, IProgressMonitor progressMonitor) throws CoreException ;

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
