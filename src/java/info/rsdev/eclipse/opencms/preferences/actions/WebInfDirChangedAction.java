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
package info.rsdev.eclipse.opencms.preferences.actions;

import info.rsdev.eclipse.opencms.OpenCmsModuleNature;
import info.rsdev.eclipse.opencms.library.OpenCmsClasspathInitializer;
import info.rsdev.eclipse.opencms.library.OpenCmsLibraryContainer;
import info.rsdev.eclipse.opencms.loader.OpenCmsClasspathManager;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;


/**
 * @author Dave Schoorl
 *
 */
public class WebInfDirChangedAction implements PreferenceChangedAction {
	
	public WebInfDirChangedAction() {
		//System.out.println("Initializing WebInfDirChangedAction");
	}

	/* (non-Javadoc)
	 * @see info.rsdev.eclipse.opencms.developer.actions.PreferenceChangedAction#execute(java.lang.String, java.lang.String)
	 */
	public void execute(String oldValue, String newValue) {
		updateClassLoader(oldValue, newValue);
		updateOpenCmsLibrary(oldValue, newValue);
	}
	
	private void updateClassLoader(String oldValue, String newValue) {
		OpenCmsClasspathManager.getInstance().changeWebInfLocation(oldValue, newValue);
	}
	
	private void updateOpenCmsLibrary(String oldValue, String newValue) {
		//TODO: go through all projects in the workspace and update the container
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (int i=0;i < projects.length; i++) {
			IProject project = projects[i];
			if ((project.isOpen()) && (isOpenCmsModuleProject(project))) {
				try {
					IProjectNature openCmsNature = project.getNature(OpenCmsModuleNature.NATURE_ID);
					if (openCmsNature != null) {
//						openCmsNature.deconfigure();
//						openCmsNature.configure();
					}
					IJavaProject javaProject = JavaCore.create(project);
					IClasspathEntry libraryEntry = getLibraryClasspathEntry(javaProject);
					if (libraryEntry != null) {
						IPath containerPath = libraryEntry.getPath();
						IClasspathContainer container = new OpenCmsLibraryContainer(containerPath);
						JavaCore.setClasspathContainer(containerPath,
								new IJavaProject[] { javaProject },
								new IClasspathContainer[] { container }, null);
					}
				} catch (CoreException ce) {
					ce.printStackTrace(System.out);
				}
			}
		}
	}

	private IClasspathEntry getLibraryClasspathEntry(IJavaProject javaProject) throws CoreException {
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		
		for (int i = 0; i < entries.length; ++i) {
			IPath path = entries[i].getPath();
			int segmentCount = path.segmentCount();
			if (segmentCount > 0) {
				if (OpenCmsClasspathInitializer.ID.equals(path.segment(0))) {
					return entries[i];
				}
			}
		}
		
		return null;
		
	}

	private boolean isOpenCmsModuleProject(IProject project) {
		try {
			String[] natureIds = project.getDescription().getNatureIds();
			for (int i=0; i < natureIds.length; i++) {
				if (OpenCmsModuleNature.NATURE_ID.equals(natureIds[i])) {
					return true;
				}
			}
		} catch (CoreException e) {
		}
		return false;
	}

}
