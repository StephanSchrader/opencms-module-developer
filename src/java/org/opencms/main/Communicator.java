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

import info.rsdev.eclipse.opencms.module.developer.ExceptionUtils;
import info.rsdev.eclipse.opencms.module.developer.Messages;
import info.rsdev.eclipse.opencms.module.developer.OpenCmsModuleDeveloperPlugin;
import info.rsdev.eclipse.opencms.module.developer.compatibility.CmsCompatibilityHelper;
import info.rsdev.eclipse.opencms.module.developer.data.OpenCmsModuleDescriptor;
import info.rsdev.eclipse.opencms.module.developer.loader.OpenCmsClassLoader;
import info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsModuleDeveloperPreferencePage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleManager;
import org.opencms.util.CmsPropertyUtils;
import org.opencms.util.CmsStringUtil;

/**
 * This class is responsible for starting a communication session with OpenCms. 
 * 
 * Thanks to Sami Honkonen for showing how to connect to OpenCms outside a
 * web container.
 *
 * @author Dave Schoorl
 */
public class Communicator implements ICommunicator {
	
	private static final String PROJECT = "Offline";
	
	private static final String MODULES_ROOT_FOLDER = "/system/modules";
	
	private static String OPENCMS_ENCODING = "UTF-8";
	
	private static final Boolean isCommunicationInProgress = Boolean.FALSE;
	
	private static final String EMPTY_STRING = "";
	
	private static final Log LOG = CmsLog.getLog(Communicator.class);
	private static ICommunicator instance;
	private CmsObject cmso;

