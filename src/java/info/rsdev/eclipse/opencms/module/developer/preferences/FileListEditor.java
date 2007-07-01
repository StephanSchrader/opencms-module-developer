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
package info.rsdev.eclipse.opencms.module.developer.preferences;

import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

/**
 * @author Dave Schoorl
 
 */
public class FileListEditor extends ListEditor {
	
	public static final String FILE_LIST_SEPARATOR = "?";
	
	private String dialogLabel = "Open";
	
	public FileListEditor(String preferenceName, String label, String dialogLabel, Composite parent) {
		super(preferenceName, label, parent);
		if (dialogLabel != null) {
			this.dialogLabel = dialogLabel;
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.ListEditor#createList(java.lang.String[])
	 */
	protected String createList(String[] items) {
		if (items == null) { return null; }
		
		String listAsString = "";
		String separator = "";
		for (int i=0; i<items.length; i++) {
			listAsString += separator + items[i];
			separator = FILE_LIST_SEPARATOR;
		}
		
		return listAsString;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.ListEditor#getNewInputObject()
	 */
	protected String getNewInputObject() {
		//launch open file dialog and return the selected file
        FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
        fd.setText(dialogLabel);
        String[] filterExt = { "*.jar; *.zip"};
        fd.setFilterExtensions(filterExt);
        String selected = fd.open();

		return selected;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.ListEditor#parseString(java.lang.String)
	 */
	protected String[] parseString(String stringList) {
		if (stringList == null) { return null; }
		return stringList.split("\\?");
	}

}
