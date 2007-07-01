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
package org.opencms.main;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import info.rsdev.eclipse.opencms.module.developer.ExceptionUtils;
import info.rsdev.eclipse.opencms.module.developer.Messages;
import info.rsdev.eclipse.opencms.module.developer.OpenCmsModuleDeveloperPlugin;
import info.rsdev.eclipse.opencms.module.developer.loader.OpenCmsClassLoader;
import info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsModuleDeveloperPreferencePage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;

/**
 * @author Dave Schoorl
 *
 */
public class CommunicatorUtils {
	
	private CommunicatorUtils() {}
	
	public static ICommunicator getCommunicator() throws CoreException {
		Thread currentThread = Thread.currentThread();
		ClassLoader oldLoader = currentThread.getContextClassLoader();
		ClassLoader newLoader = OpenCmsClassLoader.getInstance();
		currentThread.setContextClassLoader(newLoader);
		ICommunicator communicator = null;
		try {
			Class communicatorClass = Class.forName("org.opencms.main.Communicator", false, newLoader);
			Method instantiater = communicatorClass.getMethod("getInstance", new Class[] {});
			communicator = (ICommunicator)instantiater.invoke(communicatorClass, new Object[]{});
		} catch (Throwable t) {
			ExceptionUtils.throwCoreException(t);
			close(communicator, true);
		} finally {
			currentThread.setContextClassLoader(oldLoader);
		}
		
		return communicator;
	}
	
	public static boolean isProperlyConfigured() throws CoreException {
		Preferences preferences = OpenCmsModuleDeveloperPlugin.getDefault().getPluginPreferences();
		boolean isProperlyConfigured = true;
		
		//The following preferences must have values in order to start OpenCms
		List mandatoryPreferences = new ArrayList();
		mandatoryPreferences.add(preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_WEBINF_DIR));
		mandatoryPreferences.add(preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_WEBAPP_NAME));
		mandatoryPreferences.add(preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_SERVLET_MAPPING));
		mandatoryPreferences.add(preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_USERNAME));
		mandatoryPreferences.add(preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_PASSWORD));
		
		Iterator preferencesIterator = mandatoryPreferences.iterator();
		while (preferencesIterator.hasNext() && isProperlyConfigured) {
			String preference = (String)preferencesIterator.next();
			if ((preference == null) || (preference.trim().length() == 0)) {
				isProperlyConfigured = false;
			}
		}
		
		if (isProperlyConfigured == false) {
			String message = Messages.exception_incomplete_connection_details;
			IStatus status = new Status(IStatus.ERROR, OpenCmsModuleDeveloperPlugin.PLUGIN_ID, -1, message, null);
			throw new CoreException(status);
		}
		
		return isProperlyConfigured;
	}
	
	public static void close(ICommunicator communicator, boolean force) {
		if (communicator != null) {
			Preferences preferences = OpenCmsModuleDeveloperPlugin.getDefault().getPluginPreferences();
			boolean keepAlive = preferences.getBoolean(OpenCmsModuleDeveloperPreferencePage.OPENCMS_KEEP_ALIVE);
			if (force || !keepAlive) {
				communicator.close();
			}
		}
	}


}
