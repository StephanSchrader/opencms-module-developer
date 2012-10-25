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
package info.rsdev.eclipse.opencms.preferences;

import info.rsdev.eclipse.opencms.Messages;
import info.rsdev.eclipse.opencms.OpenCmsModuleDeveloperPlugin;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * @author Dave Schoorl
 *
 */
public class OpenCmsModuleDeveloperPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	public static final String OPENCMS_WEBINF_DIR = "info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsWebInfDir"; //$NON-NLS-1$

	public static final String OPENCMS_SRC_DIR = "info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsSrcDir"; //$NON-NLS-1$
	
	public static final String OPENCMS_ADDITIONAL_JARS = "info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsAdditionalJars";
	
	public static final String OPENCMS_KEEP_ALIVE = "info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsKeepAlive";
	
	public static final String OPENCMS_MAKE_LIBRARY = "info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsMakeLibrary";
	
	public static final String OPENCMS_SERVLET_MAPPING = "info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsServletMapping";
	
	public static final String OPENCMS_WEBAPP_NAME = "info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsWebappName";
	
	public static final String OPENCMS_PROJECT_NAME = "info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsProjectName";

	public static final String OPENCMS_USERNAME = "info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsUserName"; //$NON-NLS-1$
	
	public static final String OPENCMS_PASSWORD = "info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsPassword"; //$NON-NLS-1$
	
	private ScopedPreferenceStore preferences;
	
	private IPropertyChangeListener propertyChangeListener = null;
	
	public OpenCmsModuleDeveloperPreferencePage() {
		this(null, null);
	}
	
	/**
	 * @param title
	 */
	public OpenCmsModuleDeveloperPreferencePage(String title) {
		this(title, null);
	}

	/**
	 * @param title
	 * @param image
	 */
	@SuppressWarnings("deprecation")
    public OpenCmsModuleDeveloperPreferencePage(String title, ImageDescriptor image) {
		super(title, image, GRID);
		preferences = new ScopedPreferenceStore(new InstanceScope(), OpenCmsModuleDeveloperPlugin.PLUGIN_ID);
		setPreferenceStore(preferences);
		propertyChangeListener = new PropertyChangeListener();
		preferences.addPropertyChangeListener(propertyChangeListener);
	}

	protected void createFieldEditors() {
		DirectoryFieldEditor webinfDirEditor = new DirectoryFieldEditor(OPENCMS_WEBINF_DIR, Messages.preferences_webinf_dir, getFieldEditorParent());
		addField(webinfDirEditor);
		DirectoryFieldEditor srcDirEditor = new DirectoryFieldEditor(OPENCMS_SRC_DIR, Messages.preferences_src_dir, getFieldEditorParent());
		addField(srcDirEditor);
		ListEditor additionalJarsEditor = new FileListEditor(OPENCMS_ADDITIONAL_JARS, Messages.preferences_additional_jars, Messages.preferences_select_additional_jars, getFieldEditorParent());
		addField(additionalJarsEditor);
//		BooleanFieldEditor keepAliveEditor = new BooleanFieldEditor(OPENCMS_KEEP_ALIVE, Messages.preferences_keep_alive, getFieldEditorParent());
//		addField(keepAliveEditor);
//		BooleanFieldEditor makeLibraryEditor = new BooleanFieldEditor(OPENCMS_MAKE_LIBRARY, Messages.preferences_make_library, getFieldEditorParent());
//		addField(makeLibraryEditor);
		StringFieldEditor servletMappingEditor = new StringFieldEditor(OPENCMS_SERVLET_MAPPING, Messages.preferences_servlet_mapping, getFieldEditorParent());
		addField(servletMappingEditor);
		StringFieldEditor webappNameEditor = new StringFieldEditor(OPENCMS_WEBAPP_NAME, Messages.preferences_webappname, getFieldEditorParent());
		addField(webappNameEditor);
//		StringFieldEditor projectNameEditor = new StringFieldEditor(OPENCMS_PROJECT_NAME, Messages.preferences_projectname, getFieldEditorParent());
//		addField(projectNameEditor);
		StringFieldEditor userNameEditor = new StringFieldEditor(OPENCMS_USERNAME, Messages.preferences_username, getFieldEditorParent());
		addField(userNameEditor);
		StringFieldEditor passwordEditor = new StringFieldEditor(OPENCMS_PASSWORD, Messages.preferences_password, getFieldEditorParent());
		addField(passwordEditor);
	}
	
	public boolean performOk() {
		try {
			preferences.save();
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
		
		return super.performOk();
	}

	public void init(IWorkbench workbench) { }
	
	public void dispose() {
		preferences.removePropertyChangeListener(propertyChangeListener);
		propertyChangeListener = null;
		super.dispose();
	}
	
}
