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
package info.rsdev.eclipse.opencms.module.developer.library;

import info.rsdev.eclipse.opencms.module.developer.Messages;
import info.rsdev.eclipse.opencms.module.developer.OpenCmsModuleDeveloperPlugin;
import info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsModuleDeveloperPreferencePage;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Dave Schoorl
 *
 */
public class OpenCmsLibraryContainer implements IClasspathContainer {
	
	public static final String CONTAINER_ID = "OpenCmsLibLocation";

	private IPath containerPath = null;
	
	private File webinfLibLocation = null;
	
	private File webinfClassesLocation = null;
	
	private File[] additionalJars = null;
	
	private IClasspathEntry[] classpath = null;

	public OpenCmsLibraryContainer(IPath containerPath) throws CoreException {
		Preferences preferences = OpenCmsModuleDeveloperPlugin.getDefault().getPluginPreferences();
		String webinfLocation = preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_WEBINF_DIR);
		this.setWebInfLocation(webinfLocation);
		
		//also add additional jars to the Library container
		String additionalJars = preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_ADDITIONAL_JARS);
		this.additionalJars = getFileEntries(additionalJars);
		
		this.containerPath = containerPath;
		this.classpath = generateClasspath();
	}
	
	private File[] getFileEntries(String additionalJars) {
		
		List additionalJarList = null;
		if ((additionalJars != null) && (additionalJars.length() > 0)) {
			additionalJarList = Arrays.asList(additionalJars.split("\\?"));
		}

		return getFileEntries(additionalJarList);
	}

	private File[] getFileEntries(List fileNames) {
		List fileList = new ArrayList();
		
		if (fileNames != null) {
			for (int i=0; i<fileNames.size(); i++) {
				File file = new File((String)fileNames.get(i));
				if ((file.exists()) && (file.isFile())) {
					fileList.add(file);
				}
			}
		}
		
		return (File[])fileList.toArray(new File[fileList.size()]);
	}
	
	private IClasspathEntry[] generateClasspath() {
		Vector entries = new Vector();
		try {
			//add classes directory to the classpath -- doesn't seem to accept classes folder outside the workspace
//			if (webinfClassesLocation.exists()) {
//				IPath filePath = new Path(webinfClassesLocation.getAbsolutePath());
//				IClasspathEntry entry = JavaCore.newLibraryEntry(filePath, null, null); 
//				entries.add(entry);
//			}
			
			//add all jar and zip files from the lib location to the container
			if (webinfLibLocation != null) {
				File[] files = webinfLibLocation.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						String fileName = pathname.getName().toLowerCase();
						if (pathname.isFile() && ((fileName.endsWith(".jar")) || (fileName.endsWith(".zip")))) {
							return true;
						}
						return false;
					}
				});
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (file.exists()) {
						IPath filePath = new Path(file.getAbsolutePath());
						entries.add(JavaCore.newLibraryEntry(filePath, null, null));
					}
				}
			}
			
			//and finally, add all additional jars to the library
			for (int i = 0; i < additionalJars.length; i++) {
				File file = additionalJars[i];
				if (file.exists()) {
					IPath filePath = new Path(file.getAbsolutePath());
					entries.add(JavaCore.newLibraryEntry(filePath, null, null));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return (IClasspathEntry[]) entries.toArray(new IClasspathEntry[entries.size()]);
	}

	public IClasspathEntry[] getClasspathEntries() {
		return classpath;
	}

	public String getDescription() {
		return Messages.opencms_library_name;
	}

	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	public IPath getPath() {
		return containerPath;
	}

	public void changeAdditionalJars(List oldJars, List newJars) {
		this.additionalJars = getFileEntries(newJars);
	}

	public void setWebInfLocation(String newDir) {
		
		if (newDir != null) {
			File webinfLocation = new File(newDir);
			if (webinfLocation.exists()) {
				//add lib and classes directories from WEB-INF to the Library container
				File libLocation = new File(webinfLocation, "lib");
				if (libLocation.exists()) {
					this.webinfLibLocation = libLocation;
				}
				File classesLocation = new File(webinfLocation, "classes");
				if (classesLocation.exists()) {
					this.webinfClassesLocation = classesLocation;
				}
				
				return;
			}
		}
		
		//when not a valid directory, reset the subfolders lib and classes
		this.webinfLibLocation = null;
		this.webinfClassesLocation = null;
	}

}