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
package info.rsdev.eclipse.opencms.module.developer.data;

import info.rsdev.eclipse.opencms.module.developer.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the information that describes an OpenCms Module.
 * However, it has no dependencies on OpenCms code or types.
 * 
 * @author Dave Schoorl
 */
public class OpenCmsModuleDescriptor {
	
	private static final String EMPTY_STRING = "";
	
	private String moduleName = null;
	
	private String niceName = null;
	
	private String decription = null;
	
	private String version = null;
	
	private String moduleGroupName = null;
	
	private String actionClassname = null;
	
	private String authorName = null;
	
	private String authorEmail = null;
	
	private boolean importModule = true;
	
	private boolean createResourcesFolder = true;
	
	private boolean createTemplateFolder = true;
	
	private boolean createElementsFolder = true;
	
	private boolean createSourceFolder = true;
	
	private boolean createClassesFolder = true;
	
	private boolean createLibFolder = true;
	
	private List resourceTypes = new ArrayList();
	
	private List explorerTypes = new ArrayList();
	
	private List resources = null;
	
	public OpenCmsModuleDescriptor() {}
	
	public OpenCmsModuleDescriptor(String moduleName) {
		if ((moduleName == null) || (moduleName.trim().length() == 0)) {
			throw new IllegalArgumentException(Messages.exception_no_module_name);
		}
		this.moduleName = moduleName;
	}

	public String getActionClassname() {
		if (actionClassname == null) { return EMPTY_STRING; }
		return actionClassname;
	}

	public void setActionClassname(String actionClassname) {
		this.actionClassname = actionClassname;
	}

	public boolean isCreateClassesFolder() {
		return createClassesFolder;
	}

	public void setCreateClassesFolder(boolean createClassesFolder) {
		this.createClassesFolder = createClassesFolder;
	}

	public boolean isCreateElementsFolder() {
		return createElementsFolder;
	}

	public void setCreateElementsFolder(boolean createElementsFolder) {
		this.createElementsFolder = createElementsFolder;
	}

	public boolean isCreateLibFolder() {
		return createLibFolder;
	}

	public void setCreateLibFolder(boolean createLibFolder) {
		this.createLibFolder = createLibFolder;
	}

	public boolean isCreateResourcesFolder() {
		return createResourcesFolder;
	}

	public void setCreateResourcesFolder(boolean createResourcesFolder) {
		this.createResourcesFolder = createResourcesFolder;
	}

	public boolean isCreateSourceFolder() {
		return createSourceFolder;
	}

	public void setCreateSourceFolder(boolean createSourceFolder) {
		this.createSourceFolder = createSourceFolder;
	}

	public boolean isCreateTemplateFolder() {
		return createTemplateFolder;
	}

	public void setCreateTemplateFolder(boolean createTemplateFolder) {
		this.createTemplateFolder = createTemplateFolder;
	}

	public String getDecription() {
		if (decription == null) { return EMPTY_STRING; }
		return decription;
	}

	public void setDecription(String decription) {
		this.decription = decription;
	}

	public List getExplorerTypes() {
		return explorerTypes;
	}

	public void setExplorerTypes(List explorerType) {
		this.explorerTypes = explorerType;
	}

	public boolean isImportModule() {
		return importModule;
	}

	public void setImportModule(boolean importModule) {
		this.importModule = importModule;
	}

	public String getModuleGroupName() {
		if (moduleGroupName == null) { return EMPTY_STRING; }
		return moduleGroupName;
	}

	public void setModuleGroupName(String moduleGroupName) {
		this.moduleGroupName = moduleGroupName;
	}

	public String getModuleName() {
		if (moduleName == null) { return EMPTY_STRING; }
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getNiceName() {
		if (niceName == null) { return EMPTY_STRING; }
		return niceName;
	}

	public void setNiceName(String niceName) {
		this.niceName = niceName;
	}

	public List getResources() {
		return resources;
	}

	public void setResources(List resources) {
		this.resources = resources;
	}

	public List getResourceTypes() {
		return resourceTypes;
	}

	public void setResourceTypes(List resourceTypes) {
		this.resourceTypes = resourceTypes;
	}

	public String getVersion() {
		if (version == null) { return EMPTY_STRING; }
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAuthorEmail() {
		if (authorEmail == null) { return EMPTY_STRING; }
		return authorEmail;
	}

	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}

	public String getAuthorName() {
		if (authorName == null) { return EMPTY_STRING; }
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	
}
