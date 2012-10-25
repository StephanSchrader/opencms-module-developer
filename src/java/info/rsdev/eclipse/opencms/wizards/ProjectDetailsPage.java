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
package info.rsdev.eclipse.opencms.wizards;

import info.rsdev.eclipse.opencms.Messages;
import info.rsdev.eclipse.opencms.data.OpenCmsModuleDescriptor;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Dave Schoorl
 *
 */
public class ProjectDetailsPage extends WizardPage implements IExchanger {
	
	private OpenCmsModuleDescriptor descriptor = null;
	
	//List of components to collect data from -- TODO: use databinding
	private Text niceName = null;
	private Text description = null;
	private Text version = null;
	private Text moduleGroup = null;
	private Text actionClass = null;
	private Text authorName = null;
	private Text authorEmail = null;

	public ProjectDetailsPage(String pageName, OpenCmsModuleDescriptor descriptor) {
		super(pageName);
		setTitle(Messages.wizard_details_title);
		setDescription(Messages.wizard_details_description);
		this.descriptor = descriptor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		
		//TODO: use databinding
		
		Composite container = new Composite(parent, SWT.NULL);
	    GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 2;
	    container.setLayout(gridLayout);
	    setControl(container);
	    
	    Label niceNameLabel = new Label(container, SWT.NONE);
	    niceNameLabel.setText(Messages.wizard_label_nicename);
	    niceName = new Text(container, SWT.BORDER);
	    niceName.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
	    
	    Label descriptionLabel = new Label(container, SWT.NONE);
	    descriptionLabel.setText(Messages.wizard_label_description);
	    GridData descriptionLabelGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	    descriptionLabelGridData.horizontalSpan = 2;
	    descriptionLabel.setLayoutData(descriptionLabelGridData);
	    
	    description = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
	    GridData descriptionGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL );
	    descriptionGridData.horizontalSpan = 2;
	    descriptionGridData.verticalSpan = 3;
	    description.setLayoutData(descriptionGridData);
	    
	    Label versionLabel = new Label(container, SWT.NONE);
	    versionLabel.setText(Messages.wizard_label_version);
	    version = new Text(container, SWT.BORDER);
	    version.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
	    
	    Label moduleGroupLabel = new Label(container, SWT.NONE);
	    moduleGroupLabel.setText(Messages.wizard_label_modulegroup);
	    moduleGroup = new Text(container, SWT.BORDER);
	    moduleGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
	    
	    Label actionClassLabel = new Label(container, SWT.NONE);
	    actionClassLabel.setText(Messages.wizard_label_actionclass);
	    actionClass = new Text(container, SWT.BORDER);
	    actionClass.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
	    
	    Label authorNameLabel = new Label(container, SWT.NONE);
	    authorNameLabel.setText(Messages.wizard_label_authorname);
	    authorName = new Text(container, SWT.BORDER);
	    authorName.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
	    
	    Label authorEmailLabel = new Label(container, SWT.NONE);
	    authorEmailLabel.setText(Messages.wizard_label_authoremail);
	    authorEmail = new Text(container, SWT.BORDER);
	    authorEmail.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
	    
	    setWidgetValues();
	}
	
	public void setWidgetValues() {
		niceName.setText(descriptor.getNiceName());
		description.setText(descriptor.getDecription());
		version.setText(descriptor.getVersion());
		moduleGroup.setText(descriptor.getModuleGroupName());
		actionClass.setText(descriptor.getActionClassname());
		authorName.setText(descriptor.getAuthorName());
		authorEmail.setText(descriptor.getAuthorEmail());
	}
	
	public void getWidgetValues() {
		descriptor.setNiceName(niceName.getText());
		descriptor.setDecription(description.getText());
		descriptor.setVersion(version.getText());
		descriptor.setModuleGroupName(moduleGroup.getText());
		descriptor.setActionClassname(actionClass.getText());
		descriptor.setAuthorName(authorName.getText());
		descriptor.setAuthorEmail(authorEmail.getText());
	}
	
}
