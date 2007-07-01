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
import info.rsdev.eclipse.opencms.module.developer.Messages;
import info.rsdev.eclipse.opencms.module.developer.data.OpenCmsModuleDescriptor;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.opencms.main.CommunicatorUtils;
import org.opencms.main.ICommunicator;

/**
 * @author Dave Schoorl
 *
 */
public class CreateProjectPage extends WizardPage implements IExchanger {
	
	private OpenCmsModuleDescriptor descriptor = null;
	
	//List of components to collect data from -- TODO: use databinding?
	private Button importIndicatorButton = null;
	private Text moduleNameText = null;
	private Combo moduleNameCombo = null;
	private Button fetchModulesButton = null;
	private Button createTemplates = null;
	private Button createElements = null; 
	private Button createResources = null;
	private Button createClasses = null;
	private Button createLib = null;
	private Button createSource = null;
	
	//Components for rebuilding userinterface
	private Composite parent = null;
	private Composite container = null;
	
	private boolean importIndicator = false;

	public CreateProjectPage(String pageName, OpenCmsModuleDescriptor descriptor) {
		super(pageName);
		setTitle(Messages.wizard_project_title);
		setDescription(Messages.wizard_project_description);
		this.descriptor = descriptor;
		importIndicator = descriptor.isImportModule();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		this.parent = parent;
		buildUserInterface();
	}
	
