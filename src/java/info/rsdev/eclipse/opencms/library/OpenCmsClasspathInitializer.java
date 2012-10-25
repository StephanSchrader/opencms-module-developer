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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import info.rsdev.eclipse.opencms.OpenCmsModuleDeveloperPlugin;

/**
 * @author Dave Schoorl
 *
 */
public class OpenCmsClasspathInitializer extends ClasspathContainerInitializer {
	
	public static final String ID = "info.rsdev.eclipse.opencms.module.developer.OpenCmsClasspath";

	private IClasspathContainer container;
	
	public OpenCmsClasspathInitializer() { }

	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {

		int size = containerPath.segmentCount();
		if (size > 0) {
			if (containerPath.segment(0).equals(ID)) {
				container = new OpenCmsLibraryContainer(containerPath);
				JavaCore.setClasspathContainer(containerPath,
						new IJavaProject[] { project },
						new IClasspathContainer[] { container }, null);
			}
		}
	}
	
	public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) 
	{
		return true;
	}
	
	public IStatus getAttributeStatus(IPath containerPath, IJavaProject project, String attributeKey)
	{
		return new Status(IStatus.OK, OpenCmsModuleDeveloperPlugin.PLUGIN_ID, "");
	}
	
	public IStatus getAccessRulesStatus(IPath containerPath, IJavaProject project)
	{
		return new Status(IStatus.OK, OpenCmsModuleDeveloperPlugin.PLUGIN_ID, "");
	}
	
	public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject project, IClasspathContainer containerSuggestion) throws CoreException
	{
		JavaCore.setClasspathContainer(containerPath,
				new IJavaProject[] { project },
				new IClasspathContainer[] { containerSuggestion }, null);
	}

}
