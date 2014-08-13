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
package info.rsdev.eclipse.opencms.library;

import info.rsdev.eclipse.opencms.Messages;
import info.rsdev.eclipse.opencms.OpenCmsModuleDeveloperPlugin;
import info.rsdev.eclipse.opencms.preferences.OpenCmsModuleDeveloperPreferencePage;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Dave Schoorl
 *
 */
public class OpenCmsLibraryContainer implements IClasspathContainer {
	public static final String CONTAINER_ID = "OpenCmsLibLocation";
	private IPath containerPath = null;
	private File webinfLibLocation = null;
	private File openCmsSourceLocation = null;
	
	@SuppressWarnings("unused")
    private File webinfClassesLocation = null;
	
	private File[] additionalJars = null;
	
	private IClasspathEntry[] classpath = null;

    public OpenCmsLibraryContainer(IPath containerPath) throws CoreException {
		IPreferenceStore preferenceStore = OpenCmsModuleDeveloperPlugin.getInstance().getPreferenceStore();

		String webinfLocation = preferenceStore.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_WEBINF_DIR);
		this.setWebInfLocation(webinfLocation);
		
		//also add additional jars to the Library container
		String additionalJars = preferenceStore.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_ADDITIONAL_JARS);
		this.additionalJars = getFileEntries(additionalJars);

		//also add additional jars to the Library container
		String openCmsSourceLocation = preferenceStore.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_SRC_DIR);
		this.setOpenCmsSourceLocation(openCmsSourceLocation);
		
		
		this.containerPath = containerPath;
		this.classpath = generateClasspath();
	}
	
	private File[] getFileEntries(String additionalJars) {
		
		List<String> additionalJarList = null;
		if ((additionalJars != null) && (additionalJars.length() > 0)) {
			additionalJarList = Arrays.asList(additionalJars.split("\\?"));
		}

		return getFileEntries(additionalJarList);
	}

	private File[] getFileEntries(List<String> fileNames) {
		List<File> fileList = new ArrayList<File>();
		
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
		Vector<IClasspathEntry> entries = new Vector<IClasspathEntry>();
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
						String filename = file.getName();
						String path = file.getAbsolutePath();
						if (openCmsSourceLocation!=null &&  (filename.startsWith("org.opencms") || (filename.startsWith("opencms"))))
							entries.add(JavaCore.newLibraryEntry(new Path(path), new Path(openCmsSourceLocation.getAbsolutePath()), null));
						else
							entries.add(JavaCore.newLibraryEntry(new Path(path), null, null));
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
		
		Collections.sort(entries, new Comparator<IClasspathEntry>() {
			public int compare(IClasspathEntry e1, IClasspathEntry e2) {
				return e1.getPath().lastSegment().compareTo(e2.getPath().lastSegment());
			}
		});		
		
		
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

	public void changeAdditionalJars(List<String> oldJars, List<String> newJars) {
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

	public void setOpenCmsSourceLocation(String newDir) {
		File srcLocation = new File(newDir);
		if (srcLocation.exists()) 
		{	
			this.openCmsSourceLocation = srcLocation;
		}
	}
	
	
}