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
package info.rsdev.eclipse.opencms.module.developer.wizards;

import info.rsdev.eclipse.opencms.module.developer.ExceptionUtils;
import info.rsdev.eclipse.opencms.module.developer.OpenCmsModuleNature;
import info.rsdev.eclipse.opencms.module.developer.data.OpenCmsModuleDescriptor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.opencms.main.CommunicatorUtils;
import org.opencms.main.ICommunicator;

/**
 * Collect all information neccesary to create a module in OpenCms using a wizard
 * and create module in OpenCms after the user clicked 'finish' in the wizard.
 * 
 * @author Dave Schoorl
 */
public class NewModuleWizard extends Wizard implements INewWizard {

	private static final String WIZARD_ID = "info.rsdev.eclipse.opencms.module.developer.newmodulewizard";

	private IStructuredSelection initialSelection = null;

	private CreateProjectPage createProjectPage = null;

	private ProjectDetailsPage projectDetailsPage = null;

	private OpenCmsModuleDescriptor data = null;

	/**
	 * 
	 */
	public NewModuleWizard() {
	}

	public void addPages() {
		if (data == null) {
			data = new OpenCmsModuleDescriptor();
		}
		createProjectPage = new CreateProjectPage("Create OpenCms Module", data);
		addPage(createProjectPage);
		projectDetailsPage = new ProjectDetailsPage("bladibla", data);
		addPage(projectDetailsPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		//iterate over pages to collect user input
		IWizardPage[] pages = getPages();
		for (int i = 0; i < pages.length; i++) {
			if (pages[i] instanceof IExchanger) {
				((IExchanger) pages[i]).getWidgetValues();
			}
		}

		//Create new module in separate thread
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) 
					throws InvocationTargetException, InterruptedException 
				{
					try {
						createNewModule(monitor, data);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InterruptedException e) {
			// User canceled, so stop but don't close wizard.
			return false;
		} catch (Exception e) {
			CoreException ce = null;
			if (e.getCause() instanceof CoreException) {
				ce = (CoreException)e.getCause();
			} else {
				ce = ExceptionUtils.makeCoreException(e);
			}
			ExceptionUtils.showErrorDialog(ce, getShell());
			return false;
		}
		return true;
	}

	private void createNewModule(IProgressMonitor monitor, OpenCmsModuleDescriptor descriptor)
		throws CoreException
	{
		//Create project
		String moduleName = descriptor.getModuleName();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root= workspace.getRoot();
		IProject project= root.getProject(moduleName);
		project.create(monitor);
		project.open(monitor);
		
		//Set project natures
		IProjectDescription projectDescription = project.getDescription();
		projectDescription.setNatureIds(new String[] { OpenCmsModuleNature.NATURE_ID, JavaCore.NATURE_ID });
		project.setDescription(projectDescription, null);

		//Set classes as output folder
		IJavaProject javaProject = JavaCore.create(project);
		IFolder binDir = project.getFolder("classes");
		IPath binPath = binDir.getFullPath();
		javaProject.setOutputLocation(binPath, null);
		
		//Set source folder and default JRE
		createFolder(project, "src");
		IFolder srcFolder = project.getFolder("src");
		IClasspathEntry srcEntry = JavaCore.newSourceEntry(srcFolder.getFullPath());
		IClasspathEntry jreEntry = JavaRuntime.getDefaultJREContainerEntry();
		if (jreEntry == null) {
			javaProject.setRawClasspath(new IClasspathEntry[] { srcEntry }, null);
		} else { 
			javaProject.setRawClasspath(new IClasspathEntry[] { srcEntry, jreEntry }, null);
		}
		
		if (descriptor.isCreateElementsFolder()) {
			createFolder(project, "elements");
		}
		if (descriptor.isCreateResourcesFolder()) {
			createFolder(project, "resources");
		}
		if (descriptor.isCreateTemplateFolder()) {
			createFolder(project, "templates");
		}
		if (descriptor.isCreateLibFolder()) {
			createFolder(project, "lib");
		}
		
		//instantiate OpenCms Nature (which adds the OpenCms Library)
		IProjectNature openCmsNature = project.getNature(OpenCmsModuleNature.NATURE_ID);
		if (openCmsNature != null) {
			openCmsNature.configure();
		}
		
		//create or import OpenCms module
		ICommunicator communicator = null;
		try {
			if (CommunicatorUtils.isProperlyConfigured()) {
				communicator = CommunicatorUtils.getCommunicator(monitor);
				if (descriptor.isImportModule()) {
					communicator.getFromServer(project, monitor);
				} else {
					communicator.createModule(descriptor, monitor);
				}
			}
		} finally {
			CommunicatorUtils.close(communicator, false);
		}
	}
	
	private void createFolder(IProject project, String subFolderName) 
			throws CoreException {
		IFolder folder = project.getFolder(subFolderName);
		if ( !folder.exists()) {
			folder.create(true, true, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 *      org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.initialSelection = selection;
	}

}
