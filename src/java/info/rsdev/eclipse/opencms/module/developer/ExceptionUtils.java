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
package info.rsdev.eclipse.opencms.module.developer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Dave Schoorl
 *
 */
public class ExceptionUtils {
	
	private ExceptionUtils() {}
	
	public static void throwCoreException(Throwable t) throws CoreException {
		CoreException ce = makeCoreException(t);
		throw ce;
	}
	
	/**
	 * Turn the Throwable into a CoreException (if it not yet is one) and
	 * output it to the Eclipse Log-file.
	 * @param t
	 * @return
	 */
	public static CoreException makeCoreException(Throwable t) {
		CoreException ce = null;
		if (t instanceof CoreException) {
			ce = (CoreException)t;
		} else {
			//turn the Throwable in a IStatus-object
			IStatus status = transform(t);
			
			//But if the Throwable has one or more causes, I want to show the root cause,
			//since these are normally the most descriptive
			if (hasCause(t)) {
				Throwable cause = getRootCause(t);
				String rootMessage = cause.toString();
				
				//set rootMessage as the message of the status-object
				status = setMessage(status, rootMessage);
			}
			
			ce = new CoreException(status);
		}
		
		//show message of root exception log message to logfile
		OpenCmsModuleDeveloperPlugin.getDefault().getLog().log(ce.getStatus());
		
		return ce;
	}
	
	private static IStatus setMessage(IStatus status, String rootMessage) {
		Throwable t = status.getException();
		int code = status.getCode();
		int severity = status.getSeverity();
		String pluginId = status.getPlugin();
		IStatus[] children = status.getChildren();
		IStatus newStatus = null;
		if (status.isMultiStatus()) {
			newStatus = new MultiStatus(pluginId, code, children, rootMessage, t);
		} else {
			newStatus = new Status(severity, pluginId, code, rootMessage, t);
		}
		
		return newStatus;
	}

	public static void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, OpenCmsModuleDeveloperPlugin.PLUGIN_ID, -1, message, null);
		throw new CoreException(status);
	}
	
	public static void showErrorDialog(CoreException ce, Shell shell) {
		if (shell != null) {
			ErrorDialog dialog = new ErrorDialog(shell, "Error", null, ce.getStatus(), ErrorDialog.DIALOG_DEFAULT_BOUNDS);
			dialog.open();
		}
	}
	
	private static Throwable getRootCause(Throwable t) {
		Throwable parentCause = t;
		while (hasCause(parentCause)) {
			parentCause = parentCause.getCause();
		}
		return parentCause;
	}
	
	private static IStatus transform(Throwable t) {
		if (t == null) { return null; }
		
		if (hasCause(t)) {
			MultiStatus multiStatus = new MultiStatus(OpenCmsModuleDeveloperPlugin.PLUGIN_ID, Status.ERROR, t.toString(), t);
			multiStatus.add(transform(t.getCause()));
			return multiStatus;
		} else {
			return new Status(Status.ERROR, OpenCmsModuleDeveloperPlugin.PLUGIN_ID, Status.OK, t.toString(), t);
		}
	}
	
	private static boolean hasCause(Throwable t) {
		if (t == null) {
			return false;
		}
		return (t.getCause() != null);
	}

}