	/**
	 * Create a new Communicator instance. This component is responsible for starting a 
	 * communication session with OpenCms. Thanks to Sami Honkonen for showing how to 
	 * connect to OpenCms outside a web container.
	 * 
	 */
	private Communicator(final IProgressMonitor progressMonitor) throws CoreException {
		if (progressMonitor != null) {
			progressMonitor.beginTask(Messages.task_start_opencms, 5000);
		}
		try {
			Preferences preferences = OpenCmsModuleDeveloperPlugin.getDefault().getPluginPreferences();
			String webinfLocation = preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_WEBINF_DIR);
			String servletMapping = preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_SERVLET_MAPPING);
			String webappName = preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_WEBAPP_NAME);
			String userName = preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_USERNAME);
			String password = preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_PASSWORD);
			OpenCmsCore opencms = null;
			
			try {
				if (progressMonitor != null) {
					progressMonitor.subTask(Messages.task_configure_opencms);
				}
				opencms = OpenCmsCore.getInstance();
				CmsCompatibilityHelper.initCmsSystemInfo(opencms.getSystemInfo(), webinfLocation, servletMapping, webappName);
				ExtendedProperties configuration = null;
				String propertyPath = opencms.getSystemInfo().getConfigurationFileRfsPath();
				configuration = CmsPropertyUtils.loadProperties(propertyPath);
				if (progressMonitor != null) {
					progressMonitor.worked(500);
				}
				
				//Start a timer task that updates the progressMonitor during the following long running operation
				if (progressMonitor != null) {
					progressMonitor.subTask(Messages.task_initialize_opencms);
				}
				
//				Thread progressBarUpdateThread = new Thread(new Runnable() {
//					
//					private boolean initializationInProgress = true;
//
//					public void run() {
//						try {
//							while(initializationInProgress) {
//								Display.getDefault().asyncExec(new Runnable() {
//						               public void run() {
//						            	   progressMonitor.worked(50);
//						               }
//						            });
//								this.wait(500);
//							}
//						} catch (InterruptedException e) {
//							initializationInProgress = false;
//						}
//					}
//					
//				});
//				progressBarUpdateThread.start();
				opencms = opencms.upgradeRunlevel(configuration);	//this is a longrunning operation when connection to OpenCms does not yet exist
				if (progressMonitor != null) {
					progressMonitor.worked(4000);
				}
				//Stop the timer task. The longrunning operation has finished
//				progressBarUpdateThread.interrupt();
	
			} catch (Exception t) {
				if (opencms != null) {
					try {
						opencms.shutDown();
					} catch (Exception ex) {}
				}
				OpenCmsClassLoader.markInvalid();
				ExceptionUtils.throwCoreException(t);
			}
			
			CmsProject project = null;
			try {
				if (progressMonitor != null) {
					progressMonitor.subTask(Messages.task_login_opencms);
				}
	            this.cmso = opencms.initCmsObject(opencms.getDefaultUsers().getUserGuest());
	            this.cmso.loginUser(userName, password);
				project = this.cmso.readProject(PROJECT);
				if (progressMonitor != null) {
					progressMonitor.worked(500);
				}
			} catch(Throwable t) {
				if (opencms != null) {
					close();
				}
				ExceptionUtils.throwCoreException(t);
			}
	        this.cmso.getRequestContext().setCurrentProject(project);
	        
	        //List configs = opencms.getConfigurationManager().getConfigurations();
	        CmsSystemConfiguration systemConfig = (CmsSystemConfiguration)opencms.getConfigurationManager().getConfiguration(CmsSystemConfiguration.class);
	        OPENCMS_ENCODING = systemConfig.getDefaultContentEncoding();
		} finally {
			if (progressMonitor != null) {
				progressMonitor.done();
			}
		}
	}

	public static ICommunicator getInstance(IProgressMonitor progressMonitor) throws CoreException {
		if (instance == null) {
			IProgressMonitor subProgressMonitor = null;
			if (progressMonitor != null) {
				subProgressMonitor = new SubProgressMonitor(progressMonitor, 5000);
			}
			instance = new Communicator(subProgressMonitor);
		}
		return instance;
	}

	public CmsObject getCmsObject() {
		return this.cmso;
	}
	
	/* (non-Javadoc)
	 * @see org.opencms.main.ICommunicator#close()
	 */
	public void close() {
		OpenCmsCore opencms = OpenCmsCore.getInstance();
		opencms.shutDown();
        Communicator.instance = null;
        this.cmso = null;
	}
	
	public List getModules(IProgressMonitor progressMonitor) throws CoreException {
		//Retrieve a list of installed modules
		List installedModules = null;
		if (installedModules == null) {
			installedModules = new ArrayList();
			CmsModuleManager moduleManager = OpenCms.getModuleManager();
			installedModules.addAll(moduleManager.getModuleNames());
			Collections.sort(installedModules);
		}
		
		List installedModulesWithResources = new ArrayList();
		CmsObject cms = getCmsObject();
		try {
			//Retrieve a list of folders from /system/modules
			List moduleFolders = cms.getSubFolders(MODULES_ROOT_FOLDER);
			for (int i=0; i < moduleFolders.size(); i++) {
				CmsFolder moduleFolder = (CmsFolder)moduleFolders.get(i);
				String moduleName = moduleFolder.getName();
				if (installedModules.contains(moduleName)) {
					installedModulesWithResources.add(moduleName);
				}
			}
		} catch (CmsException e) {
			ExceptionUtils.throwCoreException(e);
		}
		
		//Only return the installed modules that have a module directory
		return installedModulesWithResources;
	}
	
	public void createModule(OpenCmsModuleDescriptor descriptor, IProgressMonitor progressMonitor) throws CoreException {
		if ((descriptor == null) || (descriptor.getModuleName() == null)) {
			return;
		}
		
		String moduleName = descriptor.getModuleName();
		CmsModule newModule = new CmsModule();
		newModule.setName(moduleName);
		newModule.setActionClass(descriptor.getActionClassname());
		newModule.setAuthorEmail(descriptor.getAuthorEmail());
		newModule.setAuthorName(descriptor.getAuthorName());
		newModule.setCreateClassesFolder(descriptor.isCreateClassesFolder());
		newModule.setCreateElementsFolder(descriptor.isCreateElementsFolder());
		newModule.setCreateLibFolder(descriptor.isCreateLibFolder());
		newModule.setCreateModuleFolder(true);
		newModule.setCreateResourcesFolder(descriptor.isCreateResourcesFolder());
		newModule.setCreateTemplateFolder(descriptor.isCreateTemplateFolder());
		newModule.setDescription(descriptor.getDecription());
		newModule.setGroup(descriptor.getModuleGroupName());
		newModule.setNiceName(descriptor.getNiceName());
		//TODO: set version
		newModule.setResourceTypes(descriptor.getResourceTypes());
		newModule.setExplorerTypes(descriptor.getExplorerTypes());
		CmsObject cms = getCmsObject();
		CmsModuleManager moduleManager = OpenCms.getModuleManager();
		try {
			moduleManager.addModule(cms, newModule);
		} catch (Exception e) {
			ExceptionUtils.throwCoreException(e);
		}
	}
	
	public void getFromServer(IResource resource, IProgressMonitor progressMonitor)
			throws CoreException {
		
		if (resource instanceof IProject) {
			getFromServer((IProject)resource, progressMonitor );
		} else if (resource instanceof IFolder) {
			getFromServer((IFolder)resource, progressMonitor );
		} else if (resource instanceof IFile) {
			getFromServer((IFile)resource, progressMonitor );
		}
	}
	
	public void copyToServer(IResource resource, IProgressMonitor progressMonitor)
			throws CoreException {
		
		if (resource instanceof IFile) {
			copyToServer((IFile)resource, progressMonitor );
		}  else if (resource instanceof IFolder) {
			copyToServer((IFolder)resource, progressMonitor );
		} else if (resource instanceof IProject) {
			copyToServer((IProject)resource, progressMonitor );
		}
		
	}

	public void publish(IResource resource, IProgressMonitor progressMonitor)
			throws CoreException {
		
		if (resource instanceof IProject) {
			publish((IProject)resource, progressMonitor );
		} else if (resource instanceof IFolder) {
			publish((IFolder)resource, progressMonitor );
		} else if (resource instanceof IFile) {
			publish((IFile)resource, progressMonitor );
		}
	}	

	/* (non-Javadoc)
	 * @see org.opencms.main.ICommunicator#copyToServer(org.eclipse.jdt.core.IJavaProject)
	 */
	protected void copyToServer(IProject javaProject, IProgressMonitor progressMonitor) throws CoreException {
		CmsObject cms = getCmsObject();
		if (cms != null) {
			try {
				String eclipseProjectName = javaProject.getName();
				OpenCmsModuleDescriptor descriptor = new OpenCmsModuleDescriptor(eclipseProjectName);
				CmsFolder openCmsModuleFolder = getModuleFolder(cms, descriptor, true);
				if (openCmsModuleFolder != null) {
					IResource[] contents = javaProject.members();
					if (contents != null) {
						List remoteSubFolders = cms.getSubFolders(openCmsModuleFolder.getRootPath());
						if (remoteSubFolders == null) {
							remoteSubFolders = new ArrayList();
						}
						for (int i=0; i < contents.length; i++) {
							if (contents[i] instanceof IFolder) {
								preserveOpenCmsCounterpart(contents[i], remoteSubFolders);
								String folderName = ((IFolder)contents[i]).getName();
								if (canCopyToServer(folderName) ) {
									uploadFolderContents(cms, openCmsModuleFolder, (IFolder)contents[i]);
								}
							}
						}
						for (int i=0; i<remoteSubFolders.size(); i++) {
							CmsResource remoteResource = (CmsResource)remoteSubFolders.get(i);
							String remoteResourceName = remoteResource.getRootPath();
							cms.lockResource(remoteResourceName);
							CmsCompatibilityHelper.deleteResource(cms, remoteResource, remoteResourceName);
						}
					}
				}
			} catch (Throwable t) {
				ExceptionUtils.throwCoreException(t);
			}
		}
	}
	
	protected void copyToServer(IFolder folder, IProgressMonitor progressMonitor) throws CoreException {
		if (!canCopyToServer(folder.getProjectRelativePath().toString()))
			return;
		
		CmsObject cms = getCmsObject();
		if (cms != null) {
			try {
				String eclipseProjectName = folder.getProject().getName();
				OpenCmsModuleDescriptor descriptor = new OpenCmsModuleDescriptor(eclipseProjectName);
				CmsFolder openCmsModuleFolder = getModuleFolder(cms, descriptor, true);
				
				String parentPath = CmsResource.getParentFolder(openCmsModuleFolder.getRootPath() + folder.getProjectRelativePath());
				CmsFolder remoteParentFolder = getOpenCmsFolder(cms, parentPath);
				
				uploadFolderContents(cms, remoteParentFolder, folder);
				
			} catch (Throwable t) {
				ExceptionUtils.throwCoreException(t);
			}
		}
	}	
	
	protected void copyToServer(IFile file, IProgressMonitor progressMonitor) throws CoreException {
		if (!canCopyToServer(file.getProjectRelativePath().toString()))
			return;
		
		CmsObject cms = getCmsObject();
		if (cms != null) {
			try {
				String eclipseProjectName = file.getProject().getName();
				OpenCmsModuleDescriptor descriptor = new OpenCmsModuleDescriptor(eclipseProjectName);
				CmsFolder openCmsModuleFolder = getModuleFolder(cms, descriptor, true);
				
				String resourceName = openCmsModuleFolder.getRootPath() + file.getProjectRelativePath();
				String resourcePath = CmsResource.getFolderPath(resourceName);
				CmsFolder remoteFolder = getOpenCmsFolder(cms, resourcePath);
				
				boolean found = false;
				if (cms.existsResource(resourceName, CmsResourceFilter.IGNORE_EXPIRATION)) {
					found = true;
				} else {
					String resourceName1 = resourceName.substring(0, resourceName.lastIndexOf('.'));
					if (cms.existsResource(resourceName1, CmsResourceFilter.IGNORE_EXPIRATION)) {
						found = true;
						resourceName = resourceName1;
					}
				}

				CmsFile remoteFile = null;
				if(found) {
					try{
						remoteFile = cms.readFile(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
					} catch(CmsException e) {
						cms.lockResource(resourceName);
						cms.renameResource(resourceName, resourceName + ".bak");
						cms.unlockResource(resourceName + ".bak");
					}
				}

				uploadFileContents(cms, remoteFile, remoteFolder, file); 
				
			} catch (Throwable t) {
				ExceptionUtils.throwCoreException(t);
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see org.opencms.main.ICommunicator#getFromServer(org.eclipse.jdt.core.IJavaProject)
	 */
	protected void getFromServer(IProject javaProject, IProgressMonitor progressMonitor) throws CoreException {
		CmsObject cms = getCmsObject();
		if (cms != null) {
			try {
				String eclipseProjectName = javaProject.getName();
				OpenCmsModuleDescriptor descriptor = new OpenCmsModuleDescriptor(eclipseProjectName);
				CmsFolder openCmsModuleFolder = getModuleFolder(cms, descriptor, false);
				if (openCmsModuleFolder != null) {
					//download the module from the server and replace all local contents, 
					//except for the src and classes directories.
					String moduleFolderName = openCmsModuleFolder.getRootPath();
					List remoteResources = cms.getResourcesInFolder(moduleFolderName, CmsResourceFilter.IGNORE_EXPIRATION);
					Iterator remoteResourceIterator = remoteResources.iterator(); 
					while (remoteResourceIterator.hasNext()) {
						CmsResource remoteResource = (CmsResource)remoteResourceIterator.next();
						if (remoteResource.isFolder()) {
							String remoteResourceName = remoteResource.getName();
							if (canDownloadFromServer(remoteResourceName)) {
								downloadFolderContents(cms, javaProject, (CmsFolder)remoteResource);
							}
						}
					}
				}
			} catch (Throwable t) {
				ExceptionUtils.throwCoreException(t);
			}
		}
	}
	
	protected void getFromServer(IFolder folder, IProgressMonitor progressMonitor) throws CoreException {
		if (!canDownloadFromServer(folder.getProjectRelativePath().toString()))
			return;

		CmsObject cms = getCmsObject();
		if (cms != null) {
			try {
				
				String eclipseProjectName = folder.getProject().getName();
				OpenCmsModuleDescriptor descriptor = new OpenCmsModuleDescriptor(eclipseProjectName);
				CmsFolder openCmsModuleFolder = getModuleFolder(cms, descriptor, false);
				String folderName = openCmsModuleFolder.getRootPath() + folder.getProjectRelativePath();
				
				CmsFolder remoteFolder = null;
				try{
					remoteFolder = cms.readFolder(folderName, CmsResourceFilter.IGNORE_EXPIRATION);
				} catch(CmsException e){
					
				}
				if (null != remoteFolder) {
					downloadFolderContents(cms, folder.getParent(), remoteFolder);
				}

			} catch (Throwable t) {
				ExceptionUtils.throwCoreException(t);
			}
		}
	}	
	
	protected void getFromServer(IFile file, IProgressMonitor progressMonitor) throws CoreException {
		if (!canDownloadFromServer(file.getProjectRelativePath().toString()))
			return;

		CmsObject cms = getCmsObject();
		if (cms != null) {
			try {
				String eclipseProjectName = file.getProject().getName();
				OpenCmsModuleDescriptor descriptor = new OpenCmsModuleDescriptor(eclipseProjectName);
				CmsFolder openCmsModuleFolder = getModuleFolder(cms, descriptor, false);
				String resourceName = openCmsModuleFolder.getRootPath() + file.getProjectRelativePath();
				
				boolean found = false;
				if (cms.existsResource(resourceName, CmsResourceFilter.IGNORE_EXPIRATION)) {
					found = true;
				} else {
					String resourceName1 = resourceName.substring(0, resourceName.lastIndexOf('.'));
					if (cms.existsResource(resourceName1, CmsResourceFilter.IGNORE_EXPIRATION)) {
						found = true;
						resourceName = resourceName1;
					}
				}
				if (found) {
					CmsFile remoteFile = null;
					try{
						remoteFile = cms.readFile(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
					} catch(CmsException e){
						;
					}
					
					if (null != remoteFile) {
						if(file.exists()) {
							file.delete(true, null);
						}
						
						downloadFileContents(cms, file.getParent(), remoteFile);
					}
				}

			} catch (Throwable t) {
				ExceptionUtils.throwCoreException(t);
			}
		}
	}

	protected void publish(IProject javaProject, IProgressMonitor progressMonitor) throws CoreException {
		//throw new UnsupportedOperationException("Publishing is not yet implemented");
		CmsObject cms = getCmsObject();
		if (cms != null) {
			try {
				String eclipseProjectName = javaProject.getName();
				OpenCmsModuleDescriptor descriptor = new OpenCmsModuleDescriptor(eclipseProjectName);
				CmsFolder openCmsModuleFolder = getModuleFolder(cms, descriptor, false);
				String resourceName = openCmsModuleFolder.getRootPath();
				publishFolder(cms, resourceName);
			} catch (Throwable t) {
				ExceptionUtils.throwCoreException(t);
			}
		}
	}
	
	protected void publish(IFolder folder, IProgressMonitor progressMonitor) throws CoreException {
		//throw new UnsupportedOperationException("Publishing is not yet implemented");
		CmsObject cms = getCmsObject();
		if (cms != null) {
			try {
				String eclipseProjectName = folder.getProject().getName();
				OpenCmsModuleDescriptor descriptor = new OpenCmsModuleDescriptor(eclipseProjectName);
				CmsFolder openCmsModuleFolder = getModuleFolder(cms, descriptor, false);
				String resourceName = openCmsModuleFolder.getRootPath() + folder.getProjectRelativePath();
				publishFolder(cms, resourceName);
			} catch (Throwable t) {
				ExceptionUtils.throwCoreException(t);
			}
		}
	}
	
	protected void publish(IFile file, IProgressMonitor progressMonitor) throws CoreException {
		//throw new UnsupportedOperationException("Publishing is not yet implemented");
		CmsObject cms = getCmsObject();
		if (cms != null) {
			try {
				String eclipseProjectName = file.getProject().getName();
				OpenCmsModuleDescriptor descriptor = new OpenCmsModuleDescriptor(eclipseProjectName);
				CmsFolder openCmsModuleFolder = getModuleFolder(cms, descriptor, false);
				String resourceName = openCmsModuleFolder.getRootPath() + file.getProjectRelativePath();
				
				if(!cms.existsResource(resourceName, CmsResourceFilter.IGNORE_EXPIRATION)) {
					resourceName = resourceName.substring(0, resourceName.lastIndexOf('.'));
				}
				
				publishFile(cms, resourceName);
			} catch (Throwable t) {
				ExceptionUtils.throwCoreException(t);
			}
		}
	}		
	
	private void publishFolder(CmsObject cms, String parentFolderName) throws Exception {
		/* There has been a change in the public interface between OpenCms 6 and 7 related
		 * to the state of a resource (NEW, DELETED, UNCHANGED etc.). They have been refactored
		 * into a class CmsResourceState, which is new in OpenCms 7. This code should work for 
		 * both OpenCms 6 and 7, as long as CmsResourceState in OpenCms 7 is a singleton object 
		 * (like Boolean.FALSE, Boolean.TRUE etc.)
		 */
		CmsResource cmsResource = cms.readResource(parentFolderName, CmsResourceFilter.ALL);
		boolean stateChanged = CmsCompatibilityHelper.isChanged(cmsResource);
		boolean stateDeleted = CmsCompatibilityHelper.isDeleted(cmsResource);
		
		CmsLock lock = cms.getLock(parentFolderName);
        if (!lock.isNullLock() && stateChanged) {
            cms.unlockResource(parentFolderName);	//resource is locked, so unlock it
        }

        if (stateChanged) {
        	/* The commented code line below is the OpenCms 7.x way of publishing, but it is not
        	 * compatible with openCms 6.x, therefore we currently still keep on using the deprecated  
        	 * OpenCms 6.x way. In the future this might be relocated to the CmsCompatibilityHelper-class.
        	 */
//        	OpenCms.getPublishManager().publishResource(cms, parentFolderName);
        	cms.publishResource(parentFolderName);
        }
        
        //search down the tree for more publishable resources
        if ( !stateDeleted) {
			List childResources = cms.getResourcesInFolder(parentFolderName, CmsResourceFilter.ALL);
			for (int i=0; i < childResources.size(); i++) {
				CmsResource resource = (CmsResource)childResources.get(i);
				if (resource instanceof CmsFolder) {
					publishFolder(cms, resource.getRootPath());
				} else {
					publishFile(cms, resource.getRootPath());
				}
			}
        }
	}
	
	private void publishFile(CmsObject cms, String fileName) throws Exception {
		CmsResource cmsResource = cms.readResource(fileName, CmsResourceFilter.ALL);
		boolean stateChanged = CmsCompatibilityHelper.isChanged(cmsResource);
		
		CmsLock lock = cms.getLock(fileName);
        if (!lock.isNullLock() && stateChanged) {
            cms.unlockResource(fileName);	//resource is locked, so unlock it
        }
        
        if (stateChanged) {
        	/* The commented code line below is the OpenCms 7.x way of publishing, but it is not
        	 * compatible with openCms 6.x, therefore we currently still keep on using the deprecated  
        	 * OpenCms 6.x way. In the future this might be relocated to the CmsCompatibilityHelper-class.
        	 */
//        	OpenCms.getPublishManager().publishResource(cms, fileName);
        	cms.publishResource(fileName);
        }
        
	}
	
	private boolean canCopyToServer(String resourceName) {
		if(CmsStringUtil.isNotEmptyOrWhitespaceOnly(resourceName)
			&& !(resourceName.startsWith(".") || resourceName.toLowerCase().startsWith("test"))) {
			return true;
		} else {
			return false;
		}
	}

	private boolean canDownloadFromServer(String resourceName) {
		if(CmsStringUtil.isNotEmptyOrWhitespaceOnly(resourceName)
			&& !(resourceName.toLowerCase().startsWith("src") || resourceName.toLowerCase().startsWith("classes"))) {
			return true;
		} else {
			return false;
		}
	}
	
	
	
	private CmsFolder getModuleFolder(CmsObject cms, OpenCmsModuleDescriptor descriptor, boolean create) throws CmsException, CoreException {
		if (descriptor == null) { return null; }
		
		CmsModuleManager moduleManager = OpenCms.getModuleManager();
		String moduleName = descriptor.getModuleName();
		if (!moduleManager.hasModule(moduleName)) {
			if (create) {
				createModule(descriptor, null);
			} else {
				ExceptionUtils.throwCoreException(Messages.exception_no_such_module + moduleName);
			}
		}
		
		CmsFolder modulesFolder = getOpenCmsFolder(cms, "/system/modules/" + moduleName);
		return modulesFolder;
	}

	private CmsFolder getOpenCmsFolder(CmsObject cms, String folderPath) throws CmsException {
		//Strip forward slash as first character
		if (folderPath.startsWith("/")) {
			folderPath = folderPath.substring(1);
		}
		
		String[] pathElements = folderPath.split("/");
		String reconstructedPath = "";
		String correctedPath = "";
		for (int i=0; i < pathElements.length; i++) {
			reconstructedPath += "/" + pathElements[i];
			if (!cms.existsResource(reconstructedPath)) {
				pathElements[i] = createRemoteFolder(cms, reconstructedPath);
			}
			correctedPath += "/" + pathElements[i];
		}
		
		CmsFolder requestedFolder = cms.readFolder(correctedPath);
		return requestedFolder;
	}

	private String createRemoteFolder(CmsObject cms, String reconstructedPath) throws CmsException {
		int folderTypeId = OpenCmsConstants.OPENCMS_TYPE_FOLDER;
		List properties = new ArrayList();
		CmsResource cmsFolder = cms.createResource(reconstructedPath, folderTypeId, new byte[0], properties);
		return cmsFolder.getName();
	}
	
	/**
	 * Copy all contents from the OpenCms folder to the Eclipse parent folder. Folders that exist
	 * in both Eclipse as well as OpenCms are preserved: the files present in the eclipse folder 
	 * will be removed, but the folder itself not. This way, properties set on the folder will remain.
	 *   
	 * @param localParentFolder
	 * @param remoteFolder
	 * @throws CoreException
	 */
	private void downloadFolderContents(CmsObject cms, IContainer localParentFolder, CmsFolder remoteFolder) 
			throws CoreException, CmsException {
		String remoteFolderName = remoteFolder.getName();
		IResource localResource = localParentFolder.findMember(remoteFolderName);
		
		List localSubFolderNames = new ArrayList();
		IFolder localFolder = null;
		if (localResource == null) {
			//create folder in Eclipse workspace
			localFolder = getLocalFolder(localParentFolder, remoteFolderName);
			if (localFolder.exists() == false) {
				localFolder.create(true, true, null);
			}
		} else {
			//remove all files in this folder -- and collect all subfolders for further processing
			if (localResource instanceof IFolder) {
				localFolder = (IFolder)localResource;
			}
			IResource[] members = localFolder.members();
			if (members != null) {
				for (int i=0; i<members.length; i++) {
					if (members[i] instanceof IFolder) {
						String subFolderName = members[i].getName();
						localSubFolderNames.add(subFolderName);
					} else {
						members[i].delete(true, null);
					}
				}
			}
		}
		
		//Copy the remote folder's contents to Eclipse
		String remotePath = remoteFolder.getRootPath();
		List remoteFolderContents = cms.getResourcesInFolder(remotePath, CmsResourceFilter.DEFAULT);
		if (remoteFolderContents != null) {
			Iterator folderContentIterator = remoteFolderContents.iterator();
			while (folderContentIterator.hasNext()) {
				CmsResource remoteResource = (CmsResource)folderContentIterator.next();
				if (remoteResource.isFile()) {
					downloadFileContents(cms, localFolder, (CmsFile)remoteResource);
				} else {
					String remoteResourceName = remoteResource.getName();
					if (localSubFolderNames.contains(remoteResourceName)) {
						//preserve Eclipse folder when there is an OpenCms counterpart -- so that properties remain
						localSubFolderNames.remove(remoteResourceName);
					}
					IResource localSubResource = localParentFolder.findMember(remoteFolderName);
					IFolder localSubFolder = null;
					if (localSubResource == null) {
						localSubFolder = getLocalFolder(localParentFolder, remoteResourceName);
						//something is wrong: the folder is removed meanwhile in another process??
						if (localSubFolder.exists() == false) {
							localSubFolder.create(true, true, null);
						}
					} else {
						if (localSubResource instanceof IFolder) {
							localSubFolder = (IFolder)localSubResource;
						}
					}
					//recursively call downloading of subfolders
					downloadFolderContents(cms, localSubFolder, (CmsFolder)remoteResource);
				}
			}
		}
		
		//remove the remaining local subfolders that did not have a counterpart in OpenCms
		for (int i=0; i<localSubFolderNames.size(); i++) {
			String folderName = (String)localSubFolderNames.get(i);
			IFolder folder = getLocalFolder(localParentFolder, folderName);
			if (folder.exists()) {
				folder.delete(true, null);
			}
		}
	}
	
	private void downloadFileContents(CmsObject cms, IContainer localParentFolder, CmsFile remoteFile) 
		throws CoreException, CmsException {
		String path = remoteFile.getRootPath();
		int typeId = remoteFile.getTypeId();
		String fileName = correctOpenCmsFileName(remoteFile.getName(), typeId);
		//int length = remoteFile.getLength();
		byte[] fileContents = cms.readFile(path).getContents();
		
		IFile localFile = getFileHandle(localParentFolder, fileName);
		localFile.create(new ByteArrayInputStream(fileContents), true, null);
	}

	/**
	 * Copy all contents from the Eclipse parent folder to the OpenCms folder. Folders that exist
	 * in both Eclipse as well as OpenCms are preserved and files in Eclipse that have a counterpart 
	 * in OpenCms are preserved also, only their contents are replaced. This way, properties set 
	 * in OpenCms will remain. Files and folders present in OpenCms but not in Eclipse will be 
	 * deleted. However, deletion is only effective after publication of the OpenCms module. 
	 *   
	 * @param cms
	 * @param remoteParentFolder
	 * @param localFolder
	 * @throws CoreException
	 */
	private void uploadFolderContents(CmsObject cms, CmsFolder remoteParentFolder, IContainer localFolder) 
		throws CoreException, CmsException, IOException {
		
		//get a list of folder contents in OpenCms and Eclipse
		String remoteParentFolderName = remoteParentFolder.getRootPath();
		String remoteFolderName = remoteParentFolderName + localFolder.getName();
		CmsFolder remoteFolder = getOpenCmsFolder(cms, remoteFolderName);	//TODO: remove OpenCms file that has the same name as the requested folder 
		IResource[] localResources = localFolder.members();
		List remoteResources = cms.getResourcesInFolder(remoteFolder.getRootPath(), CmsResourceFilter.DEFAULT);
		if (remoteResources == null) {
			remoteResources = new ArrayList();
		}
		
		/* upload files -- replacing the file contents in OpenCms when it has a matching filename 
		 * and thus preserving the OpenCms file properties
		 */
		if ((localResources != null) && (localResources.length > 0)){
			for (int i=0; i < localResources.length; i++) {
				CmsResource remoteResource = preserveOpenCmsCounterpart(localResources[i], remoteResources);
				if (localResources[i] instanceof IFolder) {
					if ((remoteResource != null) && (remoteResource instanceof CmsFolder) == false) {
						//TODO: remove remote file that has the same name as the local folder
					}
					uploadFolderContents(cms, remoteFolder, (IFolder)localResources[i]);
				} else {
					if (localResources[i] instanceof IFile) {
						CmsFile remoteFile = null;
						if (remoteResource instanceof CmsFile) {
							remoteFile = (CmsFile)remoteResource;
						} else {
							//TODO: remove remote folder that has the same name as the local file
						}
						uploadFileContents(cms, remoteFile, remoteFolder, (IFile)localResources[i]);
					}
				}
			}
		}
		
		//remove files and folders in OpenCms that are not present in Eclipse
		for (int i=0; i < remoteResources.size(); i++) {
			CmsResource remoteResource = (CmsResource)remoteResources.get(i);
			String remoteResourceName = remoteResource.getRootPath(); 
			cms.lockResource(remoteResourceName);
			CmsCompatibilityHelper.deleteResource(cms, remoteResource, remoteResourceName);
		}
	}
	
	private void uploadFileContents(CmsObject cms, CmsFile remoteFile, CmsFolder remoteParentFolder, IFile localFile) 
		throws CmsException, CoreException, IOException {
		
		//read the contents of the local file into a byte array
		byte[] contents = null;
		InputStream contentStream = localFile.getContents(true);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(4096);
		byte[] buffer = new byte[4096];
		int bytesRead = 0;
		try {
			while ((bytesRead = contentStream.read(buffer, 0, 4096)) >= 0) {
				byteArrayOutputStream.write(buffer, 0, bytesRead);
			}
			contents = byteArrayOutputStream.toByteArray();
		} finally {
			if (contentStream != null) {
				try { contentStream.close(); } catch (IOException e) {}
			}
			if (byteArrayOutputStream != null) {
				try { byteArrayOutputStream.close(); } catch (IOException e) {}
			}
		}
		
		//create or replace the OpenCms file
		if (remoteFile == null) {
			//Create a new OpenCms file and upload the contents
			String path = remoteParentFolder.getRootPath();
			String fileName = localFile.getName();
			String extension = getFileExtension(fileName);
			int cmsType = getOpenCmsType(extension);
			cms.createResource(path + fileName, cmsType, contents, null);
		} else {
			//replace the contents of the existing OpenCms file
			String remoteFileName = remoteFile.getRootPath();
			remoteFile.setContents(contents);
			cms.lockResource(remoteFileName);
			cms.writeFile(remoteFile);
		}
	}
	
	
	private CmsResource preserveOpenCmsCounterpart(IResource resource, List remoteResources) {
		CmsResource remoteResource = null;
		String localResourceName = resource.getName(); 
		
		boolean hasCounterPart = false;
		for (int i=0; ((i < remoteResources.size()) && (hasCounterPart == false)); i++) {
			CmsResource cmsResource = (CmsResource)remoteResources.get(i);
			int cmsType = cmsResource.getTypeId();
			String remoteResourceName = cmsResource.getName();
			if (cmsType != OpenCmsConstants.OPENCMS_TYPE_FOLDER) {
				remoteResourceName = correctOpenCmsFileName(remoteResourceName, cmsType);
			}
			
			if (localResourceName.equals(remoteResourceName)) {
				hasCounterPart = true;
				remoteResource = cmsResource;
				remoteResources.remove(cmsResource);
			}
		}
		return remoteResource;
	}

	private String correctOpenCmsFileName(String fileName, int cmsType) {
		String fileExtension = getFileExtension(fileName);
		String requiredExtension = getRequiredExtension(cmsType);
		if ((requiredExtension != null) && (requiredExtension.equals(fileExtension) == false)) {
			fileName += "." + requiredExtension;
		}
		
		return fileName;
	}

	private int getOpenCmsType(String extension) {
		int cmsFileType = OpenCmsConstants.OPENCMS_TYPE_BINARY;
		if(null != extension) extension = extension.toLowerCase();
		if (OpenCmsConstants.OPENCMS_TYPE_MAPPING.containsKey(extension)) {
			cmsFileType = ((Integer)OpenCmsConstants.OPENCMS_TYPE_MAPPING.get(extension)).intValue();
		}
		return cmsFileType;
	}

	private String getFileExtension(String fileName) {
		String extension = EMPTY_STRING;
		if (fileName != null) {
			int dotPosition = fileName.lastIndexOf('.');
			if ((dotPosition >= 0) && (dotPosition < fileName.length())) {
				extension = fileName.substring(dotPosition + 1);
			}
		}
		return extension;
	}

	private String getRequiredExtension(int cmsType) {
		String requiredExtension = null;
		Integer typeId = new Integer(cmsType);
		if (OpenCmsConstants.OPENCMS_EXTENSION_MAPPING.containsKey(typeId)) {
			requiredExtension = (String)OpenCmsConstants.OPENCMS_EXTENSION_MAPPING.get(typeId);
		}
		return requiredExtension;
	}
	
	private IFolder getLocalFolder(IContainer parent, String subFolderName) throws CoreException {
		IPath subFolderPath = parent.getFullPath().append(subFolderName);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFolder localFolder = root.getFolder(subFolderPath);
		if (localFolder.exists() == false) {
			localFolder.create(true, true, null);
		}
		return localFolder;
	}
	
	private IFile getFileHandle(IContainer parent, String filename) throws CoreException {
		IPath filePath = parent.getFullPath().append(filename);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFile(filePath);
		return file;
	}

}
