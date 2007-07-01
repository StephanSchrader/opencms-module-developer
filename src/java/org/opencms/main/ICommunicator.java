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

import info.rsdev.eclipse.opencms.module.developer.data.OpenCmsModuleDescriptor;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Dave Schoorl
 *
 */
public interface ICommunicator {

	public void close();
	
	public void createModule(OpenCmsModuleDescriptor descriptor) throws CoreException;

	public void getFromServer(IProject javaProject) throws CoreException;
	
	public void copyToServer(IProject javaProject) throws CoreException;

	public void publish(IProject javaProject) throws CoreException;

	public List getModules() throws CoreException;

}