	private void buildUserInterface() {
		
		if ((container != null) && (!container.isDisposed())) {
			container.dispose();
		}
		clearUserInterface();
		
		//TODO: use databinding?
		
		Composite container = new Composite(parent, SWT.NULL);
	    GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 3;
	    container.setLayout(gridLayout);
	    setControl(container);
	    
	    importIndicatorButton = new Button(container, SWT.CHECK);
	    importIndicatorButton.setText(Messages.wizard_import_module);
	    importIndicatorButton.addSelectionListener(new SelectionListener() {
	    	
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e); 
			}

			public void widgetSelected(SelectionEvent e) {
				Button widget = (Button)e.getSource();
				boolean selection = widget.getSelection();
				String moduleName = moduleNameCombo.getText();
				if (selection != importIndicator) {
					importIndicator = selection;
					fetchModulesButton.setEnabled(selection);
					if (selection == false) {
						moduleNameCombo.setItems(new String[] {});
					}
				}
				moduleNameCombo.setText(moduleName);
			}
	    });
	    
	    GridData importIndicatorGridData = new GridData();
	    importIndicatorGridData.horizontalSpan = 3;
	    importIndicatorButton.setLayoutData(importIndicatorGridData);
	    importIndicatorButton.setSelection(importIndicator);
	    
	    //Due to problems redrawing the wizard page, I take a workaround
	    if (true /*importIndicator*/) {
		    //select modulename from combobox (import = true)
		    Label moduleNameLabel = new Label(container, SWT.NONE);
		    moduleNameLabel.setText(Messages.wizard_label_modulename);
		    
		    moduleNameCombo = new Combo(container, SWT.BORDER);
		    moduleNameCombo.setItems(new String[] {});
		    moduleNameCombo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		    moduleNameCombo.addModifyListener(new ModifyListener() {
		    	
				public void modifyText(ModifyEvent e) {
					//enable or disable next/finish button when module name is not empty or empty
					
					Combo moduleNameCombo = (Combo)e.getSource();
					String data = moduleNameCombo.getText();
					boolean isModuleNameFilled = (data != null) && (data.trim().length() > 0);
					boolean isValidModuleName = true;
					
					//validate if there is not already a project with this name in the workspace
					if (isModuleNameFilled) {
						IWorkspace workspace = ResourcesPlugin.getWorkspace();
						IWorkspaceRoot root= workspace.getRoot();
						isValidModuleName = (root.findMember(data) == null);
					}
					
					if (isValidModuleName) {
						setErrorMessage(null);
					} else {
						
						setErrorMessage(Messages.exception_project_already_exist);
					}
					
					//Validate if modulename contains text
					if ((isPageComplete() && isValidModuleName) == false) {
						getContainer().updateButtons();
					}
					setPageComplete(isValidModuleName);	//update button status
				}
		    	
		    });
		    
		    fetchModulesButton = new Button(container, SWT.NONE);
		    fetchModulesButton.setText(Messages.wizard_label_fetchmodules);
		    fetchModulesButton.setToolTipText(Messages.wizard_tooltip_fetchmodules);
		    fetchModulesButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					String moduleName = moduleNameCombo.getText();
					ICommunicator communicator = null;
					try {
						if (CommunicatorUtils.isProperlyConfigured()) {
							communicator = CommunicatorUtils.getCommunicator();
							List moduleNames = communicator.getModules();
							if (moduleNames.size() > 0) {
								moduleNameCombo.setItems((String[])moduleNames.toArray(new String[moduleNames.size()]));
								if ((moduleName == null) || (moduleName.trim().length() == 0)) {
									moduleNameCombo.select(0);
								} else {
									moduleNameCombo.setText(moduleName);
								}
								
							}
						}
					} catch (CoreException ce) {
						ExceptionUtils.showErrorDialog(ce, getShell());
					} finally {
						CommunicatorUtils.close(communicator, false);
					}
				}
		    });
		    fetchModulesButton.setEnabled(importIndicator);
	    } else {
		    //Set modulename manually (import = false)
		    Label moduleNameLabel = new Label(container, SWT.NONE);
		    moduleNameLabel.setText(Messages.wizard_label_modulename);
		    
		    moduleNameText = new Text(container, SWT.BORDER);
		    moduleNameText.addModifyListener(new ModifyListener() {
	
				public void modifyText(ModifyEvent e) {
					//enable or disable next/finish button when module name is not empty or empty
					
					//TODO: validate if there is not already a project with this name in the workspace
					
					Text moduleNameText = (Text)e.getSource();
					String data = moduleNameText.getText();
					boolean isModuleNameFilled = (data != null) && (data.trim().length() > 0);
					if ((isPageComplete() && isModuleNameFilled) == false) {	//has pageComplete status changed?
						getContainer().updateButtons();
					}
					setPageComplete(isModuleNameFilled);
				}
		    	
		    });
		    GridData moduleNameTextGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		    moduleNameTextGridData.horizontalSpan = 2;
		    moduleNameText.setLayoutData(moduleNameTextGridData);
	    }
	    
	    createTemplates = new Button(container, SWT.CHECK);
	    createTemplates.setText(Messages.wizard_create_templates);
	    GridData createTemplatesGridData = new GridData();
	    createTemplatesGridData.horizontalSpan = 3;
	    createTemplates.setLayoutData(createTemplatesGridData);
	    
	    createElements = new Button(container, SWT.CHECK);
	    createElements.setText(Messages.wizard_create_elements);
	    GridData createElementsGridData = new GridData();
	    createElementsGridData.horizontalSpan = 3;
	    createElements.setLayoutData(createElementsGridData);
	    
	    createResources = new Button(container, SWT.CHECK);
	    createResources.setText(Messages.wizard_create_resources);
	    GridData createResourcesGridData = new GridData();
	    createResourcesGridData.horizontalSpan = 3;
	    createResources.setLayoutData(createElementsGridData);
	    
	    createClasses = new Button(container, SWT.CHECK);
	    createClasses.setText(Messages.wizard_create_classes);
	    GridData createClassesGridData = new GridData();
	    createClassesGridData.horizontalSpan = 3;
	    createClasses.setLayoutData(createClassesGridData);
	    
	    createLib = new Button(container, SWT.CHECK);
	    createLib.setText(Messages.wizard_create_lib);
	    GridData createLibGridData = new GridData();
	    createLibGridData.horizontalSpan = 3;
	    createLib.setLayoutData(createLibGridData);
	    
	    createSource = new Button(container, SWT.CHECK);
	    createSource.setText(Messages.wizard_create_source);
	    GridData createSourceGridData = new GridData();
	    createSourceGridData.horizontalSpan = 3;
	    createSource.setLayoutData(createSourceGridData);
	    
	    setWidgetValues();
	}
	
	private void clearUserInterface() {
		importIndicatorButton = null;
		moduleNameText = null;
		moduleNameCombo = null;
		fetchModulesButton = null;
		createTemplates = null;
		createElements = null; 
		createResources = null;
		createClasses = null;
		createLib = null;
		createSource = null;
		container = null;
		setControl(null);
	}

	public void setWidgetValues() {
	    importIndicatorButton.setSelection(importIndicator);
	    if (true /*importIndicator*/) {
	    	List items = Arrays.asList(moduleNameCombo.getItems());
	    	if ((items != null) && (items.size() > 0)) {
		    	int selectionIndex = items.indexOf(descriptor.getModuleName());
		    	moduleNameCombo.select(selectionIndex);
	    	} else {
	    		moduleNameCombo.setText(descriptor.getModuleName());
	    	}
	    } else {
	    	moduleNameText.setText(descriptor.getModuleName());
	    }
	    createTemplates.setSelection(descriptor.isCreateTemplateFolder());
	    createElements.setSelection(descriptor.isCreateElementsFolder());
	    createResources.setSelection(descriptor.isCreateResourcesFolder());
	    createClasses.setSelection(descriptor.isCreateClassesFolder());
	    createLib.setSelection(descriptor.isCreateLibFolder());
	    createSource.setSelection(descriptor.isCreateSourceFolder());
	}
	
	public void getWidgetValues() {
	    descriptor.setImportModule(importIndicatorButton.getSelection());
	    if (true /*importIndicator*/) {
	    	String moduleName = moduleNameCombo.getText();
//	    	int selectionIndex = moduleNameCombo.getSelectionIndex();
//	    	if (selectionIndex >= 0) {
//	    		List items = Arrays.asList(moduleNameCombo.getItems());
//	    		moduleName = (String)items.get(selectionIndex);
//	    	}
	    	descriptor.setModuleName(moduleName);
	    } else {
	    	descriptor.setModuleName(moduleNameText.getText());
	    }
	    descriptor.setCreateTemplateFolder(createTemplates.getSelection());
	    descriptor.setCreateElementsFolder(createElements.getSelection());
	    descriptor.setCreateResourcesFolder(createResources.getSelection());
	    descriptor.setCreateClassesFolder(createClasses.getSelection());
	    descriptor.setCreateLibFolder(createLib.getSelection());
	    descriptor.setCreateSourceFolder(createSource.getSelection());
	    
	    String moduleName = descriptor.getModuleName();
	    if ((moduleName != null) && (moduleName.trim().length() > 0)) {
	    	setPageComplete(true);
	    }
	}
	
	protected List getModules() {
		List moduleNames = null;
		ICommunicator communicator = null;
		try {
			if (CommunicatorUtils.isProperlyConfigured()) {
				communicator = CommunicatorUtils.getCommunicator();
				moduleNames = communicator.getModules();
			}
		} catch (CoreException ce) {
			ExceptionUtils.showErrorDialog(ce, getShell());
		}
		return moduleNames;
	}
	
}
