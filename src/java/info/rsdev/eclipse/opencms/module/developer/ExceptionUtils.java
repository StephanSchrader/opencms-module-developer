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
		CoreException ce = null;
		if (t instanceof CoreException) {
			ce = (CoreException)t;
		} else {
			IStatus status = transform(t);
			ce = new CoreException(status);
		}
		
		//log message to logfile
		OpenCmsModuleDeveloperPlugin.getDefault().getLog().log(ce.getStatus());
		
		throw ce;
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
		
		IStatus status = null;
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
