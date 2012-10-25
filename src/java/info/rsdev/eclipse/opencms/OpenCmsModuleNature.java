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
package info.rsdev.eclipse.opencms;

import info.rsdev.eclipse.opencms.library.OpenCmsClasspathInitializer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Dave Schoorl
 *
 */
public class OpenCmsModuleNature implements IProjectNature {

	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID = OpenCmsModuleDeveloperPlugin.PLUGIN_ID + ".OpenCmsModuleNature";

	private IProject project;

	/* Add the OpenCms libraries to the Projects classpath. The path to the libraries are
	 * defined by the user in the preferences and point to the OpenCms installation on the 
	 * local machine.
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		
		for (int i = 0; i < entries.length; ++i) {
			IPath path = entries[i].getPath();
			int segmentCount = path.segmentCount();
			if (segmentCount > 0) {
				if (OpenCmsClasspathInitializer.ID.equals(path.segment(0))) {
					//Classpath already contains the OpenCmsLibrary container
					return;
				}
			}
		}
		
		// Add the OpenCmsLibrary container initializer as first in the list
		IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
		System.arraycopy(entries, 0, newEntries, 1, entries.length);
		newEntries[0] = JavaCore.newContainerEntry(new Path(OpenCmsClasspathInitializer.ID), false);
		
		javaProject.setRawClasspath(newEntries, null);
		project.setDescription(javaProject.getProject().getDescription(), null);
	}

	/* Remove the OpenCms libraries from the Projects classpath. The path to the libraries are
	 * defined by the user in the preferences and point to the OpenCms installation on the 
	 * local machine.
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		
		for (int i = 0; i < entries.length; ++i) {
			IPath path = entries[i].getPath();
			int segmentCount = path.segmentCount();
			if (segmentCount > 0) {
				if (OpenCmsClasspathInitializer.ID.equals(path.segment(0))) {
					//remove OpenCmsLibrary container from the projects classpath
					IClasspathEntry[] newEntries = new IClasspathEntry[entries.length - 1];
					System.arraycopy(entries, 0, newEntries, 0, i);
					System.arraycopy(entries, i + 1, newEntries, i, entries.length - i - 1);
					javaProject.setRawClasspath(newEntries, null);
					project.setDescription(javaProject.getProject().getDescription(), null);
					return;
				}
			}
		}
	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}
	
//	private void createOpenCmsLinkFolder(String linkFolderName, IPath path) throws CoreException {
//		IFolder linkFolder = project.getFolder(linkFolderName);
//		if (project.getWorkspace().validateLinkLocation(linkFolder, path).isOK()) {
//			if ( !linkFolder.exists()) {
//				linkFolder.createLink(path, IResource.NONE, null);
//			}
//		}
//	}
//	
//	private void removeOpenCmsLinkFolder(String linkFolderName) throws CoreException {
//		IFolder linkFolder = project.getFolder(linkFolderName);
//		if (linkFolder.exists()) {
//			linkFolder.delete(true, null);
//		}
//	}

}
