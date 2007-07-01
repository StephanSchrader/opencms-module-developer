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

import info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsModuleDeveloperPreferencePage;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Dave Schoorl
 */
public class OpenCmsModuleDeveloperPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "info.rsdev.eclipse.opencms.module.developer";

	// The shared instance
	private static OpenCmsModuleDeveloperPlugin plugin;
	
	/**
	 * The constructor
	 */
	public OpenCmsModuleDeveloperPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static OpenCmsModuleDeveloperPlugin getDefault() {
		return plugin;
	}

	protected void initializeDefaultPluginPreferences() {
		//TODO: use new (default) preferences mechanism
		super.initializeDefaultPluginPreferences();
		getPluginPreferences().setDefault(OpenCmsModuleDeveloperPreferencePage.OPENCMS_MAKE_LIBRARY, true);
		getPluginPreferences().setDefault(OpenCmsModuleDeveloperPreferencePage.OPENCMS_KEEP_ALIVE, true);
	}
	
	

}
