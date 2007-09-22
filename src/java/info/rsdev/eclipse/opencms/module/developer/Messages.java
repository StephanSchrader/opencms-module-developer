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

import org.eclipse.osgi.util.NLS;

/**
 * @author Dave Schoorl
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

	public static String preferences_webinf_dir;
	public static String preferences_additional_jars;
	public static String preferences_select_additional_jars;
	public static String preferences_keep_alive;
	public static String preferences_make_library;
	public static String preferences_servlet_mapping;
	public static String preferences_webappname;
	public static String preferences_projectname;
	public static String preferences_username;
	public static String preferences_password;
	
	public static String wizard_project_title;
	public static String wizard_project_description;
	public static String wizard_details_title;
	public static String wizard_details_description;
	public static String wizard_import_module;
	public static String wizard_create_templates;
	public static String wizard_create_elements;
	public static String wizard_create_resources;
	public static String wizard_create_classes;
	public static String wizard_create_lib;
	public static String wizard_create_source;
	
	public static String wizard_label_modulename;
	public static String wizard_label_nicename;
	public static String wizard_label_description;
	public static String wizard_label_version;
	public static String wizard_label_modulegroup;
	public static String wizard_label_actionclass;
	public static String wizard_label_authorname;
	public static String wizard_label_authoremail;
	
	public static String wizard_label_fetchmodules;
	public static String wizard_tooltip_fetchmodules;
	
	public static String task_establish_connection;
	public static String task_start_opencms;
	public static String task_configure_opencms;
	public static String task_initialize_opencms;
	public static String task_login_opencms;
	public static String task_shutdown_opencms;
	public static String task_fetch_modules;
	
	
	public static String opencms_library_name;
	public static String copy_finished;
	public static String get_finished;
	public static String copy_and_publish_finished;
	public static String operation_finished_title;
	public static String operation_finished_message;
	
	public static String exception_no_module_name;
	public static String exception_incomplete_connection_details;
	public static String exception_project_already_exist;
	public static String exception_no_such_module;
	public static String exception_unsupported_file_type;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
